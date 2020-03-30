package com.log3900.game.match

import android.animation.*
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.log3900.R
import com.log3900.draw.DrawViewFragment
import com.log3900.game.group.Player
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.graphics.Color
import android.util.Log
import android.view.animation.Animation
import androidx.navigation.fragment.findNavController
import com.log3900.game.match.UI.*
import com.log3900.shared.ui.dialogs.SimpleConfirmationDialog


abstract class ActiveMatchFragment : Fragment(), ActiveMatchView {
    protected var activeMatchPresenter: ActiveMatchPresenter? = null
    private lateinit var drawFragment: DrawViewFragment

    // UI
    protected lateinit var footer: LinearLayout
    protected lateinit var toolbar: ConstraintLayout
    protected lateinit var remainingTimeTextView: TextView
    protected lateinit var remainingTimeChangeTextView: TextView
    protected var guessingView: WordGuessingView? = null
    private var wordToDrawView: WordToDrawView? = null
    private lateinit var roundEndInfoView: RoundEndInfoView
    private lateinit var matchEndInfoView: FFAMatchEndInfoView
    private lateinit var canvasMessageView: CanvasMessageView

    protected open fun setupUI(rootView: View) {
        footer = rootView.findViewById(R.id.fragment_active_match_footer_container)
        drawFragment = childFragmentManager.findFragmentById(R.id.fragment_active_match_draw_container) as DrawViewFragment
        toolbar = activity?.findViewById(R.id.toolbar_active_match_outer_container)!!

        setupHumanPlayerRecyclerView(rootView)
        setupToolbar(rootView)

        roundEndInfoView = rootView.findViewById(R.id.fragment_active_match_round_end_info_view)
        matchEndInfoView = rootView.findViewById(R.id.fragment_active_match_ffa_match_end_info_view)
        canvasMessageView = rootView.findViewById(R.id.fragment_active_match_canvas_message_view)
    }

    protected open fun setupToolbar(rootView: View) {
        remainingTimeTextView = toolbar.findViewById(R.id.toolbar_active_match_text_view_remaining_time)
        remainingTimeChangeTextView = toolbar.findViewById(R.id.toolbar_active_match_text_view_remaining_time_changed)
    }

    abstract protected open fun setupHumanPlayerRecyclerView(rootView: View)

    override fun setWordToGuessLength(length: Int) {
        guessingView?.setWordLength(length)
    }

    override fun setWordToDraw(word: String) {
        wordToDrawView?.setWordToGuess(word)
    }

    override fun enableDrawFunctions(enable: Boolean, drawingID: UUID?) {
        drawFragment.enableDrawFunctions(enable, drawingID)
    }

    override fun setTimeValue(time: String) {
        remainingTimeTextView.text = time
    }

    override fun clearCanvas() {
        drawFragment.clearCanvas()
    }

    override fun showWordGuessingView() {
        if (wordToDrawView != null) {
            footer.removeAllViews()
            wordToDrawView = null
        }

        if (guessingView == null) {
            guessingView = WordGuessingView(context!!)
            guessingView?.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            footer.addView(guessingView)
            guessingView?.listener = object : WordGuessingView.Listener {
                override fun onGuessPressed(text: String) {
                    activeMatchPresenter?.guessPressed(text)
                }

                override fun onHintPressed() {
                    activeMatchPresenter?.hintPressed()
                }
            }
        }
    }

    override fun showWordToDrawView() {
        if (guessingView != null) {
            footer.removeAllViews()
            guessingView = null
        }

        if (wordToDrawView == null) {
            wordToDrawView = WordToDrawView(context!!)
            wordToDrawView?.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            footer.addView(wordToDrawView)
        }
    }

    override fun hideCanvas() {
        drawFragment.view?.clearAnimation()
        drawFragment.view?.animation?.cancel()
        drawFragment.view?.animate()?.cancel()

        drawFragment.view!!.translationX = 0f
        drawFragment.view!!.translationY = 0f
        drawFragment.view!!.rotation = 0f
        drawFragment.view!!.rotationX = 0f
        drawFragment.view!!.rotationY = 0f
        drawFragment.view!!.pivotX = 0f
        drawFragment.view!!.pivotY = 0f

        val anim = ObjectAnimator.ofPropertyValuesHolder(
            drawFragment.view,
            PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f),
            PropertyValuesHolder.ofFloat(View.ROTATION, 0f, 90f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f, 2000f)
        )

