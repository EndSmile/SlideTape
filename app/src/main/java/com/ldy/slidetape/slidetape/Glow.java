package com.ldy.slidetape.slidetape;

import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.view.View;

/**
 * Created by ldy on 2017/7/12.
 */

public class Glow {
    private EdgeEffectCompat mLeftGlow;
    private EdgeEffectCompat mRightGlow;
    private View view;

    public Glow(View view) {
        this.view = view;
    }

    public void onDraw(Canvas canvas) {

        if (mLeftGlow != null) {
            if (!mLeftGlow.isFinished()) {

                int restoreCount = canvas.save();
                canvas.rotate(270);
                canvas.translate(-view.getHeight(), 0);
                mLeftGlow.setSize(view.getWidth(), view.getHeight());
                if (mLeftGlow.draw(canvas)) {
                    ViewCompat.postInvalidateOnAnimation(view);
                }
                canvas.restoreToCount(restoreCount);
            }
        }
        if (mRightGlow != null) {
            if (!mRightGlow.isFinished()) {

                int restoreCount = canvas.save();
                canvas.rotate(90);
                canvas.translate(0, -view.getWidth() - view.getScrollX());
                mRightGlow.setSize(view.getWidth(), view.getHeight());
                if (mRightGlow.draw(canvas)) {
                    ViewCompat.postInvalidateOnAnimation(view);
                }
                canvas.restoreToCount(restoreCount);
            }
        }
    }

    public void leftOnPull(float deltaDistance, float displacement) {
        ensureLeftGlow();
        mLeftGlow.onPull(deltaDistance, displacement);
        if (mLeftGlow != null && (!mLeftGlow.isFinished())) {
            ViewCompat.postInvalidateOnAnimation(view);
        }
    }

    public void rightOnPull(float deltaDistance, float displacement) {
        ensureRightGlow();
        mRightGlow.onPull(deltaDistance, displacement);

        if (mRightGlow != null && (!mRightGlow.isFinished())) {
            ViewCompat.postInvalidateOnAnimation(view);
        }
    }

    public void leftOnAbsorb(int velocity) {
        ensureLeftGlow();
        if (mLeftGlow.isFinished()) {
            mLeftGlow.onAbsorb(velocity);
        }
    }

    public void rightOnAbsorb(int velocity) {
        ensureRightGlow();
        if (mRightGlow.isFinished()) {
            mRightGlow.onAbsorb(velocity);
        }
    }

    public void release() {
        if (mLeftGlow != null) {
            mLeftGlow.onRelease();
        }
        if (mRightGlow != null) {
            mRightGlow.onRelease();
        }
    }

    private void ensureLeftGlow() {
        if (mLeftGlow != null) {
            return;
        }
        mLeftGlow = new EdgeEffectCompat(view.getContext());
    }

    private void ensureRightGlow() {
        if (mRightGlow != null) {
            return;
        }
        mRightGlow = new EdgeEffectCompat(view.getContext());
    }
}
