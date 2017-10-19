package com.ldy.slidetape.slidetape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

/**
 * Created by ldy on 2017/10/19.
 */

public class SlideTapeGroup extends ViewGroup {

    private SlideTapeView slideTapeView;

    private int textTopSpace, textTapeSpace, tapeBottomSpace, textSize;
    private int defaultTextTopSpace, defaultTapeBottomSpace;
    private Paint leftLinePaint;
    private TextPaint textPaint;
    private Paint indicatorPaint;

    private TapeTextCalculator tapeTextCalculator;
    private Rect textBounds;

    public SlideTapeGroup(Context context) {
        this(context, null);
    }

    public SlideTapeGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideTapeGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        defaultTextTopSpace = dip2px(28);
        defaultTapeBottomSpace = dip2px(28);
        textSize = sp2px(28);
        textTapeSpace = dip2px(28);

        slideTapeView = new SlideTapeView(context);
        slideTapeView.setCenterProgressListener(new SlideTapeView.CenterProgressListener() {

            @Override
            public void centerProgressChange(int centerProgress) {
                invalidate();
            }
        });
        slideTapeView.setBackgroundColor(0xffF6F9F6);
        addView(slideTapeView);

        leftLinePaint = new Paint();
        leftLinePaint.setStrokeWidth(dip2px(1));
        leftLinePaint.setColor(Color.BLACK);

        textPaint = new TextPaint();
        textPaint.setColor(0xff4DB977);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textBounds = new Rect();

        indicatorPaint = new Paint();
        indicatorPaint.setStrokeWidth(dip2px(3));
        indicatorPaint.setColor(0xff4DB977);

    }

    public void setMaxProgress(int progress) {
        slideTapeView.setMaxProgress(progress);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int minHeight = textSize + textTapeSpace + slideTapeView.getMeasuredHeight();
        if (heightMode == MeasureSpec.EXACTLY) {
            if (sizeHeight <= minHeight) {
                textTopSpace = 0;
                tapeBottomSpace = 0;
            } else {
                textTopSpace = tapeBottomSpace = (sizeHeight - minHeight) / 2;
            }
        } else {
            textTopSpace = defaultTextTopSpace;
            tapeBottomSpace = defaultTapeBottomSpace;
        }
        int height = textTopSpace + minHeight + tapeBottomSpace;

        setMeasuredDimension((widthMode == MeasureSpec.EXACTLY) ? sizeWidth : slideTapeView.getMeasuredWidth(), height);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int tapeTop = textTopSpace + textTapeSpace + textSize;
        slideTapeView.layout(0, tapeTop, getWidth(), tapeTop + slideTapeView.getMeasuredHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        canvas.drawLine(0, 0, 0, getHeight(), leftLinePaint);

        String progress;
        if (tapeTextCalculator != null) {
            progress = tapeTextCalculator.calculateProgressText(slideTapeView.getCenterProgress());
        } else {
            progress = String.valueOf(slideTapeView.getCenterProgress());
        }

        textPaint.getTextBounds(progress, 0, progress.length(), textBounds);
        int height = textBounds.height();
        int textTop = textTopSpace - textPaint.getFontMetricsInt().top;
        if (height < textSize) {
            textTop -= (textSize - height) / 2;
        }

        canvas.drawText(progress, getWidth() / 2, textTop, textPaint);

        float indicatorLeftMargin = (getWidth()) / 2;
        int top = slideTapeView.getTop();
        canvas.drawLine(indicatorLeftMargin, top, indicatorLeftMargin, top + slideTapeView.getHeight() / 2, indicatorPaint);

    }

    public void setTapeTextCalculator(TapeTextCalculator tapeTextCalculator) {
        this.tapeTextCalculator = tapeTextCalculator;
        slideTapeView.setTapeTextCalculator(tapeTextCalculator);
    }

    private int dip2px(float dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
