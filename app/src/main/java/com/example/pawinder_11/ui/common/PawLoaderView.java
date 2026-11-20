package com.example.pawinder_11.ui.common;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import com.example.pawinder_11.R;

public class PawLoaderView extends AppCompatImageView {

    public PawLoaderView(Context context) {
        this(context, null);
    }

    public PawLoaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PawLoaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setImageResource(R.drawable.paw_loading);
    }

    private void updateAnimation(boolean run) {
        Drawable d = getDrawable();
        if (!(d instanceof AnimationDrawable)) return;
        AnimationDrawable anim = (AnimationDrawable) d;
        if (run && !anim.isRunning()) {
            anim.start();
        } else if (!run && anim.isRunning()) {
            anim.stop();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateAnimation(getVisibility() == VISIBLE);
    }

    @Override
    protected void onDetachedFromWindow() {
        updateAnimation(false);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        updateAnimation(visibility == VISIBLE);
    }
}