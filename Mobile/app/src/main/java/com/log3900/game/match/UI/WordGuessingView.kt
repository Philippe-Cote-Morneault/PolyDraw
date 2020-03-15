package com.log3900.game.match.UI

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import com.log3900.R

class WordGuessingView(context: Context) : ConstraintLayout(context) {
    private var layout: ConstraintLayout
    private var editTextContainer: LinearLayout
    private var guessButton: MaterialButton
    private var letterEditTexts: ArrayList<EditText> = ArrayList()
    var listener: Listener? = null

    init {
        layout = View.inflate(context, R.layout.view_word_guessing, this) as ConstraintLayout
        editTextContainer = layout.findViewById(R.id.view_word_guessing_edit_text_container)
        guessButton = layout.findViewById(R.id.view_word_guessing_button_guess)
        guessButton.setOnClickListener {
            if (listener != null) {
                listener?.onGuessPressed(getText())
            }
        }

    }

    fun setWordLength(wordLength: Int) {
        editTextContainer.removeAllViews()
        letterEditTexts.clear()

        for (i in 0 until wordLength) {
            val newEditText = generateEditText()
            letterEditTexts.add(newEditText)
            editTextContainer.addView(newEditText)
        }
    }

    fun getText(): String {
        var text = ""
        letterEditTexts.forEach {
            text += it.text.toString()
        }

        return text
    }

    private fun generateEditText(): EditText {
        val editText = EditText(context)
        editText.setEms(1)
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        editText.filters += InputFilter.LengthFilter(1)
        editText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        setTextChangedListener(editText)

        return editText
    }

    private fun setTextChangedListener(editText: EditText) {
        editText.doAfterTextChanged {
            val newValue = it?.toString()
            if (newValue != null && newValue.isNotEmpty()) {
                val editTextIndex = letterEditTexts.indexOf(editText)

                if (editTextIndex < letterEditTexts.size - 1) {
                    letterEditTexts[editTextIndex + 1].requestFocus()
                }
            }
        }
        editText.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (editText.text.isEmpty()) {
                        val editTextIndex = letterEditTexts.indexOf(editText)

                        if (editTextIndex > 0) {
                            letterEditTexts[editTextIndex - 1].requestFocus()
                        }
                    }
                }
            }
            false
        }
    }

    interface Listener {
        fun onGuessPressed(text: String)
    }
}