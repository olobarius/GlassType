// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.view.View
import android.view.WindowManager
import androidx.core.graphics.ColorUtils
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import kotlin.math.max

/**
 * Transparent keyboard modes:
 * - [MODE_DESKTOP]: the home screen wallpaper is drawn behind the keys, aligned to the exact screen
 *   position of the keyboard, so it looks like the home screen shines through.
 * - [MODE_SEE_THROUGH]: the keyboard background is drawn translucent, so the app window behind it is visible.
 * In both modes the theme background color is laid over with the user-defined opacity.
 */
object KeyboardTransparency {
    const val MODE_OFF = "off"
    const val MODE_DESKTOP = "desktop"
    const val MODE_SEE_THROUGH = "see_through"

    private var cachedBitmap: Bitmap? = null
    private var cachedKey: String? = null

    fun mode(prefs: SharedPreferences): String =
        prefs.getString(Settings.PREF_TRANSPARENCY_MODE, Defaults.PREF_TRANSPARENCY_MODE) ?: MODE_OFF

    fun isActive(prefs: SharedPreferences) = mode(prefs) != MODE_OFF

    /** alpha (0..255) of the theme background color drawn over the wallpaper / app behind */
    fun backgroundAlpha(prefs: SharedPreferences): Int =
        percentToAlpha(prefs.getInt(Settings.PREF_BACKGROUND_OPACITY, Defaults.PREF_BACKGROUND_OPACITY))

    /** alpha (0..255) for key backgrounds, 255 if transparency is off */
    fun keyAlpha(prefs: SharedPreferences): Int =
        if (!isActive(prefs)) 255
        else percentToAlpha(prefs.getInt(Settings.PREF_KEY_OPACITY, Defaults.PREF_KEY_OPACITY))

    private fun percentToAlpha(percent: Int) = (percent.coerceIn(0, 100) * 255 + 50) / 100

    /**
     * whether the real home screen wallpaper can be read. Samsung (Android 15) checks the photos permission
     * READ_MEDIA_IMAGES, stock Android 14+ requires "all files access".
     */
    fun canReadSystemWallpaper(context: Context): Boolean =
        WallpaperFetcher.getSystemWallpaper(context) != null

