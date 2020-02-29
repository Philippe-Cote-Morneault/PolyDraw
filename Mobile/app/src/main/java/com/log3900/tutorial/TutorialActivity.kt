package com.log3900.tutorial

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton
import com.log3900.R
import com.log3900.settings.theme.ThemeManager

class TutorialActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var previousButton: MaterialButton
    private lateinit var skipTutorialButton: MaterialButton
    private lateinit var nextButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        setupUI()
    }

    private fun setupUI() {
        toolbar = findViewById(R.id.activity_tutorial_toolbar)
        drawerLayout = findViewById(R.id.activity_tutorial_drawer_layoutt)
        previousButton = findViewById(R.id.activity_tutorial_footer_button_prev)
        skipTutorialButton = findViewById(R.id.activity_tutorial_footer_button_skip)
        nextButton = findViewById(R.id.activity_tutorial_footer_button_next)

        addButtonListeners()


        setupToolbar()
    }

    private fun addButtonListeners() {
        previousButton.setOnClickListener { onPreviousButtonClick() }
        skipTutorialButton.setOnClickListener { onSkipTutorialButtonClick() }
        nextButton.setOnClickListener { onNextButtonClick() }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun onPreviousButtonClick() {

    }

    private fun onSkipTutorialButtonClick() {
        finish()
    }

    private fun onNextButtonClick() {

    }
}