package lx.own.view;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;

/**
 * <p>状态布局，用于显示加载失败-重新加载-加载中的新需求</p><br/>
 *
 * @author Lx
 * 23/12/2016
 */

public class StateFrameLayout extends FrameLayout implements View.OnClickListener {

    @IntDef({STATE_LOADING, STATE_FAILED, STATE_SUCCESS, STATE_EMPTY})
    private @interface State {
    }

    @Override
    public void onClick(View v) {
        if (v == mFailedView && mOnFailedViewClickedListener != null)
            mOnFailedViewClickedListener.onFailedViewClicked();
    }

    public interface OnFailedViewClickedListener {
        void onFailedViewClicked();
    }

    private OnFailedViewClickedListener mOnFailedViewClickedListener;

    public OnFailedViewClickedListener getOnRetryListener() {
        return mOnFailedViewClickedListener;
    }

    public void setOnRetryListener(OnFailedViewClickedListener l) {
        this.mOnFailedViewClickedListener = l;
    }

    public static final int STATE_LOADING = 1;
    public static final int STATE_FAILED = 1 << 1;
    public static final int STATE_SUCCESS = 1 << 2;
    public static final int STATE_EMPTY = 1 << 3;

    private int mState;
    private ViewStub mEmptyVS;
    private ViewStub mLoadingVS;
    private ViewStub mFailedVS;

    private int mEmptyRes = -1;
    private int mLoadingRes = -1;
    private int mFailedRes = -1;

    private View mEmptyView;
    private View mLoadingView;
    private View mFailedView;

    public StateFrameLayout(Context context) {
        this(context, null);
    }

    public StateFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mState = STATE_LOADING;
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mEmptyVS = new ViewStub(getContext());
        mEmptyVS.setLayoutParams(params);
        addView(mEmptyVS);
        mLoadingVS = new ViewStub(getContext());
        mLoadingVS.setLayoutParams(params);
        addView(mLoadingVS);
        mFailedVS = new ViewStub(getContext());
        mFailedVS.setLayoutParams(params);
        addView(mFailedVS);
    }

    //	public StateLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    //		super(context, attrs, defStyleAttr, defStyleRes);
    //	}

    @State
    public int getCurrentState() {
        return mState;
    }

    public void setState(@State int state) {
        mState = state;
        refreshDisplayView();
    }

    public boolean resetmEmptyRes(@LayoutRes int mEmptyRes) {
        this.mEmptyRes = mEmptyRes;
        return mEmptyView == null;
    }

    public boolean resetmLoadingRes(@LayoutRes int mLoadingRes) {
        this.mLoadingRes = mLoadingRes;
        return mLoadingView == null;
    }

    public boolean resetmFailedRes(@LayoutRes int mFailedRes) {
        this.mFailedRes = mFailedRes;
        return mFailedView == null;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

    }

    private void refreshDisplayView() {
        hideAllStatus();
        switch (mState) {
            case STATE_LOADING:
                if (buildLoadingView())
                    mLoadingView.setVisibility(VISIBLE);
                break;
            case STATE_SUCCESS:

                break;
            case STATE_FAILED:
                if (buildFailedView()) {
                    mFailedView.setOnClickListener(this);
                    mFailedView.setVisibility(VISIBLE);
                }
                break;
            case STATE_EMPTY:
                if (buildEmptyView())
                    mEmptyView.setVisibility(VISIBLE);
                break;
            default:
                throw new IllegalArgumentException("Unknown State has been set!");
        }
    }

    private void hideAllStatus() {
        if (mEmptyView != null)
            mEmptyView.setVisibility(View.GONE);
        if (mLoadingView != null)
            mLoadingView.setVisibility(View.GONE);
        if (mFailedView != null) {
            mFailedView.setVisibility(View.GONE);
            mFailedView.setOnClickListener(null);
        }
    }

    private boolean buildEmptyView() {
        if (mEmptyView == null && mEmptyRes != -1 && mEmptyVS.getParent() != null) {
            mEmptyVS.setLayoutResource(mEmptyRes);
            mEmptyView = mEmptyVS.inflate();
        }
        return mEmptyView != null;
    }

    private boolean buildFailedView() {
        if (mFailedView == null && mFailedRes != -1 && mFailedVS.getParent() != null) {
            mFailedVS.setLayoutResource(mFailedRes);
            mFailedView = mFailedVS.inflate();
        }
        return mFailedView != null;
    }

    private boolean buildLoadingView() {
        if (mLoadingView == null && mLoadingRes != -1 && mLoadingVS.getParent() != null) {
            mLoadingVS.setLayoutResource(mLoadingRes);
            mLoadingView = mLoadingVS.inflate();
        }
        return mLoadingView != null;
    }
}
