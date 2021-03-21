package com.abc.qwert.slovicky;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

import org.jetbrains.annotations.NotNull;

public class CardsPagerTransformerBasic implements ViewPager.PageTransformer {

    private final float distance;

    CardsPagerTransformerBasic(float distance) {
        this.distance = distance;
    }

    @Override
    public void transformPage(@NotNull View page, float position) {

        if (position >= 0) {
            page.setScaleX(0.8f - 0.05f * position);
            page.setScaleY(0.8f - 0.05f * position);
            page.setTranslationX(-page.getWidth() * position + distance * position);

            page.findViewById(R.id.transparency).setAlpha((position * 0.33f));
            page.setAlpha(1 - (position * 0.33f));
        } else {
            page.setScaleX(0.8f);
            page.setScaleY(0.8f);
        }

    }

}
