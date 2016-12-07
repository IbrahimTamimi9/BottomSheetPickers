package com.philliphsu.bottomsheetpickers.date;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.philliphsu.bottomsheetpickers.R;
import com.philliphsu.bottomsheetpickers.Utils;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import static android.support.v4.content.ContextCompat.getColor;
import static com.philliphsu.bottomsheetpickers.date.PagingDayPickerView.MONTH_NAVIGATION_BAR_SIZE;

/**
 * Created by Phillip Hsu on 12/6/2016.
 */
final class MonthPickerView extends View {
    private static final String TAG = "MonthPickerView";
    
    private static final int NUM_COLUMNS = 3;
    private static final int NUM_ROWS = 4;
    private static final int MONTH_SEPARATOR_WIDTH = 1;

    private static int MONTH_LABEL_TEXT_SIZE;
    private static int MONTH_SELECTED_CIRCLE_SIZE;

    // affects the padding on the sides of this view
    private int mEdgePadding = 0;
    
    private Paint mMonthLabelPaint;
    private Paint mSelectedCirclePaint;

    // Quick reference to the width of this view, matches parent
    private int mWidth;
    private int mRowHeight;
    private int mSelectedMonth = -1;
    // The presently selected year in the date picker
    private int mYear;
    // The presently selected day in the date picker
    private int mDayOfMonth;
    // The month of the current date
    private int mCurrentMonth = -1;

    private final String[] mShortMonthLabels;

    @Nullable  // Created only when a DatePickerController is set.
    private DateRangeHelper mDateRangeHelper;

    private int mNormalTextColor;
    private int mCurrentMonthTextColor;
    private int mDisabledMonthTextColor;
    private int mSelectedMonthTextColor;

    public MonthPickerView(Context context) {
        this(context, null);
    }

    public MonthPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();

        mShortMonthLabels = new DateFormatSymbols().getShortMonths();

        mNormalTextColor = getColor(context, R.color.text_color_primary_light);
        // Same as background color
        mSelectedMonthTextColor = getColor(context, R.color.date_picker_view_animator);
        mCurrentMonthTextColor = Utils.getThemeAccentColor(context);
        mDisabledMonthTextColor = getColor(context, R.color.text_color_disabled_light);

        MONTH_LABEL_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.month_picker_month_label_size);
        MONTH_SELECTED_CIRCLE_SIZE = res.getDimensionPixelSize(R.dimen.month_select_circle_radius);

        mRowHeight = (res.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height)
                - MONTH_NAVIGATION_BAR_SIZE) / NUM_ROWS;
        mEdgePadding = res.getDimensionPixelSize(R.dimen.month_view_edge_padding);

        // TODO: Set up accessibility components.
        
        // Sets up any standard paints that will be used
        initView();
        initialize(11, 7, 2016);
    }

    /**
     * Sets all the parameters for displaying the months.
     * <p>
     * Parameters have a default value and will only update if a new value is
     * included, except for focus month, which will always default to no focus
     * month if no value is passed in. The only required parameter is the week
     * start.
     * @param selectedMonth the selected month, or -1 for no selection
     * @param day the day of month
     * @param year the year
     */
    void initialize(int selectedMonth, int day, int year) {
        mSelectedMonth = selectedMonth;

        mYear = year;
        mCurrentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int daysInMonth = Utils.getDaysInMonth(mCurrentMonth, mYear);
        boolean isValidDayOfMonth = day >= 1 && day <= daysInMonth;
        if (isValidDayOfMonth) {
            mDayOfMonth = day;
        }

        // Invalidate cached accessibility information.
//        mTouchHelper.invalidateRoot();
        invalidate();
    }

    public void setSelectedMonth(int month) {
        mSelectedMonth = month;
    }

    public void setDatePickerController(DatePickerController controller) {
        mDateRangeHelper = new DateRangeHelper(controller);
    }

    void setTheme(Context context, boolean themeDark) {
        if (themeDark) {
            mNormalTextColor = getColor(context, R.color.text_color_primary_dark);
            mSelectedMonthTextColor = getColor(context, R.color.dark_gray);
            mDisabledMonthTextColor = getColor(context, R.color.text_color_disabled_dark);
            initView();
        }
    }

    /**
     * Sets up the text and style properties for painting. Override this if you
     * want to use a different paint.
     */
    protected void initView() {
        mMonthLabelPaint = new Paint();
        mMonthLabelPaint.setAntiAlias(true);
        mMonthLabelPaint.setTextSize(MONTH_LABEL_TEXT_SIZE);
        mMonthLabelPaint.setStyle(Style.FILL);
        mMonthLabelPaint.setTextAlign(Align.CENTER);
        mMonthLabelPaint.setFakeBoldText(false);

        mSelectedCirclePaint = new Paint();
        mSelectedCirclePaint.setFakeBoldText(true);
        mSelectedCirclePaint.setAntiAlias(true);
        mSelectedCirclePaint.setColor(mCurrentMonthTextColor);
        mSelectedCirclePaint.setTextAlign(Align.CENTER);
        mSelectedCirclePaint.setStyle(Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight * NUM_ROWS
                + MONTH_NAVIGATION_BAR_SIZE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;

        // Invalidate cached accessibility information.
//        mTouchHelper.invalidateRoot();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawMonthLabels(canvas);
    }

    public int getYear() {
        return mYear;
    }

    public int getDayOfMonth() {
        return mDayOfMonth;
    }

    private void drawMonthLabels(Canvas canvas) {
        int y = (((mRowHeight + MONTH_LABEL_TEXT_SIZE) / 2) - MONTH_SEPARATOR_WIDTH);
        final float monthWidthHalf = (mWidth - mEdgePadding * 2) / (NUM_COLUMNS * 2.0f);
        int col = 0;
        for (int month = Calendar.JANUARY; month <= Calendar.DECEMBER; month++) {
            final int x = (int)((2 * col + 1) * monthWidthHalf + mEdgePadding);
            drawMonthLabel(canvas, mYear, month, mDayOfMonth, x, y);
            col++;
            if (col == NUM_COLUMNS) {
                col = 0;
                y += mRowHeight;
            }
        }
    }

    private void drawMonthLabel(Canvas canvas, int year, int month, int day, int x, int y) {
        if (mSelectedMonth == month) {
            canvas.drawCircle(x , y - (MONTH_LABEL_TEXT_SIZE / 3), MONTH_SELECTED_CIRCLE_SIZE,
                    mSelectedCirclePaint);
        }

        // TODO: From MonthPicker, navigate to and select a year in year picker. You will see that
        // the navigation arrows are made visible again! Make sure they stay invisible.

        // If we have a mindate or maxdate, gray out the month if it's outside the range.
        // If the date range helper has not been created, just let the runtime throw an NPE.
        if (mDateRangeHelper != null && mDateRangeHelper.isOutOfRange(year, month, day)) {
            mMonthLabelPaint.setColor(mDisabledMonthTextColor);
        } else if (mCurrentMonth == month) {
            mMonthLabelPaint.setFakeBoldText(true);
            mMonthLabelPaint.setColor(mSelectedMonth == month ? mSelectedMonthTextColor : mCurrentMonthTextColor);
        } else {
            mMonthLabelPaint.setFakeBoldText(mSelectedMonth == day);
            mMonthLabelPaint.setColor(mSelectedMonth == month ? mSelectedMonthTextColor : mNormalTextColor);
        }
        canvas.drawText(mShortMonthLabels[month], x, y, mMonthLabelPaint);
    }
}
