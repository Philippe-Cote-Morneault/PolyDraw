package com.log3900.draw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.divyanshu.draw.widget.DrawView
import com.log3900.R

// See https://github.com/divyanshub024/AndroidDraw
// and https://android.jlelse.eu/a-guide-to-drawing-in-android-631237ab6e28

class DrawViewFragment : Fragment() {
    lateinit var drawView: DrawView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_draw_view, container, false)
        setUpUi(root)
        return root
    }

    private fun setUpUi(root: View) {
        drawView = root.findViewById(R.id.draw_view_canvas)
    }
}