    fun hasAllFilesAccess(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()

    /** quick toggle: off <-> last used transparent mode (home screen by default) */
    fun toggle(context: Context) {
        val prefs = context.prefs()
        val current = mode(prefs)
        val next = if (current == MODE_OFF) {
            prefs.getString(Settings.PREF_TRANSPARENCY_LAST_MODE, MODE_DESKTOP) ?: MODE_DESKTOP
        } else MODE_OFF
        val editor = prefs.edit().putString(Settings.PREF_TRANSPARENCY_MODE, next)
        if (current != MODE_OFF) editor.putString(Settings.PREF_TRANSPARENCY_LAST_MODE, current)
        editor.apply()
        Settings.clearCachedBackgroundImages(context)
    }

    /** remember the mode chosen in settings for the quick toggle */
    fun rememberMode(prefs: SharedPreferences, mode: String) {
        if (mode != MODE_OFF) prefs.edit().putString(Settings.PREF_TRANSPARENCY_LAST_MODE, mode).apply()
    }

    fun clearCache() {
        cachedBitmap = null
        cachedKey = null
    }

    /** to be called when the system wallpaper changed */
    fun onWallpaperChanged(context: Context) {
        try {
            Settings.getSystemWallpaperFile(context, false).delete()
            Settings.getSystemWallpaperFile(context, true).delete()
        } catch (_: Exception) { }
        clearCache()
        Settings.clearCachedBackgroundImages(context)
    }

    /** returns null if transparency is off */
    fun createBackground(context: Context, prefs: SharedPreferences, night: Boolean): TransparentKeyboardBackground? {
        return when (mode(prefs)) {
            MODE_DESKTOP -> TransparentKeyboardBackground(loadWallpaper(context, prefs, night))
            MODE_SEE_THROUGH -> TransparentKeyboardBackground(null)
            else -> null
        }
    }

    private fun loadWallpaper(context: Context, prefs: SharedPreferences, night: Boolean): Bitmap? {
        val landscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val blur = prefs.getInt(Settings.PREF_BACKGROUND_BLUR_AMOUNT, Defaults.PREF_BACKGROUND_BLUR_AMOUNT)
        val source = findWallpaperFile(context, night, landscape) ?: return null
        val key = "${source.absolutePath}|${source.lastModified()}|$blur"
        if (key == cachedKey) cachedBitmap?.let { if (!it.isRecycled) return it }

        val bitmap = try {
            val raw = BitmapUtils.decodeSampledBitmap(source, 2048, true) ?: return null
            if (blur <= 0) raw
            else FastBlurEngine.generateBlurredThumbnail(raw, maxThumbnailDim = 400, blurPasses = 3, radius = blur * 2)
                .also { if (it != raw) raw.recycle() }
        } catch (e: Throwable) {
            Log.w("KeyboardTransparency", "could not load wallpaper: ${e.message}")
            null
        }
        cachedBitmap = bitmap
        cachedKey = key
        return bitmap
    }

    /** system wallpaper (cached as file) or, as fallback, the user selected background image */
    private fun findWallpaperFile(context: Context, night: Boolean, landscape: Boolean): java.io.File? {
        val sysFile = Settings.getSystemWallpaperFile(context, false)
        if (!sysFile.isFile) {
            WallpaperFetcher.getSystemWallpaper(context)?.let { wp ->
                try {
                    sysFile.outputStream().use { wp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                } catch (e: Exception) {
                    Log.w("KeyboardTransparency", "could not cache wallpaper: ${e.message}")
                }
            }
        }
        if (sysFile.isFile) return sysFile
        val custom = Settings.getCustomBackgroundFile(context, night, landscape)
        if (custom.isFile) return custom
        val customPortrait = Settings.getCustomBackgroundFile(context, night, false)
        if (customPortrait.isFile) return customPortrait
        val customDay = Settings.getCustomBackgroundFile(context, false, false)
        return customDay.takeIf { it.isFile }
    }
}

/**
 * Draws the wallpaper exactly at the screen position the keyboard occupies (center-crop to the whole
 * display, like the launcher does), then the theme background color with the chosen opacity on top.
 */
class TransparentKeyboardBackground(private val wallpaper: Bitmap?) : Drawable() {
    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
    private val overlayPaint = Paint().apply { style = Paint.Style.FILL; color = Color.TRANSPARENT }
    private val location = IntArray(2)
    private val screen = Rect()

    fun setOverlay(themeBackground: Int, alpha: Int) {
        overlayPaint.color = ColorUtils.setAlphaComponent(themeBackground, alpha.coerceIn(0, 255))
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.isEmpty) return
        val bm = wallpaper
        if (bm != null && !bm.isRecycled) {
            val view = callback as? View
            val screenSize = screenSize(view)
            val sw = screenSize.width().toFloat()
            val sh = screenSize.height().toFloat()
            // position of the drawing view on screen; the drawable may be shared between views,
            // so only trust the callback view if it has the same size as our bounds
            val viewLeft: Float
            val viewTop: Float
            if (view != null && view.isAttachedToWindow && view.width == b.width() && view.height == b.height()) {
                view.getLocationOnScreen(location)
                viewLeft = location[0].toFloat()
                viewTop = location[1].toFloat()
            } else {
                viewLeft = 0f
                viewTop = sh - b.height()
            }
            val scale = max(sw / bm.width, sh / bm.height)
            val offsetX = (sw - bm.width * scale) / 2f
            val offsetY = (sh - bm.height * scale) / 2f
            canvas.save()
            canvas.clipRect(b)
            canvas.translate(b.left + offsetX - viewLeft, b.top + offsetY - viewTop)
            canvas.scale(scale, scale)
            canvas.drawBitmap(bm, 0f, 0f, bitmapPaint)
            canvas.restore()
        }
        if (Color.alpha(overlayPaint.color) > 0)
            canvas.drawRect(b, overlayPaint)
    }

    private fun screenSize(view: View?): Rect {
        val ctx = view?.context
        if (ctx != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val wm = ctx.getSystemService(WindowManager::class.java)
                if (wm != null) {
                    screen.set(wm.maximumWindowMetrics.bounds)
                    if (!screen.isEmpty) return screen
                }
            } catch (_: Exception) { }
        }
        val dm = (ctx?.resources ?: android.content.res.Resources.getSystem()).displayMetrics
        screen.set(0, 0, dm.widthPixels, max(dm.heightPixels, bounds.height()))
        return screen
    }

    override fun setAlpha(alpha: Int) {
        bitmapPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // colors are controlled via setOverlay, filters would tint the wallpaper
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
