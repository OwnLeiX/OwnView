package lx.own.view.offline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * <p> </p><br/>
 *
 * @author Lx
 * @date 26/12/2016
 */

public class OwnPagerTabIndicator extends FrameLayout implements ViewPager.OnPageChangeListener {

    private static final String TAG = "OwnPagerTabIndicator";

    public static final int MODE_HORIZONTAL = 394;
    public static final int MODE_VERTICAL = 489;

    private ViewPager mViewPager;
    private PagerAdapter mAdapter;
    private LinearLayout mLinearLayout;
    private ArrayList<TextView> mTabs;
    private ArrayList<ViewPager.OnPageChangeListener> mListeners;

    private int mTextColor = 0xFFFFFFFF;
    private int mDividerColor = 0x37FFFFFF;
    private int mIndicatorColor = 0xFFFF0000;
    private int mClickColor = 0x37FF0000;

    private float mTextSize = 16.0f;
    private int mIndicatorHeight = 7;
    private int mDividerPadding = 10;

    private float mRoundX = 5;
    private float mRoundY = 10;
    private int mIndicatorLeft = 0;
    private int mTargetLeft = 0;

    private int mIndicatorTop = 0;
    private int mTargetTop = 0;

    private Paint mIndicatorPaint;
    private Paint mDividerPaint;

    private LinearLayout.LayoutParams mTabParams;

    private int mMode = MODE_HORIZONTAL;

    public OwnPagerTabIndicator(Context context) {
        super(context);
        init(context);
    }

    public OwnPagerTabIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OwnPagerTabIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

//    public OwnPagerTabIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    private void init(Context context) {
        mListeners = new ArrayList<>();
        mTabs = new ArrayList<>();
        mLinearLayout = new LinearLayout(context);
        mTabParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mTabParams.weight = 1;
        addView(mLinearLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mIndicatorPaint = new Paint();
        mIndicatorPaint.setAntiAlias(true);

        mDividerPaint = new Paint();
        mDividerPaint.setAntiAlias(true);
        mDividerPaint.setColor(mDividerColor);
    }

    public void setViewPager(ViewPager viewPager) {
        mAdapter = viewPager.getAdapter();
        if (mAdapter == null)
            throw new IllegalArgumentException("this ViewPager doesn't has an Adapter !");
        mViewPager = viewPager;
        initTabs();
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setCurrentItem(0);
        invalidate();
    }

    private void initTabs() {
        for (int i = 0; i < mAdapter.getCount(); i++) {
            TextView tab = new TextView(getContext());
            tab.setText(mAdapter.getPageTitle(i));
            tab.setTextColor(mTextColor);
            tab.setTextSize(mTextSize);
            tab.setGravity(Gravity.CENTER);
            tab.setTypeface(Typeface.DEFAULT_BOLD);
            final int position = i;
            tab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(position);
                }
            });
            mTabs.add(tab);
            mLinearLayout.addView(tab, mTabParams);
        }
    }

    private void upDateTabs() {
        for (int i = 0; i < mTabs.size(); i++) {
            TextView tab = mTabs.get(i);
            tab.setText(mAdapter.getPageTitle(i));
            tab.setTextColor(mTextColor);
            tab.setTextSize(mTextSize);
            tab.setBackgroundColor(Color.TRANSPARENT);
            if (i == mViewPager.getCurrentItem())
                tab.setTextColor(mIndicatorColor);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMode == MODE_HORIZONTAL) {
            int height = getHeight();
            int currentItem = mViewPager.getCurrentItem();
            if (Math.abs(mIndicatorLeft - mTargetLeft) > 10) {
                mIndicatorPaint.setColor(mClickColor);
                TextView tab = mTabs.get(currentItem);
                Rect rect = new Rect(tab.getLeft(), tab.getTop(), tab.getRight(), tab.getBottom());
                canvas.drawRect(rect, mIndicatorPaint);
            }
            mIndicatorPaint.setColor(mIndicatorColor);
            RectF rect = new RectF(mIndicatorLeft, height - mIndicatorHeight, mIndicatorLeft + mTabs.get(currentItem).getWidth(), height);
            canvas.drawRoundRect(rect, mRoundX, mRoundY, mIndicatorPaint);
            for (int i = 0; i < mTabs.size() - 1; i++) {
                canvas.drawLine(mTabs.get(i).getRight(), 0 + mDividerPadding, mTabs.get(i).getRight(), height - mDividerPadding, mDividerPaint);
            }
        } else {
            int width = getWidth();
            int currentItem = mViewPager.getCurrentItem();
            if (Math.abs(mIndicatorLeft - mTargetLeft) > 10) {
                mIndicatorPaint.setColor(mClickColor);
                TextView tab = mTabs.get(currentItem);
                Rect rect = new Rect(tab.getLeft(), tab.getTop(), tab.getRight(), tab.getBottom());
                canvas.drawRect(rect, mIndicatorPaint);
            }
            mIndicatorPaint.setColor(mIndicatorColor);
            RectF rect = new RectF(width - mIndicatorHeight, mIndicatorTop, width, mIndicatorTop + mTabs.get(currentItem).getHeight());
            canvas.drawRoundRect(rect, mRoundX, mRoundY, mIndicatorPaint);
            for (int i = 0; i < mTabs.size() - 1; i++) {
                canvas.drawLine(0 + mDividerPadding, mTabs.get(i).getBottom(), width - mDividerPadding, mTabs.get(i).getBottom(), mDividerPaint);
            }
        }
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        TextView tv = mTabs.get(position);
        if (mMode == MODE_HORIZONTAL) {
            mIndicatorLeft = (int) (tv.getRight() - (1.0f - positionOffset) * tv.getWidth());
            mTargetLeft = tv.getLeft();
        } else {
            mIndicatorTop = (int) (tv.getBottom() - (1.0f - positionOffset) * tv.getHeight());
            mTargetTop = tv.getTop();
        }
        upDateTabs();
        invalidate();
        if (mListeners.size() > 0) {
            for (ViewPager.OnPageChangeListener mListener : mListeners) {
                mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (mListeners.size() > 0) {
            for (ViewPager.OnPageChangeListener mListener : mListeners) {
                mListener.onPageSelected(position);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mListeners.size() > 0) {
            for (ViewPager.OnPageChangeListener mListener : mListeners) {
                mListener.onPageScrollStateChanged(state);
            }
        }

    }

    public void addOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mListeners.add(listener);
    }

    public void removeOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mListeners.remove(listener);
    }

    public void setMode(int mode) {
        mMode = mode;
        switch (mMode) {
            case MODE_HORIZONTAL:
                mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                break;
            case MODE_VERTICAL:
                mLinearLayout.setOrientation(LinearLayout.VERTICAL);
                break;
        }
    }
}
