package com.example.gymbuddy_weighttracker;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Helpers {
    public static void handleAds(FrameLayout adContainer, Activity activity){
        AdView ad = new AdView(activity.getApplicationContext());
        ad.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        adContainer.addView(ad);
        loadBanner(ad, activity);
        adContainer.getLayoutParams().height=ad.getAdSize().getHeightInPixels(activity.getApplicationContext());
    }

    private static void loadBanner(AdView ad, Activity activity) {
        AdRequest adRequest = new AdRequest.Builder().build();

        AdSize adSize = getAdSize(activity);
        ad.setAdSize(adSize);

        ad.loadAd(adRequest);
    }

    private static AdSize getAdSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int divider=1;
        if(activity.getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE) divider=2;

        int adWidth = (int) (widthPixels / density)/divider;

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity.getApplicationContext(), adWidth);
    }

    public static int secsToDays(float secs) {
        return (Math.round(secs / 60 / 60 / 24));
    }

    public static boolean setLayoutAlpha(ConstraintLayout layout) {
//        isBlurred = true;
        int no = layout.getChildCount();
        View view;
        for (int i = 0; i < no; i++) {
            view = layout.getChildAt(i);
            view.setAlpha(0.3f);
        }
        return true;
    }

    public static boolean resetLayoutAlpha(ConstraintLayout layout) {
//        isBlurred = false;
        int no = layout.getChildCount();
        View view;
        for (int i = 0; i < no; i++) {
            view = layout.getChildAt(i);
            view.setAlpha(1.0f);
        }
        return false;
    }

    public static long daysToSeconds(int days) {
        return days * 24 * 60 * 60;
    }

    public static void setupActionBar(String text1, String text2, ActionBar actionBar, Activity activity) {
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(activity.getApplicationContext(),R.color.lime_500)));
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        View customActionBar = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.action_bar, null);
        actionBar.setCustomView(customActionBar, params);
        TextView abText1 = activity.findViewById(R.id.abText1);
        TextView abText2 = activity.findViewById(R.id.abText2);
        abText1.setText(text1);
        abText2.setText(text2);
    }

    public static void shake(View v) {
        ObjectAnimator
                .ofFloat(v, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
                .setDuration(200)
                .start();
    }

    public static String stringFormat(double d) {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        return df.format(d);
    }
}
