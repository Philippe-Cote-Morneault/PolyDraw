package com.log3900.game.match.UI

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.log3900.R

class WordToDrawView(context: Context) : ConstraintLayout(context) {
    private var layout: ConstraintLayout
    private var wordTitleTextView: TextView
    private var wordTextView: TextView

    init {
        layout = View.inflate(context, R.layout.view_word_to_draw, this) as ConstraintLayout
        wordTitleTextView = layout.findViewById(R.id.view_word_to_draw_text_view_word_title)
        wordTextView = layout.findViewById(R.id.view_word_to_draw_text_view_word)
    }

    fun setWordToGuess(word: String) {
        wordTextView.text = word
    }
}