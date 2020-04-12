package com.log3900.settings.theme

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.log3900.MainApplication
import com.log3900.R

class ThemePickerFragment(val themeChangedCallback: () -> Unit) : DialogFragment() {
    private lateinit var themesRecyclerView: RecyclerView
    private lateinit var themesAdapter: ThemeAdapter
    private var themes = ThemeManager.getThemesAsArrayList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity)
            .setTitle(MainApplication.instance.getContext().getString(R.string.theme_selection_instruction))
            .setPositiveButton(MainApplication.instance.getContext().getString(R.string.save)) { _, _ ->
                ThemeManager.changeTheme(themesAdapter.selectedTheme).subscribe()
            }
            .setNegativeButton(MainApplication.instance.getContext().getString(R.string.cancel)) { _, _ ->

            }

        val view = activity?.layoutInflater?.inflate(R.layout.fragment_theme_picker, null)

        themesRecyclerView = view?.findViewById(R.id.fragment_theme_picker_recycler_view)!!

        setupRecyclerView()

        dialogBuilder.setView(view)
        return dialogBuilder.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_theme_picker, container, false)

        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        themeChangedCallback()
    }

    private fun setupRecyclerView() {
        themesAdapter = ThemeAdapter(themes)
        themesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this.context, 4)
            adapter = themesAdapter
        }
    }
}