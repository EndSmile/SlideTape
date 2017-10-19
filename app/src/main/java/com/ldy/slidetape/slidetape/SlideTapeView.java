package com.ldy.slidetape.slidetape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.OverScroller;

/**
 * Created by ldy on 2017/10/16.
 * 滑动卷尺
 */

public class SlideTapeView extends View {
    /**
     * Sentinel value for no current active pointer.
     * Used by {@link #activePointerId}.
     */
    private static final int INVALID_POINTER = -1;
    private static final String TAG = "SlideTapeView";
    private Paint shortLinePaint;
    private int mTouchSlop;
    private VelocityTracker velocityTracker;
    private int maxFlingVelocity;

    private int lineSpace;
    private int shortLineHeight;
    private int longLineHeight;
    private int lineTextSpace;
    private int textBottomSpace;
    private int textSize;
    private int longLineInterval = 10;

    private Paint textPaint;
    private OverScroller scroller;
    private Paint longLinePaint;
    private Glow glow;

    private int[] lineXArray;

    private int lastPointX;
    private int centerX;
    private int activePointerId = INVALID_POINTER;
    private boolean glowing;
    private boolean mIsBeingDragged;
    private int lastMotionX;

    private int centerProgress;
    private CenterProgressListener centerProgressListener;

    private TapeTextCalculator tapeTextCalculator;
    private Rect textBounds;

    public SlideTapeView(Context context) {
        this(context, null);
    }

    public SlideTapeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideTapeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttr();

        initPaint();

