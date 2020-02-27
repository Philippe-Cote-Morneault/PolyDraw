package com.log3900.draw

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.scaleMatrix
import androidx.fragment.app.Fragment
import com.divyanshu.draw.widget.DrawView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.log3900.R
import kotlinx.android.synthetic.main.fragment_draw_view.*
import kotlinx.android.synthetic.main.fragment_draw_view.draw_tools_fab
import kotlinx.android.synthetic.main.fragment_draw_view.draw_tools_view
import kotlinx.android.synthetic.main.view_draw_color_palette.*

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpFab()
        setUpColorButtons()
    }

    private fun setUpUi(root: View) {
        drawView = root.findViewById(R.id.draw_view_canvas)

        val draw: FloatingActionButton  = root.findViewById(R.id.draw_tools_fab)
        println(draw)
        println(draw_tools_fab)

//        draw_tools_fab.setOnClickListener {
//            draw_tools_view.visibility =
//                if (it.visibility == View.GONE)
//                    View.VISIBLE
//                else
//                    View.GONE
//        }
    }

    private fun setUpFab() {
        draw_tools_fab.setOnClickListener {
            draw_tools_view.apply {
                if (visibility == View.GONE) {
                    visibility = View.VISIBLE
                    animate()
                        .translationX(0f)
                        .alpha(1.0f)
                        .setListener(null)
                } else {
                    animate()
                        .translationX(100f)
                        .alpha(0f)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                visibility = View.GONE
                            }
                        })

                }
            }
        }
    }

    private fun setUpColorButtons() {
        // Black by default
        updateColorScale(color_picker_black)

        color_picker_black.setOnClickListener {
            changeDrawColor(resources.getColor(R.color.color_draw_black, null))
            updateColorScale(it)
        }
        color_picker_white.setOnClickListener {
            changeDrawColor(resources.getColor(R.color.color_draw_white, null))
            updateColorScale(it)
        }
        color_picker_red.setOnClickListener {
            changeDrawColor(resources.getColor(R.color.color_draw_red, null))
            updateColorScale(it)
        }
        color_picker_green.setOnClickListener {
            changeDrawColor(resources.getColor(R.color.color_draw_green, null))
            updateColorScale(it)
        }
        color_picker_blue.setOnClickListener {
            changeDrawColor(resources.getColor(R.color.color_draw_blue, null))
            updateColorScale(it)
        }
        color_picker_yellow.setOnClickListener {
            changeDrawColor(resources.getColor(R.color.color_draw_yellow, null))
            updateColorScale(it)
        }
        color_picker_cyan.setOnClickListener {
            changeDrawColor(resources.getColor(R.color.color_draw_cyan, null))
            updateColorScale(it)
        }
        color_picker_magenta.setOnClickListener {
            changeDrawColor(resources.getColor(R.color.color_draw_magenta, null))
            updateColorScale(it)
        }
    }

    private fun changeDrawColor(color: Int) {
        drawView.setColor(color)
    }

    private fun updateColorScale(colorPicked: View) {
        // Reset all colors
        color_picker_black.scaleX = 1f
        color_picker_black.scaleY = 1f

        color_picker_white.scaleX = 1f
        color_picker_white.scaleY = 1f

        color_picker_red.scaleX = 1f
        color_picker_red.scaleY = 1f

        color_picker_green.scaleX = 1f
        color_picker_green.scaleY = 1f

        color_picker_blue.scaleX = 1f
        color_picker_blue.scaleY = 1f

        color_picker_yellow.scaleX = 1f
        color_picker_yellow.scaleY = 1f

        color_picker_cyan.scaleX = 1f
        color_picker_cyan.scaleY = 1f

        color_picker_magenta.scaleX = 1f
        color_picker_magenta.scaleY = 1f

        // Scale up the selected color
        colorPicked.isSelected = true
        colorPicked.animate()
            .scaleX(1.5f)
            .scaleY(1.5f)
    }
}