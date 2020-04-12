package com.log3900.shared.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.EditText

class SmartEditText : EditText {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(context, attributeSet, defStyle)

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if (selEnd < text.length) {
            post { setSelection(text.length) }
        }
    }
}