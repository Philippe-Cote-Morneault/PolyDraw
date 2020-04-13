package com.log3900.game.match.UI

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import com.log3900.R
import com.log3900.shared.ui.SmartEditText

class WordGuessingView(context: Context) : ConstraintLayout(context) {
    private var layout: ConstraintLayout
    private var editTextContainer: LinearLayout
    private var guessButton: MaterialButton
    private var hintButton: MaterialButton
    private var letterEditTexts: ArrayList<SmartEditText> = ArrayList()
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

        hintButton = layout.findViewById(R.id.view_word_guessing_button_hint)
        hintButton.setOnClickListener {
            listener?.onHintPressed()
        }

    }

    fun clearCurrentText() {
        letterEditTexts.forEach {
            it.setText("")
        }
    }

    fun showHintButton(show: Boolean) {
        if (show) {
            hintButton.visibility = View.VISIBLE
        } else {
            hintButton.visibility = View.GONE
        }
    }

    fun enableActions(enable: Boolean) {
        enableHintButton(enable)
        enableGuessButton(enable)
        enableEditTexts(enable)
    }

    fun enableHintButton(enable: Boolean) {
        hintButton.isEnabled = enable
    }

    fun enableGuessButton(enable: Boolean) {
        guessButton.isEnabled = enable
    }

    fun enableEditTexts(enable: Boolean) {
        letterEditTexts.forEach {
            it.isEnabled = enable
        }
    }

    fun setWordLength(wordLength: Int) {
        editTextContainer.removeAllViews()
        letterEditTexts.clear()
        enableGuessButton(false)

        for (i in 0 until wordLength) {
            val newEditText = generateEditText()
            letterEditTexts.add(newEditText)
            editTextContainer.addView(newEditText)
        }
    }

    fun setWordGuessTextColor(color: Int) {
        letterEditTexts.forEach { it.setTextColor(color) }
    }

    fun getText(): String {
        var text = ""
        letterEditTexts.forEach {
            text += it.text.toString()
        }

        return text
    }

    fun getEditTexts(): ArrayList<SmartEditText> {
        return letterEditTexts
    }

    private fun generateEditText(): SmartEditText {
        val editText = SmartEditText(context)
        editText.setEms(1)
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        editText.filters += InputFilter.LengthFilter(1)
        editText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        setTextChangedListener(editText)

        editText.imeOptions = EditorInfo.IME_ACTION_SEND
        editText.setOnEditorActionListener { textView, actionID, keyEvent ->
            return@setOnEditorActionListener when (actionID) {
                EditorInfo.IME_ACTION_SEND -> {
                    if (listener != null) {
                        val text = getText()
                        if (text.length == letterEditTexts.size) {
                            listener?.onGuessPressed(text)
                        }
                    }
                    true
                }
                else -> false
            }
        }

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
                var allFull = true
                letterEditTexts.forEach {
                    if (it.text.toString().isEmpty()) {
                        allFull = false
                    }
                }

                if (allFull) {
                    enableGuessButton(true)
                } else {
                    enableGuessButton(false)
                }
            } else {
                enableGuessButton(false)
            }
        }
        editText.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (editText.text.isEmpty()) {
                        val editTextIndex = letterEditTexts.indexOf(editText)

                        if (editTextIndex > 0) {
                            letterEditTexts[editTextIndex - 1].requestFocus()
                            letterEditTexts[editTextIndex - 1].setText("")
                        }
                    }
                }
            }
            false
        }
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                editText.post {
                    editText.setSelection(editText.text.length)
                }
            }
        }
    }

    interface Listener {
        fun onGuessPressed(text: String)
        fun onHintPressed()
    }
}