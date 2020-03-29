package com.log3900.game.match.UI

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.log3900.R

class CanvasMessageView(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {
    private var layout: ConstraintLayout
    private var messageTextView: TextView

    init {
        layout = View.inflate(context, R.layout.view_canvas_message, this) as ConstraintLayout
        messageTextView = layout.findViewById(R.id.view_canvas_message_text_view)
    }

    fun setMessage(message: String) {
        messageTextView.text = message
    }
}