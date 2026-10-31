// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import helium314.keyboard.keyboard.PointerTracker

/**
 * "Peek": while the user holds a peek button, the whole keyboard except that button is hidden and
 * the keyboard reports zero height to the app, so the app shows what was hidden behind the keyboard.
 * Releasing the button shows the keyboard again immediately.
 */
class KeyboardPeek(private val ime: LatinIME) {

    var isPeeking = false
        private set
    private var keepView: View? = null

    private class SavedAncestor(val view: View, val background: Drawable?, val elevation: Float, val clip: Boolean)
    private val savedAncestors = ArrayList<SavedAncestor>()
    private val savedAlpha = HashMap<View, Float>()
    private val location = IntArray(2)

    fun start(keep: View) {
        if (isPeeking) return
        val root = ime.mInputView as? ViewGroup ?: return
        isPeeking = true
        keepView = keep
        PointerTracker.dismissAllPopupKeysPanels()

        // walk up from the button to the input view: fade out all siblings, remove ancestor backgrounds
        var child: View = keep
        var parent = child.parent
        while (parent is ViewGroup) {
            for (i in 0 until parent.childCount) {
                val sibling = parent.getChildAt(i)
                if (sibling !== child) fade(sibling, hide = true)
            }
            savedAncestors.add(SavedAncestor(parent, parent.background, parent.elevation, parent.clipToOutline))
            parent.background = null
            parent.elevation = 0f
            parent.clipToOutline = false
            if (parent === root) break
            child = parent
            parent = parent.parent
        }
        ime.onPeekChanged(true)
    }

    fun end() {
        if (!isPeeking) return
        isPeeking = false
        for (saved in savedAncestors) {
            saved.view.background = saved.background
            saved.view.elevation = saved.elevation
            saved.view.clipToOutline = saved.clip
        }
        savedAncestors.clear()
        for (view in savedAlpha.keys.toList()) fade(view, hide = false)
        savedAlpha.clear()
        keepView = null
        ime.onPeekChanged(false)
    }

    /** rect of the peek button in window coordinates, the only touchable area while peeking */
    fun getTouchableRect(outRect: Rect): Boolean {
        val keep = keepView ?: return false
        if (!isPeeking || !keep.isAttachedToWindow) return false
        keep.getLocationInWindow(location)
        outRect.set(location[0], location[1], location[0] + keep.width, location[1] + keep.height)
        return !outRect.isEmpty
    }

    private fun fade(view: View, hide: Boolean) {
        view.animate().cancel()
        val target = if (hide) {
            if (!savedAlpha.containsKey(view)) savedAlpha[view] = view.alpha
            0f
        } else savedAlpha[view] ?: 1f
        view.animate().alpha(target).setDuration(FADE_MS).start()
    }

    companion object {
        private const val FADE_MS = 80L
    }
}
