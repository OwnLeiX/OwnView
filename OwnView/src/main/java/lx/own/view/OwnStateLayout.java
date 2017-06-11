package lx.own.view;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * <p> </p><br/>
 *
 * @author Lx
 * @date 23/12/2016
 */

public class OwnStateLayout extends FrameLayout {

    public static final int STATE_LOADING = 1;
    public static final int STATE_FAILED = 1 << 1;
    public static final int STATE_SUCCESS = 1 << 2;
    public static final int STATE_EMPTY = 1 << 3;

    private int mState;
    private View mEmptyView;
    private View mLoadingView;
    private View mFailedView;
    private View mSuccessView;

    public View getEmptyView() {
        return mEmptyView;
    }

    public View getSuccessView() {
        return mSuccessView;
    }

    public View getFailedView() {
        return mFailedView;
    }

    public View getLoadingView() {
        return mLoadingView;
    }

    public int getCurrentState() {
        return mState;
    }

    public void resetEmptyView(View emptyView) {
        if (emptyView == null)
            throw new IllegalArgumentException("the method OwnStateLayout.resetEmptyView(View emptyView) can not have a null argument!");
        removeView(mEmptyView);
        mEmptyView = emptyView;
        addView(mEmptyView);
        refreshDisplayView();
    }

    public void resetFailedView(View failedView) {
        if (failedView == null)
            throw new IllegalArgumentException("the method OwnStateLayout.resetEmptyView(View emptyView) can not have a null argument!");
        removeView(mFailedView);
        mFailedView = failedView;
        addView(mFailedView);
        refreshDisplayView();
    }

    public void resetLoadingView(View loadingView) {
        if (loadingView == null)
            throw new IllegalArgumentException("the method OwnStateLayout.resetEmptyView(View emptyView) can not have a null argument!");
        removeView(mLoadingView);
        mLoadingView = loadingView;
        addView(mLoadingView);
        refreshDisplayView();
    }


    public void setState(int state) {
        mState = state;
        refreshDisplayView();
    }

    private OwnStateLayout(Context context, View successView, View failedView, View emptyView, View loadingView) {
        super(context);
        mEmptyView = emptyView;
        mSuccessView = successView;
        mFailedView = failedView;
        mLoadingView = loadingView;
        init();
    }

   /* public StateLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }*/

    private void init() {
        mState = STATE_LOADING;
        initStateViews();
        refreshDisplayView();
    }

    private void initStateViews() {
        LayoutParams defaultParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        defaultParams.gravity = Gravity.CENTER;

        LayoutParams customParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (mLoadingView == null) {
            ProgressBar loading = new ProgressBar(getContext());
            loading.setIndeterminate(true);
            loading.setLayoutParams(defaultParams);
            mLoadingView = loading;
        } else {
            mLoadingView.setLayoutParams(customParams);
        }

        if (mFailedView == null) {
            TextView failed = new TextView(getContext());
            failed.setText("Failed, Try Again.");
            failed.setLayoutParams(defaultParams);
        } else {
            mFailedView.setLayoutParams(customParams);
        }
        if (mSuccessView == null) {
            TextView success = new TextView(getContext());
            success.setText("Success!");
            success.setLayoutParams(defaultParams);
            mSuccessView = success;
        } else {
            mSuccessView.setLayoutParams(customParams);
        }
        if (mEmptyView == null) {
            TextView empty = new TextView(getContext());
            empty.setText("Data is Empty");
            empty.setLayoutParams(defaultParams);
            mEmptyView = empty;
        } else {
            mEmptyView.setLayoutParams(customParams);
        }
        addView(mSuccessView);
        addView(mFailedView);
        addView(mLoadingView);
        addView(mEmptyView);
    }

    private void refreshDisplayView() {
        mLoadingView.setVisibility(View.GONE);
        mFailedView.setVisibility(View.GONE);
        mSuccessView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        switch (mState) {
            case STATE_LOADING:
                mLoadingView.setVisibility(VISIBLE);
                break;
            case STATE_SUCCESS:
                mSuccessView.setVisibility(VISIBLE);
                break;
            case STATE_FAILED:
                mFailedView.setVisibility(VISIBLE);
                break;
            case STATE_EMPTY:
                mEmptyView.setVisibility(VISIBLE);
                break;
            default:
                throw new IllegalArgumentException("Unknown State has been set!");
        }
    }

    public static class Builder {

        private View success, empty, loading, failed;
        private int retryResId = -1;
        private Context mContext;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder setSucessView(int resId) {
            success = LayoutInflater.from(mContext).inflate(resId, null);
            return this;
        }

        public Builder setEmptyView(int resId) {
            empty = LayoutInflater.from(mContext).inflate(resId, null);
            return this;
        }

        public Builder setLoadingView(int resId) {
            loading = LayoutInflater.from(mContext).inflate(resId, null);
            return this;
        }

        public Builder setFailedView(int resId) {
            failed = LayoutInflater.from(mContext).inflate(resId, null);
            return this;
        }

        public Builder setFailedViewWithRetryView(int failedViewResId, int retryViewResId) {
            failed = LayoutInflater.from(mContext).inflate(failedViewResId, null);
            retryResId = retryViewResId;
            return this;
        }

        public Builder setSucessView(View view) {
            success = view;
            return this;
        }

        public Builder setEmptyView(View view) {
            empty = view;
            return this;
        }

        public Builder setLoadingView(View view) {
            loading = view;
            return this;
        }

        public Builder setFailedView(View view) {
            failed = view;
            return this;
        }

        public OwnStateLayout create() {
            OwnStateLayout instance = new OwnStateLayout(mContext, success, failed, empty, loading);
            return instance;
        }

    }
}
