package com.log3900.tutorial

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.button.MaterialButton
import com.log3900.R
import com.log3900.settings.theme.ThemeManager

class TutorialActivity : AppCompatActivity() {
    // UI Elements
    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var previousButton: MaterialButton
    private lateinit var skipTutorialButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var viewPager: ViewPager

    private lateinit var tutorialSlidesAdapter: TutorialFragmentPagerAdapter
    private var tutorialManager: TutorialManager = TutorialManager()

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
        viewPager = findViewById(R.id.activity_tutorial_view_pager)

        setupViewPager()

        addButtonListeners()


        setupToolbar()
    }

    private fun addButtonListeners() {
        previousButton.setOnClickListener { onPreviousButtonClick() }
        skipTutorialButton.setOnClickListener { onSkipTutorialButtonClick() }
        nextButton.setOnClickListener { onNextButtonClick() }
    }

    private fun setupViewPager() {
        tutorialSlidesAdapter = TutorialFragmentPagerAdapter(supportFragmentManager, tutorialManager.tutorialSlides)
        viewPager.adapter = tutorialSlidesAdapter
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun onPreviousButtonClick() {
        if (viewPager.currentItem > 0) {
            viewPager.setCurrentItem(viewPager.currentItem - 1)
        }
    }

    private fun onSkipTutorialButtonClick() {
        finish()
    }

    private fun onNextButtonClick() {
        if (viewPager.currentItem < tutorialSlidesAdapter.count - 1) {
            viewPager.setCurrentItem(viewPager.currentItem + 1)
        }
    }
}