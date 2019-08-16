package lx.own.view.online;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import lx.own.R;

/**
 * <p>状态布局，用于显示加载失败-重新加载-加载中的新需求</p><br/>
 *
 * @author Lx
 * @date 23/12/2016
 * <p>
 * 支持自定义属性.以便各个业务根据需要自己调整
 * app:stateFrame_fail_layout,stateFrame_loading_layout
 * @update by xjunjie@gmail.com 2019/6/24 15:33
 */
@SuppressWarnings("Convert2Lambda")
public class StateFrameLayout extends FrameLayout {

    public StateFrameLayout(@NonNull Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public StateFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public StateFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StateFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init(context, attrs, defStyleAttr);
    }

    public interface OnRetryListener {
        void onRetry();
    }

    private OnRetryListener mOnRetryListener;

    public OnRetryListener getOnRetryListener() {
        return mOnRetryListener;
    }

    public void resetOnRetryListener(OnRetryListener mOnRetryListener) {
        this.mOnRetryListener = mOnRetryListener;
    }

    private int mState;
    private ViewStub mEmptyVS;
    private ViewStub mLoadingVS;
    private ViewStub mFailedVS;

    private int mEmptyRes;
    private int mLoadingRes;  //默认
    private int mFailedRes;    //默认
    private int mInitState = STATE.LOADING;

    private View mEmptyView;
    private View mLoadingView;
    private View mFailedView;


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mState = this.mInitState;
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mEmptyVS = new ViewStub(getContext());
        mEmptyVS.setLayoutParams(params);
        mEmptyVS.setLayoutResource(mEmptyRes);
        addView(mEmptyVS);
        mLoadingVS = new ViewStub(getContext());
        mLoadingVS.setLayoutParams(params);
        mLoadingVS.setLayoutResource(mLoadingRes);
        addView(mLoadingVS);
        mFailedVS = new ViewStub(getContext());
        mFailedVS.setLayoutParams(params);
        mFailedVS.setLayoutResource(mFailedRes);
        addView(mFailedVS);
        refreshDisplayView();
    }

    @StateRef
    public int getCurrentState() {
        return mState;
    }

    public synchronized void setState(@StateRef int state) {
        if ((this.mState & state) == 0) {
            this.setStateSurely(state);
        }
    }

    public synchronized void setStateSurely(@StateRef int state) {
        this.mState = state;
        refreshDisplayView();
    }

    public void setEmptyRes(int res) {
        if (this.mEmptyRes != res) {
            this.mEmptyRes = res;
            if (mEmptyVS.getParent() == this)
                this.removeView(mEmptyVS);
            if (mEmptyView != null && mEmptyView.getParent() == this) {
                this.removeView(mEmptyView);
                mEmptyView = null;
            }
            final LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mEmptyVS = new ViewStub(getContext());
            mEmptyVS.setLayoutParams(params);
            mEmptyVS.setLayoutResource(mEmptyRes);
            addView(mEmptyVS);
            refreshDisplayView();
        }
    }

    public void setLoadingRes(int res) {
        if (this.mLoadingRes != res) {
            this.mLoadingRes = res;
            if (mLoadingVS.getParent() == this)
                this.removeView(mLoadingVS);
            if (mLoadingView != null && mLoadingView.getParent() == this) {
                this.removeView(mLoadingView);
                mLoadingView = null;
            }
            final LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mLoadingVS = new ViewStub(getContext());
            mLoadingVS.setLayoutParams(params);
            mLoadingVS.setLayoutResource(mLoadingRes);
            addView(mLoadingVS);
            refreshDisplayView();
        }
    }

    public void setFailedRes(int res) {
        if (this.mFailedView == null && this.mFailedRes != res) {
            this.mFailedRes = res;
            if (mFailedVS.getParent() == this)
                this.removeView(mFailedVS);
            if (mFailedView != null && mFailedView.getParent() == this) {
                this.removeView(mFailedView);
                mFailedView = null;
            }
            final LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mFailedVS = new ViewStub(getContext());
            mFailedVS.setLayoutParams(params);
            mFailedVS.setLayoutResource(mFailedRes);
            addView(mFailedVS);
            refreshDisplayView();
        }
    }

    public View getEmptyView() {
        return mEmptyView;
    }

    public View getLoadingView() {
        return mLoadingView;
    }

    public View getFailedView() {
        return mFailedView;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        if (context != null && attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.StateFrameLayout);
            if (typedArray != null) {
                this.mFailedRes = typedArray.getResourceId(R.styleable.StateFrameLayout_stateFrame_fail_layout, NO_ID);
                this.mLoadingRes = typedArray.getResourceId(R.styleable.StateFrameLayout_stateFrame_loading_layout, NO_ID);
                this.mEmptyRes = typedArray.getResourceId(R.styleable.StateFrameLayout_stateFrame_empty_layout, NO_ID);
                this.mInitState = typedArray.getInteger(R.styleable.StateFrameLayout_stateFrame_init_state, STATE.LOADING);
                typedArray.recycle();
            }
        }
    }

    private void refreshDisplayView() {
        hideAllStatus();
        switch (mState) {
            case STATE.LOADING: {
                if (mLoadingView == null) {
                    mLoadingView = mLoadingVS.inflate();
                }
                mLoadingView.setVisibility(VISIBLE);
            }
            break;
            case STATE.SUCCESS: {
            }
            break;
            case STATE.FAIL: {
                if (mFailedView == null) {
                    mFailedView = mFailedVS.inflate();
                    mFailedView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mOnRetryListener != null)
                                mOnRetryListener.onRetry();
                        }
                    });
                }
                mFailedView.setVisibility(VISIBLE);
            }
            break;
            case STATE.EMPTY: {
                if (mEmptyView == null) {
                    mEmptyView = mEmptyVS.inflate();
                }
                mEmptyView.setVisibility(VISIBLE);
            }
            break;
            default:
                throw new IllegalArgumentException("Unknown STATE has been set!");
        }
    }

    private void hideAllStatus() {
        if (mEmptyView != null)
            mEmptyView.setVisibility(View.GONE);
        if (mLoadingView != null)
            mLoadingView.setVisibility(View.GONE);
        if (mFailedView != null)
            mFailedView.setVisibility(View.GONE);
    }

    @IntDef({STATE.LOADING, STATE.FAIL, STATE.SUCCESS, STATE.EMPTY})
    @interface StateRef {
    }

    public interface STATE {
        int LOADING = 1;
        int FAIL = 1 << 1;
        int SUCCESS = 1 << 2;
        int EMPTY = 1 << 3;
    }
}
