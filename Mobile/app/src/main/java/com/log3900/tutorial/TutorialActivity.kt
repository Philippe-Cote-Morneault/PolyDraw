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
import com.log3900.shared.ui.ThemeUtils

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

        togglePreviousButton(false)

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

        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                onViewPagerPageChanged(position)
            }
        })
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun onViewPagerPageChanged(position: Int) {
        if (position + 1 == tutorialSlidesAdapter.count) {
            setNextButtonToFinish()
            togglePreviousButton(true)
        } else if (position == 0) {
            togglePreviousButton(false)
            setFinishButtonToNext()
        } else {
            setFinishButtonToNext()
            togglePreviousButton(true)
        }
    }

    private fun setNextButtonToFinish() {
        nextButton.text = "Finish Tutorial"
        nextButton.setBackgroundColor(ThemeUtils.resolveAttribute(R.attr.colorPrimaryDark))
        nextButton.setTextColor(ThemeUtils.resolveAttribute(R.attr.colorOnPrimaryDark))
    }

    private fun setFinishButtonToNext() {
        nextButton.text = "Next"
        nextButton.setBackgroundColor(ThemeUtils.resolveAttribute(R.attr.colorPrimary))
        nextButton.setTextColor(ThemeUtils.resolveAttribute(R.attr.colorOnPrimary))
    }

    private fun togglePreviousButton(enable: Boolean) {
        if (enable) {
            previousButton.alpha = 1f
        } else {
            previousButton.alpha = 0.5f
        }
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
        } else {
            finish()
        }
    }
}