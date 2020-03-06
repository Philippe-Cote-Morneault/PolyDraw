package com.log3900.settings.theme

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R

class ThemeViewHolder : RecyclerView.ViewHolder {
    private var themeImageView: ImageView
    private var rootView: LinearLayout
    lateinit var theme: Theme

    constructor(itemView: View, listener: ClickListener) : super(itemView) {
        rootView = itemView.findViewById(R.id.list_item_theme_root_view)
        themeImageView = itemView.findViewById(R.id.list_item_theme_image_view)

        themeImageView.setOnClickListener {
            listener.onThemeClicked(this)
        }
    }

    fun bind(theme: Theme) {
        this.theme = theme
        themeImageView.setImageResource(theme.themeImageID)

    }

    fun setSelected(isSelected: Boolean) {
        if (isSelected) {
            rootView.setBackgroundColor(Color.parseColor("#000000"))
        } else {
            rootView.setBackgroundColor(Color.parseColor("#00FFFFFF"))
        }
    }

    interface ClickListener {
        fun onThemeClicked(themeHolder: ThemeViewHolder)
    }
}