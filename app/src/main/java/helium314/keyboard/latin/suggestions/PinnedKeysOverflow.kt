// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.suggestions

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.edit
import androidx.core.view.children
import androidx.core.view.isVisible
import helium314.keyboard.event.HapticEvent
import helium314.keyboard.keyboard.KeyboardSwitcher
import helium314.keyboard.keyboard.internal.KeyboardIconsSet
import helium314.keyboard.keyboard.internal.keyboard_parser.floris.KeyCode
import helium314.keyboard.latin.AudioAndHapticFeedbackManager
import helium314.keyboard.latin.R
import helium314.keyboard.latin.common.ColorType
import helium314.keyboard.latin.common.Colors
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.ResourceUtils
import helium314.keyboard.latin.utils.ToolbarKey
import helium314.keyboard.latin.utils.dpToPx
import helium314.keyboard.latin.utils.prefs

/** Size of the toolbar keys: only the key width and the icon shrink, the strip height stays. */
object ToolbarKeySize {
    const val NORMAL = "normal"
    const val COMPACT = "compact"
    const val SMALL = "small"

    fun factor(prefs: SharedPreferences): Float =
        when (prefs.getString(Settings.PREF_TOOLBAR_KEY_SIZE, Defaults.PREF_TOOLBAR_KEY_SIZE)) {
            COMPACT -> 0.85f
            SMALL -> 0.7f
            else -> 1f
        }

    /** padding used by createToolbarKey for a square key */
    fun basePadding(res: Resources): Int {
        val defaultStripHeight = res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height).toFloat()
        val stripHeight = ResourceUtils.getSuggestionsStripHeight(res).toFloat()
        val effectiveScale = if (defaultStripHeight > 0f) stripHeight / defaultStripHeight else 1.0f
        return (9 * effectiveScale).toInt().dpToPx(res).coerceAtLeast(2)
    }

    /** scales the icon by [factor] inside a key of full height [keyDimension] */
    fun applyPadding(view: View, keyDimension: Int, factor: Float) {
        val padding = basePadding(view.resources)
        if (factor >= 1f) {
            view.setPadding(padding, padding, padding, padding)
            return
        }
        val icon = ((keyDimension - 2 * padding) * factor).toInt().coerceAtLeast(1)
        val horizontal = (padding * factor).toInt().coerceAtLeast(1)
        val vertical = ((keyDimension - icon) / 2).coerceAtLeast(0)
        view.setPadding(horizontal, vertical, horizontal, vertical)
    }
}

/**
 * Narrow screens: when suggestions are shown, only as many pinned toolbar keys stay visible as fit
 * next to the suggestions. An arrow on the right expands / collapses the remaining keys.
 * Tap: toggle until the next input field. Long press: lock the current state (shown by an underline),
 * long press again: back to automatic.
 */
class PinnedKeysOverflow(private val context: Context, private val onStateChanged: () -> Unit) {
    val button = OverflowArrowButton(context)

    /** whether the strip currently shows suggestions, set by the strip before laying them out */
    var hasSuggestions = false
    private var temporaryExpanded: Boolean? = null

    private val prefs get() = context.prefs()
    private fun lockMode() =
        prefs.getString(Settings.PREF_PINNED_KEYS_OVERFLOW_LOCK, Defaults.PREF_PINNED_KEYS_OVERFLOW_LOCK) ?: LOCK_NONE

    private val isLocked get() = lockMode() != LOCK_NONE

    private val isExpanded: Boolean
        get() = when (lockMode()) {
            LOCK_EXPANDED -> true
            LOCK_COLLAPSED -> false
            else -> temporaryExpanded ?: !hasSuggestions
        }

    init {
        button.contentDescription = context.getString(R.string.pinned_keys_overflow_expand)
        button.setOnClickListener { v ->
            AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(KeyCode.NOT_SPECIFIED, v, HapticEvent.KEY_PRESS)
            when (lockMode()) {
                LOCK_EXPANDED -> setLock(LOCK_COLLAPSED)
                LOCK_COLLAPSED -> setLock(LOCK_EXPANDED)
                else -> temporaryExpanded = !isExpanded
            }
            onStateChanged()
        }
        button.setOnLongClickListener { v ->
            AudioAndHapticFeedbackManager.getInstance().performHapticFeedback(v, HapticEvent.KEY_LONG_PRESS)
            val message = if (isLocked) {
                setLock(LOCK_NONE)
                temporaryExpanded = null
                R.string.pinned_keys_overflow_unlocked
            } else if (isExpanded) {
                setLock(LOCK_EXPANDED)
                R.string.pinned_keys_overflow_locked_expanded
            } else {
                setLock(LOCK_COLLAPSED)
                R.string.pinned_keys_overflow_locked_collapsed
            }
            KeyboardSwitcher.getInstance().showToast(context.getString(message), true)
            onStateChanged()
            true
        }
    }

