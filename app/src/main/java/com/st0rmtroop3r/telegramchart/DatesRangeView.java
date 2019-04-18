package com.st0rmtroop3r.telegramchart;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.st0rmtroop3r.telegramchart.views.DatesRange;

import java.util.Date;

import androidx.annotation.Nullable;

public class DatesRangeView extends View {

    private static final String TAG = DatesRangeView.class.getSimpleName();
    private DatesRange datesRange = new DatesRange();
    private ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    private Date dateLeft = new Date();
    private Date dateRight = new Date();
    private Date pendingDateLeft = new Date();
    private Date pendingDateRight = new Date();
    private Scroll scrollLeftDate = Scroll.None;
    private Scroll scrollRightDate = Scroll.None;
    private boolean scrollLeftDay;
    private boolean scrollRightDay;
    private boolean scrollLeftMonth;
    private boolean scrollRightMonth;
    private boolean scrollLeftYear;
    private boolean scrollRightYear;
    private String[] monthsText = new String[12];
    private float[] monthsWidth = new float[12];
    private float textSize;
    private float baseLine;
    private float oneDigitsWidth;
    private float twoDigitsWidth;
    private float fourDigitsWidth;
    private float leftMonthX;
    private float rightDayX;
    private float rightMonthX;
    private float maxMonthTextWidth;
    private boolean pendingRange = false;
    private Handler handler = new Handler();
    private Runnable startPendingAnimation = () -> changeRange(pendingDateLeft, pendingDateRight);

    public DatesRangeView(Context context) {
        super(context);
        init(context);
    }

    public DatesRangeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DatesRangeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        monthsText[0] = "Jan";
        monthsText[1] = "Feb";
        monthsText[2] = "Mar";
        monthsText[3] = "Apr";
        monthsText[4] = "May";
        monthsText[5] = "Jun";
        monthsText[6] = "Jul";
        monthsText[7] = "Aug";
        monthsText[8] = "Sep";
        monthsText[9] = "Oct";
        monthsText[10] = "Nov";
        monthsText[11] = "Dec";
        textSize = context.getResources().getDimension(R.dimen.dates_range_text_size);
        datesRange.setTextSize(textSize);
        datesRange.dash.text = " - ";
        Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        for (DatesRange.Text text : datesRange.texts) {
            text.paint.setTypeface(typeface);
        }

        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();

            if (scrollLeftDay) scrollText(scrollLeftDate, datesRange.leftDayOut, datesRange.leftDayIn, value);
            if (scrollLeftMonth) scrollText(scrollLeftDate, datesRange.leftMonthOut, datesRange.leftMonthIn, value);
            if (scrollLeftYear) scrollText(scrollLeftDate, datesRange.leftYearOut, datesRange.leftYearIn, value);