        anim.duration = 500
        anim.start()
    }

    override fun showCanvas() {
        drawFragment.view?.clearAnimation()
        drawFragment.view?.animation?.cancel()
        drawFragment.view?.animate()?.cancel()
        if (drawFragment.view!!.alpha == 1f) {
            return
        }

        drawFragment.view!!.translationX = 0f
        drawFragment.view!!.translationY = 0f
        drawFragment.view!!.rotation = 0f
        drawFragment.view!!.rotationX = 0f
        drawFragment.view!!.rotationY = 0f
        drawFragment.view!!.pivotX = 0f
        drawFragment.view!!.pivotY = 0f

        val anim = ObjectAnimator.ofPropertyValuesHolder(
            drawFragment.view,
            PropertyValuesHolder.ofFloat(View.ROTATION, 180f, 0f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, -drawFragment.view!!.width.toFloat(), 0f),
            PropertyValuesHolder.ofFloat(View.ALPHA,0f, 1f)
        )
        anim.duration = 2000

        anim.start()
    }

    override fun showConfetti() {
        drawFragment.showConfetti()
    }

    override fun pulseRemainingTime() {
        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            remainingTimeTextView,
            PropertyValuesHolder.ofFloat("scaleX", 1.4f),
            PropertyValuesHolder.ofFloat("scaleY", 1.4f)
        )
        scaleDown.duration = 500

        scaleDown.repeatCount = 1
        scaleDown.repeatMode = ObjectAnimator.REVERSE

        val anim = ValueAnimator()
        anim.setIntValues(Color.BLACK, Color.RED)
        anim.setEvaluator(ArgbEvaluator())
        anim.addUpdateListener { valueAnimator -> remainingTimeTextView.setTextColor(valueAnimator.animatedValue as Int) }
        for (drawable in remainingTimeTextView.compoundDrawables) {
            if (drawable != null) {
                anim.addUpdateListener { valueAnimator -> drawable.setTint(valueAnimator.animatedValue as Int) }
            }
        }

        anim.duration = 500

        anim.repeatCount = 1
        anim.repeatMode = ObjectAnimator.REVERSE
        anim.start()

        scaleDown.start()
    }

    override fun animateWordGuessedWrong() {
        val animator1 = ObjectAnimator.ofFloat(guessingView!!, "translationX", -10f)
        animator1.duration = 100
        animator1.repeatCount = 1
        animator1.repeatMode = ObjectAnimator.REVERSE

        val animator2 = ObjectAnimator.ofFloat(guessingView!!, "translationX", 10f)
        animator2.duration = 100
        animator2.repeatCount = 1
        animator2.repeatMode = ObjectAnimator.REVERSE

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(animator1, animator2)

        val anim = ValueAnimator()
        anim.setIntValues(Color.BLACK, Color.RED)
        anim.setEvaluator(ArgbEvaluator())
        anim.addUpdateListener { valueAnimator -> guessingView?.setWordGuessTextColor(valueAnimator.animatedValue as Int) }

        anim.duration = 200

        anim.repeatCount = 1
        anim.repeatMode = ObjectAnimator.REVERSE
        animatorSet.start()
        anim.start()
    }

    override fun animateWordGuessedRight() {
        guessingView?.getEditTexts()?.forEachIndexed { index, editText ->
            val scaleUpAnimator = ObjectAnimator.ofPropertyValuesHolder(
                editText,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.5f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.5f)
            )
            scaleUpAnimator.duration = 200
            scaleUpAnimator.repeatCount = 1
            scaleUpAnimator.repeatMode = ObjectAnimator.REVERSE
            scaleUpAnimator.startDelay = (index * 200).toLong()

            val colorChangeAnimator = ValueAnimator()
            colorChangeAnimator.setIntValues(Color.BLACK, Color.parseColor("#FF008000"))
            colorChangeAnimator.setEvaluator(ArgbEvaluator())
            colorChangeAnimator.addUpdateListener { valueAnimator -> editText.setTextColor(valueAnimator.animatedValue as Int) }
            colorChangeAnimator.duration = 200
            colorChangeAnimator.repeatCount = 1
            colorChangeAnimator.repeatMode = ObjectAnimator.REVERSE
            colorChangeAnimator.startDelay = (index * 200).toLong()
            colorChangeAnimator.start()
            scaleUpAnimator.start()
        }
    }

    override fun showRoundEndInfoView(word: String, players: ArrayList<Pair<String, Int>>) {
        roundEndInfoView.setWord(word)
        roundEndInfoView.setPlayers(players)
        roundEndInfoView.visibility = View.VISIBLE
    }

    override fun hideRoundEndInfoView() {
        roundEndInfoView.visibility = View.INVISIBLE
    }

    override fun showMatchEndInfoView(winnerName: String, players: ArrayList<Pair<String, Int>>) {
        matchEndInfoView.setWinner(winnerName)
        matchEndInfoView.setPlayers(players)
        matchEndInfoView.visibility = View.VISIBLE
    }

    override fun hideMatchEndInfoView() {
        matchEndInfoView.visibility = View.INVISIBLE
    }

    override fun enableHintButton(enable: Boolean) {
        guessingView?.enableHintButton(enable)
    }

    override fun showRemainingTimeChangedAnimation(timeChangedValue: String, isPositive: Boolean) {
        if (isPositive) {
            remainingTimeChangeTextView.setTextColor(Color.GREEN)
        } else {
            remainingTimeChangeTextView.setTextColor(Color.RED)
        }

        remainingTimeChangeTextView.text = timeChangedValue
        remainingTimeChangeTextView.bringToFront()

        val scaleUpAnimator = ObjectAnimator.ofPropertyValuesHolder(
            remainingTimeChangeTextView,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 2f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 2f)
        )
        scaleUpAnimator.duration = 2000

        val alphaChangeAnimator = ObjectAnimator.ofPropertyValuesHolder(
            remainingTimeChangeTextView,
            PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
        )
        alphaChangeAnimator.repeatCount = 1
        alphaChangeAnimator.repeatMode = ObjectAnimator.REVERSE
        alphaChangeAnimator.duration = 1000
        alphaChangeAnimator.start()
        scaleUpAnimator.start()
    }

    override fun setCanvasMessage(message: String) {
        canvasMessageView.setMessage(message)
    }

    override fun showCanvasMessageView(show: Boolean) {
        if (show) {
            canvasMessageView.visibility = View.VISIBLE
        } else {
            canvasMessageView.visibility = View.INVISIBLE
        }
    }

    override fun onBackButtonPressed() {
        SimpleConfirmationDialog(
            context!!,
            getString(R.string.quit_match),
            getString(R.string.quit_match_confirm),
            {_, _ -> findNavController().popBackStack()},
            null
        ).show()
    }

    override fun onDestroy() {
        activeMatchPresenter?.destroy()
        activeMatchPresenter = null
        super.onDestroy()
    }
}