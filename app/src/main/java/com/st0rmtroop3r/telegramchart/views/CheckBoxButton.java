package com.st0rmtroop3r.telegramchart.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CheckBox;

import com.st0rmtroop3r.telegramchart.R;

public class CheckBoxButton extends CheckBox {

    public static final String TAG = CheckBoxButton.class.getSimpleName();

    private float textSize = 70;
    private RectF backgroundRect = new RectF();
    private Paint backgroundPaint = new Paint();
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int textColorChecked;
    private int textColorUnchecked;
    private Drawable checkDrawable;
    private String text;
    private float cornerRadius;
    private float strokeWidth = 50;
    private int viewHeight;
    private float checkDrawableSize;
    private float checkTextPadding;
    private float textXChecked;
    private float textXUnchecked;
    private float textY;

    public CheckBoxButton(Context context) {
        super(context);
        init(context);
    }

    public CheckBoxButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CheckBoxButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Resources resources = context.getResources();
        textPaint.setTextSize(textSize);
        textSize = resources.getDimension(R.dimen.checkbox_text_size);
        textPaint.setTextSize(textSize);
        viewHeight = resources.getDimensionPixelSize(R.dimen.checkbox_height);
        cornerRadius = (float) viewHeight / 2;
        checkDrawable = resources.getDrawable(R.drawable.ic_check_white_24dp);
        checkDrawableSize = resources.getDimension(R.dimen.checkbox_check_size);
        checkTextPadding = resources.getDimension(R.dimen.checkbox_text_padding_check);
        strokeWidth = resources.getDimension(R.dimen.checkbox_stroke_width);
        backgroundPaint.setStrokeWidth(strokeWidth);
        textColorUnchecked = Color.BLUE;
        textColorChecked = Color.WHITE;
        setColor(textColorUnchecked);
    }

    public void setColor(int color) {
        backgroundPaint.setColor(color);
        textPaint.setColor(color);
        textColorUnchecked = color;
    }

    public void setText(String t) {
        text = t;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (isChecked()) {
            backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, backgroundPaint);
            checkDrawable.draw(canvas);
            textPaint.setColor(textColorChecked);
            canvas.drawText(text, textXChecked, textY, textPaint);
        } else {
            backgroundPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, backgroundPaint);
            textPaint.setColor(textColorUnchecked);
            canvas.drawText(text, textXUnchecked, textY, textPaint);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        float textWidth = textPaint.measureText(text);
        int viewWidth = (int) (textWidth + checkTextPadding + checkDrawableSize + viewHeight * 0.7f);

        backgroundRect.left = strokeWidth / 2;
        backgroundRect.top = backgroundRect.left;
        backgroundRect.right = viewWidth - backgroundRect.left;
        backgroundRect.bottom = viewHeight - backgroundRect.top;
        int left = (int) ((viewWidth / 2) - (textWidth + checkTextPadding + checkDrawableSize) / 2);
        int top = (int) (viewHeight / 2 - checkDrawableSize / 2);
        checkDrawable.setBounds(left, top, (int) (left + checkDrawableSize), (int) (top + checkDrawableSize));
        textXChecked = left + checkDrawableSize + checkTextPadding;
        textXUnchecked = (viewWidth - textWidth) / 2;
        textY = (viewHeight + textSize * 0.7f) / 2;

        setMeasuredDimension(viewWidth, viewHeight);
    }
}
