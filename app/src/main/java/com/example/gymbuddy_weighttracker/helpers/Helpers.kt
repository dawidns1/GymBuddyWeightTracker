package com.example.gymbuddy_weighttracker.helpers

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.gymbuddy_weighttracker.R
import com.github.mikephil.charting.data.LineData
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.roundToInt

object Helpers {
    const val kgToLbsMultiplier = 2.2F
    const val cmToInMultiplier = 0.393701F
    const val AD_ID_MAIN_NATIVE = "ca-app-pub-3836143618707347/5975953396"
    private var savedAdRequest: AdRequest? = null
    const val SWIPE_THRESHOLD = 100
    const val SWIPE_VELOCITY_THRESHOLD = 100
    const val WEIGHT_FRAGMENT_TAG = "WeightFragment"

    fun View.toggleVisibility(boolean: Boolean) {
        if (boolean) {
            if (this.visibility == View.GONE) this.visibility = View.VISIBLE
        } else
            if (this.visibility == View.VISIBLE) this.visibility = View.GONE
    }

    fun ExtendedFloatingActionButton.toggleVisibilityEFAB(boolean: Boolean) {
        if (boolean) {
            if (!this.isShown) this.show()
        } else
            if (this.isShown) this.hide()
    }

    fun View.animateAlpha(alpha: Float, animationDuration: Long = 200) {
        this.animate().apply {
            alpha(alpha)
            duration = animationDuration
            start()
        }
    }

    fun handleNativeAds(template: TemplateView, activity: Activity, adUnitId: String, adLoader: AdLoader?): AdLoader {
        var currentAdLoader = adLoader
        if (currentAdLoader == null) {
            currentAdLoader = AdLoader.Builder(activity.applicationContext, adUnitId)
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        template.visibility = View.GONE
                        super.onAdFailedToLoad(loadAdError)
                    }

                    override fun onAdLoaded() {
                        template.mockLayout!!.visibility = View.GONE
                        template.realLayout!!.visibility = View.VISIBLE
                        super.onAdLoaded()
                    }
                })
                .forNativeAd { nativeAd: NativeAd ->
                    val styles = NativeTemplateStyle.Builder().withMainBackgroundColor(ColorDrawable(ContextCompat.getColor(activity.applicationContext, R.color.grey_900))).build()
                    template.setStyles(styles)
                    template.setNativeAd(nativeAd)
                    if (activity.isDestroyed) {
                        nativeAd.destroy()
                    }
                }
                .build()
        }
        val lastAdShown = Utils.getInstance(activity.applicationContext).lastAdShown
        val currentTime = System.currentTimeMillis()
        if (lastAdShown == 0L || (currentTime - lastAdShown) / 1000 > 60 || savedAdRequest == null) {
            savedAdRequest = AdRequest.Builder().build()
            Utils.getInstance(activity.applicationContext).lastAdShown = currentTime
        }
        currentAdLoader?.loadAd(savedAdRequest!!)
        return currentAdLoader!!
    }

    fun showRatingUserInterface(activity: Activity) {
        val lastAppRating = Utils.getInstance(activity.applicationContext).lastAppRating
        var days = 0
        if (lastAppRating != 0L) {
            days = millisToDays(System.currentTimeMillis() - lastAppRating)
        }
        if (days > 30) {
            Utils.getInstance(activity.applicationContext).setLastAppRating(System.currentTimeMillis())
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task: Task<ReviewInfo?> ->
                try {
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        val flow = manager.launchReviewFlow(activity, reviewInfo)
                        flow.addOnCompleteListener { }
                    }
                } catch (ex: Exception) {
                }
            }
        }
    }

    fun secsToDays(secs: Float): Int {
        return (secs / 60 / 60 / 24).roundToInt()
    }

    private fun secsToDaysFloat(secs: Float): Float {
        return (secs / 60 / 60 / 24)
    }

    fun Float.toPrettyString(suffix: String? = "") = "${Math.round(this * 10.0) / 10.0}${suffix}"

    fun Float.toPrettyStringDecimals(suffix: String? = "") = run {
        val string = "${Math.round(this * 100.0) / 100.0}${suffix}"
        string.dropWhile { it.isDigit() }
    }

    fun TextView.blank() {
        this.text = "---"
    }

    fun setLayoutAlpha(layout: ConstraintLayout): Boolean {
        val no = layout.childCount
        var view: View
        for (i in 0 until no) {
            view = layout.getChildAt(i)
            view.alpha = 0.3f
        }
        return true
    }

    fun resetLayoutAlpha(layout: ConstraintLayout): Boolean {
        val no = layout.childCount
        var view: View
        for (i in 0 until no) {
            view = layout.getChildAt(i)
            view.alpha = 1.0f
        }
        return false
    }

    fun daysToSeconds(days: Int): Long {
        return (days * 24 * 60 * 60).toLong()
    }

    fun setupActionBar(text1: String?, text2: String?, actionBar: ActionBar?, activity: Activity) {
        actionBar?.let {
            val params = ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)
            val customActionBar = LayoutInflater.from(activity.applicationContext).inflate(R.layout.action_bar, null)
            it.apply {
                setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(activity.applicationContext, R.color.grey_900)))
                setDisplayShowTitleEnabled(false)
                setDisplayUseLogoEnabled(false)
                setDisplayHomeAsUpEnabled(false)
                setDisplayShowCustomEnabled(true)
                setDisplayShowHomeEnabled(false)
                setCustomView(customActionBar, params)
            }
            val abText1 = activity.findViewById<TextView>(R.id.abText1)
            abText1.setTextColor(ContextCompat.getColor(activity.applicationContext, R.color.lime_500))
            val abText2 = activity.findViewById<TextView>(R.id.abText2)
            abText2.setTextColor(ContextCompat.getColor(activity.applicationContext, R.color.lime_500))
            abText1.text = text1
            abText2.text = text2
        }

    }

    fun shake(v: View?) {
        ObjectAnimator
            .ofFloat(v, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
            .setDuration(200)
            .start()
    }

    fun stringFormat(d: Double): String {
        val df = DecimalFormat("#.#")
        df.decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH)
        return df.format(d)
    }

    private fun millisToDays(millis: Long): Int {
        return (millis / 1000 / 60 / 60 / 24).toInt()
    }

    fun dataToArrayOfCoordinates(data: LineData, coordinate: String = "x", count: Int = 5): ArrayList<Float> {
        val dataSet = data.getDataSetByIndex(0)
        val arrayOfCoordinates: ArrayList<Float> = ArrayList()
        val size = dataSet.entryCount

        for (i in 0 until count) {
            if (coordinate == "x")
                arrayOfCoordinates.add(secsToDaysFloat(dataSet.getEntryForIndex(size - 1 - i).x))
            else
                arrayOfCoordinates.add(dataSet.getEntryForIndex(size - 1 - i).y)
        }
        return arrayOfCoordinates
    }

    fun regressionSlope(xs: ArrayList<Float>, ys: ArrayList<Float>): Float? {
        return if (xs.size != ys.size || xs.isEmpty() || ys.isEmpty()) {
            null
        } else {
            val xAverage = xs.average().toFloat()
            val yAverage = ys.average().toFloat()
            var numerator = 0F
            var denominator = 0F
            for (i in xs.indices) {
                numerator += (xs[i] - xAverage) * (ys[i] - yAverage)
                denominator += (xs[i] - xAverage).pow(2)
            }
            numerator / denominator * 7
        }
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus
        if (view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun hideKeyboardForced(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    fun showKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
}