        initExtra(context);

    }


    public void setMaxProgress(int maxProgress) {
        if (maxProgress < 0) {
            return;
        }

        lineXArray = new int[maxProgress + 1];
        for (int i = 0; i < lineXArray.length; i++) {
            lineXArray[i] = i * lineSpace;
        }

        if (maxProgress < centerProgress) {
            smoothScrollTo(maxProgress * lineSpace);
        }
    }

    private void initAttr() {

        lineSpace = dip2px(14);
        shortLineHeight = dip2px(24);
        longLineHeight = dip2px(48);
        lineTextSpace = dip2px(18);
        textBottomSpace = dip2px(18);
        textSize = sp2px(18);
    }

    private void initPaint() {
        textPaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setColor(0xff393A39);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        longLinePaint = new Paint();
        longLinePaint.setColor(0xffDFE3DF);
        longLinePaint.setStrokeWidth(dip2px(2));
        shortLinePaint = new Paint();
        shortLinePaint.setColor(0xffE6E9E6);
        shortLinePaint.setStrokeWidth(dip2px(1));
    }

    private void initExtra(Context context) {
        scroller = new OverScroller(context);

        final ViewConfiguration vc = ViewConfiguration.get(context);
        maxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mTouchSlop = vc.getScaledTouchSlop();

        glow = new Glow(this);

        textBounds = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(sizeWidth, (heightMode == MeasureSpec.EXACTLY) ? sizeHeight
                : (int) (longLineHeight + lineTextSpace + textPaint.getTextSize() + textBottomSpace));

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw) {
            centerX = (int) (getX() + getWidth() / 2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lineXArray == null) {
            return;
        }

        int restoreCount = canvas.save();
        canvas.clipRect(getScrollX(),0 , getWidth() + getScrollX(), getHeight());
        for (int i = 0; i < lineXArray.length; i++) {
            int startX = lineXArray[i] + centerX;
            if (i % longLineInterval == 0) {
                canvas.drawLine(startX, 0, startX, longLineHeight, longLinePaint);

                String text;
                if (tapeTextCalculator != null) {
                    text = tapeTextCalculator.calculateLongLineText(i);
                } else {
                    text = String.valueOf(i);
                }

                textPaint.getTextBounds(text, 0, text.length(), textBounds);
                int height = textBounds.height();
                int textTop = longLineHeight + lineTextSpace - textPaint.getFontMetricsInt().top;
                if (height < textSize) {
                    textTop -= (textSize - height) / 2;
                }
                canvas.drawText(text, startX, textTop, textPaint);
            } else {
                canvas.drawLine(startX, 0, startX, shortLineHeight, shortLinePaint);
            }
        }

        glow.onDraw(canvas);
        canvas.restoreToCount(restoreCount);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                lastPointX = (int) event.getX();
                activePointerId = event.getPointerId(0);

                lastMotionX = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (activePointerId == INVALID_POINTER) {
                    break;
                }

                final int activePointerIndex = event.findPointerIndex(activePointerId);
                if (activePointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + activePointerId + " in onTouchEvent");
                    break;
                }

                final int x = (int) event.getX(activePointerIndex);
                int deltaX = lastMotionX - x;
                if (!mIsBeingDragged && Math.abs(deltaX) > mTouchSlop) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    mIsBeingDragged = true;
                    if (deltaX > 0) {
                        deltaX -= mTouchSlop;
                    } else {
                        deltaX += mTouchSlop;
                    }
                }

                if (mIsBeingDragged) {

                    int finalX = scroller.getFinalX();
                    if (finalX > getMaxX()) {
                        finalX = getMaxX();
                    } else if (finalX < 0) {
                        finalX = 0;
                    }
                    int targetX = finalX + deltaX;
                    float moveX = event.getX() - lastPointX;

                    lastMotionX = x;

                    if (targetX > getMaxX()) {
                        glow.rightOnPull(Math.abs(moveX) / getWidth(), 1 - event.getY() / getHeight());
                        glowing = true;
                    } else if (targetX < 0) {
                        glow.leftOnPull(Math.abs(moveX) / getWidth(), event.getY() / getHeight());
                        glowing = true;
                    } else {
                        smoothScrollBy(deltaX);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                if (mIsBeingDragged) {
                    mIsBeingDragged = false;

                    velocityTracker.computeCurrentVelocity(1000, maxFlingVelocity);
                    float velocityX = -velocityTracker.getXVelocity(activePointerId);

                    scroller.fling(scroller.getFinalX(), 0, (int) velocityX, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                    invalidate();
                    activePointerId = INVALID_POINTER;

                    recycleVelocityTracker();

                    if (glowing) {
                        glowing = false;
                        glow.release();
                    }
                }

                break;

        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {

            int currX = scroller.getCurrX();
            int maxX = getMaxX();
            if (currX > maxX) {
                glow.rightOnAbsorb((int) scroller.getCurrVelocity());
                smoothScrollTo(maxX);
                if (lineXArray != null && lineXArray.length != 0) {
                    centerProgressChange(lineXArray.length - 1);
                }
            } else if (currX < 0) {
                glow.leftOnAbsorb((int) scroller.getCurrVelocity());
                smoothScrollTo(0);
                centerProgressChange(0);
            } else {
                scrollTo(currX, scroller.getCurrY());
                postInvalidate();
                centerProgressChange(Math.round(((float) currX) / lineSpace));
            }

        } else {
            int extra = scroller.getCurrX() % lineSpace;
            if (extra != 0) {
                if (extra > ((float) lineSpace) / 2) {
                    smoothScrollBy(lineSpace - extra);
                    centerProgressChange(scroller.getCurrX() / lineSpace + 1);
                } else {
                    smoothScrollBy(-extra);
                    centerProgressChange(scroller.getCurrX() / lineSpace);
                }
            }
        }
    }

    private int getMaxX() {
        if (lineXArray == null || lineXArray.length == 0) {
            return 0;
        }
        return lineXArray[lineXArray.length - 1];
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void centerProgressChange(int centerProgress) {
        if (lineXArray == null) {
            return;
        }

        if (centerProgress < 0 || centerProgress >= lineXArray.length) {
            return;
        }

        if (centerProgressListener != null && centerProgress != this.centerProgress) {
            this.centerProgress = centerProgress;
            centerProgressListener.centerProgressChange(centerProgress);
        }
    }

    private void smoothScrollTo(int fx) {
        int dx = fx - scroller.getFinalX();
        smoothScrollBy(dx);
    }

    private void smoothScrollBy(int dx) {
        scroller.startScroll(scroller.getFinalX(), 0, dx, 0);
        invalidate();
    }

    private int dip2px(float dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public void setCenterProgressListener(CenterProgressListener centerProgressListener) {
        this.centerProgressListener = centerProgressListener;
    }

    public int getCenterProgress() {
        return centerProgress;
    }

    public void setTapeTextCalculator(TapeTextCalculator tapeTextCalculator) {
        this.tapeTextCalculator = tapeTextCalculator;
    }


    public interface CenterProgressListener {
        void centerProgressChange(int centerProgress);
    }
}
