package com.log3900.settings.theme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R

class ThemeAdapter : RecyclerView.Adapter<ThemeViewHolder> {
    private lateinit var recyclerView: RecyclerView
    var themes: ArrayList<Theme>
    var selectedThemeHolder: ThemeViewHolder? = null
    var selectedTheme = ThemeManager.getCurrentTheme()

    constructor(themes: ArrayList<Theme>) {
        this.themes = themes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_theme, parent, false) as View

        return ThemeViewHolder(view, object: ThemeViewHolder.ClickListener {
            override fun onThemeClicked(themeHolder: ThemeViewHolder) {
                if (themeHolder.theme != selectedTheme) {
                    selectedThemeHolder?.setSelected(false)
                    selectedThemeHolder = themeHolder
                    selectedTheme = themeHolder.theme
                    themeHolder.setSelected(true)
                }
            }
        })
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        if (holder == selectedThemeHolder) {
            selectedThemeHolder = null
        }

        holder.bind(themes[position])
        holder.setSelected(themes[position] == selectedTheme)
        if (themes[position] == selectedTheme) {
            selectedThemeHolder = holder
        }
    }

    override fun getItemCount(): Int {
        return themes.size
    }

}