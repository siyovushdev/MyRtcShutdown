package ru.firstmt.myrtcshutdown.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat

class ItemView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {
    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        if (gainFocus) {
            ViewCompat.animate(this).scaleX(1.1f).scaleY(1.1f).translationZ(1f).start()
        } else {
            ViewCompat.animate(this).scaleX(1.0f).scaleY(1.0f).translationZ(1f).start()
        }
    }
}