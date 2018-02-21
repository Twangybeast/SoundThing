package twangybeast.myapplication.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import twangybeast.myapplication.helpers.CoordinateHelper;

import java.io.File;

/**
 * Created by Twangybeast on 2/20/2018.
 */

public class NoteSummaryView extends View
{
    public static final int TITLE_SIZE = 75;
    public static final int DATE_SIZE = 45;
    String mNoteTitle = null;
    String mLastModified;
    GestureDetectorCompat mDetector;
    private OnClickListener onClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {

        }
    };
    GestureDetector.OnGestureListener onGestureListener;
    private String filePath;

    public NoteSummaryView(Context context)
    {
        super(context);
        init(context);
    }

    public NoteSummaryView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public NoteSummaryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context)
    {
        setPadding(Math.round(CoordinateHelper.dpToPixel(16)), Math.round(CoordinateHelper.dpToPixel(8)), Math.round(CoordinateHelper.dpToPixel(16)), Math.round(CoordinateHelper.dpToPixel(8)));
        onGestureListener = new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent event)
            {
                return true;
            }
            @Override
            public boolean onSingleTapUp(MotionEvent event)
            {
                onClickListener.onClick(NoteSummaryView.this);
                return true;
            }
        };
        mDetector = new GestureDetectorCompat(context, onGestureListener);
    }
    public void setOnClickListener(View.OnClickListener listener)
    {
        this.onClickListener = listener;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {

        int desiredWidth = 1200;
        int desiredHeight = (int) getDesiredHeight();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY)
        {
            width = widthSize;
        }
        else if (widthMode == MeasureSpec.AT_MOST)
        {
            width = Math.min(desiredWidth, widthSize);
        }
        else
        {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY)
        {
            height = heightSize;
        }
        else if (heightMode == MeasureSpec.AT_MOST)
        {
            height = Math.min(desiredHeight, heightSize);
        }
        else
        {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }
    public void setFilePath(String path)
    {
        this.filePath = path;
    }
    public String getFilePath()
    {
        return filePath;
    }
    public void setNoteTitle(String title)
    {
        mNoteTitle = title;
    }

    public void setLastModified(String lastModified)
    {
        mLastModified = lastModified;
    }
    public float getDesiredHeight()
    {
        float result = getPaddingTop() + getPaddingBottom() + CoordinateHelper.dpToPixel(9 );
        Rect rect = new Rect();
        Paint paint = new Paint();
        paint.setTextSize(TITLE_SIZE);
        paint.getTextBounds(mNoteTitle, 0, mNoteTitle.length(), rect);
        result += rect.height();
        paint.setTextSize(DATE_SIZE);
        paint.getTextBounds(mLastModified, 0, mLastModified.length(), rect);
        result += rect.height();
        return result;
    }
    @Override
    public void onDraw(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawColor(0xFFFFFFFF);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        Rect title = new Rect();
        paint.setTextSize(TITLE_SIZE);
        paint.getTextBounds(mNoteTitle, 0, mNoteTitle.length(), title);
        String titleStr = mNoteTitle;
        int maxWidth = getWidth() - (getPaddingLeft() + getPaddingRight());
        if (title.width() >= maxWidth)
        {
            int amount = 0;
            do
            {
                amount++;
                paint.getTextBounds(mNoteTitle.substring(0, amount) +"...", 0, amount + 3, title);
            }while (title.width() < maxWidth && amount <= mNoteTitle.length());
            titleStr = mNoteTitle.substring(0, amount-1) +"...";
        }
        canvas.drawText(titleStr, getPaddingLeft(), getPaddingTop() + title.height(), paint);
        Rect date = new Rect();
        paint.setColor(0xFFAAAAAA);
        paint.setTextSize(DATE_SIZE);
        paint.getTextBounds(mLastModified, 0, mLastModified.length(), date);
        canvas.drawText(mLastModified, getPaddingLeft(), getPaddingTop() + title.height() + date.height() + CoordinateHelper.dpToPixel(8), paint);
        paint.setColor(Color.GRAY);
        canvas.drawRect(0, getHeight() - CoordinateHelper.dpToPixel(1), getWidth(), getHeight(), paint);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (mDetector != null && mDetector.onTouchEvent(event))
        {
            return true;
        }
        return super.onTouchEvent(event);
    }
}
