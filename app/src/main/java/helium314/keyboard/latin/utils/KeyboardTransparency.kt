// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.core.content.edit
import androidx.core.graphics.ColorUtils
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings

/**
 * Transparent keyboard: the keyboard background is drawn translucent, so the app window behind it
 * is visible. The theme background color is laid over with the user-defined opacity.
 * No permissions are needed, nothing is read from the system.
 */
object KeyboardTransparency {
    const val MODE_OFF = "off"
    const val MODE_SEE_THROUGH = "see_through"

    /** any stored value other than "off" (including the removed "desktop" mode) means transparent */
    fun isActive(prefs: SharedPreferences) =
        (prefs.getString(Settings.PREF_TRANSPARENCY_MODE, Defaults.PREF_TRANSPARENCY_MODE) ?: MODE_OFF) != MODE_OFF

    fun setActive(prefs: SharedPreferences, active: Boolean) =
        prefs.edit { putString(Settings.PREF_TRANSPARENCY_MODE, if (active) MODE_SEE_THROUGH else MODE_OFF) }

    /** quick toggle in the toolbar */
    fun toggle(context: Context) {
        val prefs = context.prefs()
        setActive(prefs, !isActive(prefs))
    }

    /** alpha (0..255) of the theme background color drawn over the app behind */
    fun backgroundAlpha(prefs: SharedPreferences): Int =
        percentToAlpha(prefs.getInt(Settings.PREF_BACKGROUND_OPACITY, Defaults.PREF_BACKGROUND_OPACITY))

    /** alpha (0..255) for key backgrounds, 255 if transparency is off */
    fun keyAlpha(prefs: SharedPreferences): Int =
        if (!isActive(prefs)) 255
        else percentToAlpha(prefs.getInt(Settings.PREF_KEY_OPACITY, Defaults.PREF_KEY_OPACITY))

    private fun percentToAlpha(percent: Int) = (percent.coerceIn(0, 100) * 255 + 50) / 100

    /** returns null if transparency is off */
    fun createBackground(prefs: SharedPreferences): TransparentKeyboardBackground? =
        if (isActive(prefs)) TransparentKeyboardBackground() else null
}

/** Keyboard background: only the theme background color with the chosen opacity. */
class TransparentKeyboardBackground : Drawable() {
    private val overlayPaint = Paint().apply { style = Paint.Style.FILL; color = Color.TRANSPARENT }

    fun setOverlay(themeBackground: Int, alpha: Int) {
        overlayPaint.color = ColorUtils.setAlphaComponent(themeBackground, alpha.coerceIn(0, 255))
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        if (!bounds.isEmpty && Color.alpha(overlayPaint.color) > 0)
            canvas.drawRect(bounds, overlayPaint)
    }

    override fun setAlpha(alpha: Int) {
        // the opacity is controlled via setOverlay
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // colors are controlled via setOverlay
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