            if (scrollRightDay) scrollText(scrollRightDate, datesRange.rightDayOut, datesRange.rightDayIn, value);
            if (scrollRightMonth) scrollText(scrollRightDate, datesRange.rightMonthOut, datesRange.rightMonthIn, value);
            if (scrollRightYear) scrollText(scrollRightDate, datesRange.rightYearOut, datesRange.rightYearIn, value);

            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (pendingRange) {
                    pendingRange = false;
                    handler.post(startPendingAnimation);
                }
            }
        });

        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(160);
    }

    private void scrollText(Scroll direction, DatesRange.Text textOut, DatesRange.Text textIn, float factor) {
        if (direction == Scroll.None) return;
        textOut.paint.setAlpha((int) (255 - 255 * factor));
        textIn.paint.setAlpha((int) (255 * factor));
        textOut.paint.setTextSize(textSize - textSize * .5f * factor);
        textIn.paint.setTextSize(textSize * .5f + textSize * .5f * factor);
        switch (direction) {
            case Up:
                textOut.y = baseLine - textSize * factor;
                textIn.y = baseLine + textSize - textSize * factor;
                break;
            case Down:
                textOut.y = baseLine + textSize * factor;
                textIn.y = baseLine - textSize + textSize * factor;
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Paint paint = datesRange.leftDayOut.paint;
        oneDigitsWidth = paint.measureText("8");
        twoDigitsWidth = paint.measureText("88");
        fourDigitsWidth = paint.measureText("8888");
        float spaceTextLength = paint.measureText(" ");
        float dashTextWidth = paint.measureText(" - ");
        maxMonthTextWidth = 0;
        for (int i = 0, monthsTextLength = monthsText.length; i < monthsTextLength; i++) {
            String aMonthsText = monthsText[i];
            float monthTextWidth = paint.measureText(aMonthsText);
            if (monthTextWidth > maxMonthTextWidth) maxMonthTextWidth = monthTextWidth;
            monthsWidth[i] = monthTextWidth;
        }
        baseLine = textSize * 2f;
        float textX = 0;
        datesRange.leftDayOut.x = textX;
        datesRange.leftDayIn.x = textX;
        textX += twoDigitsWidth + spaceTextLength;
        leftMonthX = textX;
        datesRange.leftMonthOut.x = textX;
        datesRange.leftMonthIn.x = textX;
        textX += maxMonthTextWidth + spaceTextLength;
        datesRange.leftYearOut.x = textX;
        datesRange.leftYearIn.x = textX;
        textX += fourDigitsWidth;
        datesRange.dash.x = textX;
        textX += dashTextWidth;
        rightDayX = textX;
        datesRange.rightDayOut.x = textX;
        datesRange.rightDayIn.x = textX;
        textX += twoDigitsWidth + spaceTextLength;
        rightMonthX = textX;
        datesRange.rightMonthOut.x = textX;
        datesRange.rightMonthIn.x = textX;
        textX += maxMonthTextWidth + spaceTextLength;
        datesRange.rightYearOut.x = textX;
        datesRange.rightYearIn.x = textX;
        for (DatesRange.Text text : datesRange.texts) {
            text.y = baseLine;
        }
        setMeasuredDimension((int)(datesRange.rightYearOut.x + fourDigitsWidth), (int)(textSize * 3));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        datesRange.draw(canvas);
    }

    public void setRange(Date left, Date right) {
        dateLeft = left;
        dateRight = right;

        datesRange.leftDayIn.text = String.valueOf(left.getDate());
        datesRange.leftDayIn.x = left.getDate() < 10 ? oneDigitsWidth : 0;
        datesRange.leftDayOut.text = "";
        datesRange.leftMonthIn.text = monthsText[left.getMonth()];
        datesRange.leftMonthIn.x = leftMonthX + (maxMonthTextWidth - monthsWidth[left.getMonth()]) / 2;
        datesRange.leftMonthOut.text = "";
        datesRange.leftYearIn.text = String.valueOf(left.getYear() + 1900);
        datesRange.leftYearOut.text = "";

        datesRange.rightDayIn.text = String.valueOf(right.getDate());
        datesRange.rightDayIn.x = right.getDate() < 10 ? rightDayX + oneDigitsWidth : rightDayX;
        datesRange.rightDayOut.text = "";
        datesRange.rightMonthIn.text = monthsText[right.getMonth()];
        datesRange.rightMonthIn.x = rightMonthX + (maxMonthTextWidth - monthsWidth[right.getMonth()]) / 2;
        datesRange.rightMonthOut.text = "";
        datesRange.rightYearIn.text = String.valueOf(right.getYear() + 1900);
        datesRange.rightYearOut.text = "";
    }

    public void changeRange(Date newLeftDate, Date newRightDate) {
        if (animator.isStarted()) {
            pendingDateLeft = newLeftDate;
            pendingDateRight = newRightDate;
            pendingRange = true;
            return;
        }


        if (!dateLeft.equals(newLeftDate)) {

            scrollLeftDate = newLeftDate.after(dateLeft) ? Scroll.Up : Scroll.Down;

            if (dateLeft.getYear() != newLeftDate.getYear()) {
                scrollLeftYear = true;
                datesRange.leftYearOut.copyFrom(datesRange.leftYearIn);
                datesRange.leftYearIn.text = String.valueOf(newLeftDate.getYear() + 1900);
            } else {
                scrollLeftYear = false;
            }

            if (dateLeft.getMonth() != newLeftDate.getMonth()) {
                scrollLeftMonth = true;
                datesRange.leftMonthOut.copyFrom(datesRange.leftMonthIn);
                datesRange.leftMonthIn.text = monthsText[newLeftDate.getMonth()];
                datesRange.leftMonthIn.x = leftMonthX + (maxMonthTextWidth - monthsWidth[newLeftDate.getMonth()]) / 2;
            } else {
                scrollLeftMonth = false;
            }

            if (dateLeft.getDate() != newLeftDate.getDate()) {
                scrollLeftDay = true;
                datesRange.leftDayOut.copyFrom(datesRange.leftDayIn);
                datesRange.leftDayIn.text = String.valueOf(newLeftDate.getDate());
                datesRange.leftDayIn.x = newLeftDate.getDate() < 10 ? oneDigitsWidth : 0;
            } else {
                scrollLeftDay = false;
            }
            dateLeft = newLeftDate;
        } else {
            scrollLeftDate = Scroll.None;
            scrollLeftYear = false;
            scrollLeftMonth = false;
            scrollLeftDay = false;
        }

        if (!dateRight.equals(newRightDate)) {

            scrollRightDate = newRightDate.after(dateRight) ? Scroll.Up : Scroll.Down;

            if (dateRight.getYear() != newRightDate.getYear()) {
                scrollRightYear = true;
                datesRange.rightYearOut.copyFrom(datesRange.rightYearIn);
                datesRange.rightYearIn.text = String.valueOf(newRightDate.getYear() + 1900);
            } else {
                scrollRightYear = false;
            }

            if (dateRight.getMonth() != newRightDate.getMonth()) {
                scrollRightMonth = true;
                datesRange.rightMonthOut.copyFrom(datesRange.rightMonthIn);
                datesRange.rightMonthIn.text = monthsText[newRightDate.getMonth()];
                datesRange.rightMonthIn.x = rightMonthX + (maxMonthTextWidth - monthsWidth[newRightDate.getMonth()]) / 2;
            } else {
                scrollRightMonth = false;
            }

            if (dateRight.getDate() != newRightDate.getDate()) {
                scrollRightDay = true;
                datesRange.rightDayOut.copyFrom(datesRange.rightDayIn);
                datesRange.rightDayIn.text = String.valueOf(newRightDate.getDate());
                datesRange.rightDayIn.x = newRightDate.getDate() < 10 ? rightDayX + oneDigitsWidth : rightDayX;
            } else {
                scrollRightDay = false;
            }

            dateRight = newRightDate;
        } else {
            scrollRightDate = Scroll.None;
            scrollRightYear = false;
            scrollRightMonth = false;
            scrollRightDay = false;
        }

        animator.start();
    }

    public void setTextColor(int color) {
        datesRange.setTextColor(color);
        invalidate();
    }

    private enum Scroll {
        None,
        Up,
        Down,
    }
}