    private fun setLock(mode: String) = prefs.edit { putString(Settings.PREF_PINNED_KEYS_OVERFLOW_LOCK, mode) }

    /** a new input field starts in automatic mode again (unless locked) */
    fun resetTemporary() {
        temporaryExpanded = null
    }

    /** adds the arrow as last view of the pinned keys, call after (re)creating the pinned keys */
    fun attach(pinnedKeys: ViewGroup, keyWidth: Int, keyDimension: Int, factor: Float, colors: Colors) {
        (button.parent as? ViewGroup)?.removeView(button)
        button.layoutParams = LinearLayout.LayoutParams(keyWidth, keyDimension).apply {
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        button.minimumWidth = 0
        button.minimumHeight = 0
        button.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
        button.setBackgroundResource(R.drawable.toolbar_key_background)
        button.setImageDrawable(KeyboardIconsSet.instance.getNewDrawable(KeyboardIconsSet.NAME_TOOLBAR_KEY, context))
        ToolbarKeySize.applyPadding(button, keyDimension, factor)
        colors.setColor(button, ColorType.TOOL_BAR_EXPAND_KEY)
        colors.setColor(button.background, ColorType.TOOL_BAR_EXPAND_KEY_BACKGROUND)
        button.setLineColor(colors.get(ColorType.TOOL_BAR_EXPAND_KEY))
        button.isVisible = false
        pinnedKeys.addView(button)
    }

    /**
     * Shows as many pinned keys as fit into [availableWidth] next to [minSuggestionsWidth].
     * @return whether the visibility of any key changed
     */
    fun apply(pinnedKeys: ViewGroup, availableWidth: Int, keyWidth: Int, minSuggestionsWidth: Int): Boolean {
        if (button.parent !== pinnedKeys) return false
        val showVoice = Settings.getValues().mShowsVoiceInputKey
        val candidates = pinnedKeys.children
            .filter { it !== button && it.tag is ToolbarKey && (it.tag != ToolbarKey.VOICE || showVoice) }
            .toList()
        val count = candidates.size
        val free = availableWidth - minSuggestionsWidth
        val fitting = if (availableWidth <= 0 || keyWidth <= 0 || count * keyWidth <= free) count
            else ((free - keyWidth) / keyWidth).coerceIn(0, count)
        val overflow = fitting < count
        // expanded: as many as fit next to the arrow (on very narrow screens not even all keys fit)
        val fittingExpanded = if (availableWidth <= 0 || (count + 1) * keyWidth <= availableWidth) count
            else ((availableWidth - keyWidth) / keyWidth).coerceIn(fitting, count)
        val visibleCount = if (!overflow) count else if (isExpanded) fittingExpanded else fitting

        var changed = false
        candidates.forEachIndexed { index, view ->
            val visible = index < visibleCount
            if (view.isVisible != visible) {
                view.isVisible = visible
                changed = true
            }
        }
        if (button.isVisible != overflow) {
            button.isVisible = overflow
            changed = true
        }
        val expanded = !overflow || isExpanded
        val rtl = pinnedKeys.layoutDirection == View.LAYOUT_DIRECTION_RTL
        // the icon is an arrow to the right: ">" collapses, "<" expands
        button.scaleX = (if (expanded) 1f else -1f) * (if (rtl) -1f else 1f)
        button.locked = isLocked
        button.contentDescription = context.getString(
            if (expanded) R.string.pinned_keys_overflow_collapse else R.string.pinned_keys_overflow_expand
        )
        return changed
    }

    companion object {
        const val LOCK_NONE = "none"
        const val LOCK_EXPANDED = "expanded"
        const val LOCK_COLLAPSED = "collapsed"

        /** minimum width of a single suggestion when deciding how many pinned keys fit */
        fun minSuggestionWidth(res: Resources): Int {
            val defaultStripHeight = res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height).toFloat()
            val stripHeight = ResourceUtils.getSuggestionsStripHeight(res).toFloat()
            val scale = if (defaultStripHeight > 0f) stripHeight / defaultStripHeight else 1f
            return (64 * res.displayMetrics.density * scale).toInt()
        }
    }
}

/** Toolbar arrow that can show an underline (like the caps lock indicator) when locked. */
@SuppressLint("AppCompatCustomView")
class OverflowArrowButton(context: Context) : ImageButton(context, null, R.attr.suggestionWordStyle) {
    var locked = false
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 2f * resources.displayMetrics.density
    }

    fun setLineColor(color: Int) {
        linePaint.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!locked) return
        val density = resources.displayMetrics.density
        val iconWidth = (width - paddingLeft - paddingRight).coerceAtLeast(1)
        val half = iconWidth * 0.32f
        val centerX = width / 2f
        // directly below the arrow (the icon box has some empty space at its bottom)
        val iconHeight = (height - paddingTop - paddingBottom).coerceAtLeast(1)
        val y = height - paddingBottom - iconHeight * 0.1f + density
        canvas.drawLine(centerX - half, y, centerX + half, y, linePaint)
    }
}
