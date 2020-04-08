package com.log3900.tutorial

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
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
    private lateinit var previousButton: MaterialButton
    private lateinit var skipTutorialButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var viewPager: ViewPager
    private lateinit var toolbarSlideTitle: TextView

    private lateinit var tutorialSlidesAdapter: TutorialFragmentPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        setupUI()
    }

    fun setSlideTitle(title: Int) {
        toolbarSlideTitle.setText(title)
    }

    private fun setupUI() {
        toolbar = findViewById(R.id.activity_tutorial_toolbar)
        previousButton = findViewById(R.id.activity_tutorial_footer_button_prev)
        skipTutorialButton = findViewById(R.id.activity_tutorial_footer_button_skip)
        nextButton = findViewById(R.id.activity_tutorial_footer_button_next)
        viewPager = findViewById(R.id.activity_tutorial_view_pager)
        toolbarSlideTitle = findViewById(R.id.activity_tutorial_toolbar_fragment_title)

        togglePreviousButton(false)

        setupViewPager()

        addButtonListeners()

        TutorialManager.addSlideChangedListener { oldPos, newPos ->
            onSlideChanged(oldPos, newPos)
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun addButtonListeners() {
        previousButton.setOnClickListener { onPreviousButtonClick() }
        skipTutorialButton.setOnClickListener { onSkipTutorialButtonClick() }
        nextButton.setOnClickListener { onNextButtonClick() }
    }

    private fun setupViewPager() {
        tutorialSlidesAdapter = TutorialFragmentPagerAdapter(supportFragmentManager, TutorialManager.tutorialSlides)
        viewPager.adapter = tutorialSlidesAdapter

        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                TutorialManager.changeActiveTutorialSlide(position)
            }
        })
    }

    private fun onSlideChanged(oldPos: Int, newPos: Int) {
        if (newPos + 1 == tutorialSlidesAdapter.count) {
            setNextButtonToFinish()
            togglePreviousButton(true)
            viewPager.currentItem = newPos
        } else if (newPos == 0) {
            togglePreviousButton(false)
            setFinishButtonToNext()
            viewPager.currentItem = newPos
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
        finishTutorial()
    }

    private fun onNextButtonClick() {
        if (viewPager.currentItem < tutorialSlidesAdapter.count - 1) {
            viewPager.setCurrentItem(viewPager.currentItem + 1)
        } else {
            finishTutorial()
        }
    }

    private fun finishTutorial() {
        TutorialManager.setAccountFinishedTutorial().subscribe {
            finish()
        }
    }
}