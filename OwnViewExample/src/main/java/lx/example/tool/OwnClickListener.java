package lx.example.tool;

import android.view.View;

/**
 * <p> </p><br/>
 *
 * @author Lx
 * @date 11/06/2017
 */

public abstract class OwnClickListener implements View.OnClickListener {
    private long mClickInterval = 500;
    private long mPreClickTime;
    private View mPreClickView;

    public OwnClickListener() {
    }

    public OwnClickListener(long interval) {
        this.mClickInterval = interval;
    }

    @Override
    final public void onClick(View v) {
        long timeMillis = System.currentTimeMillis();
        if (timeMillis - mPreClickTime > mClickInterval) {
            this.onValidClick(v);
            mPreClickView = v;
            mPreClickTime = timeMillis;
        }else if (v == mPreClickView){
            this.onDoubleClick(v);
        }
    }

    protected void onDoubleClick(View v) {

    }

    protected void onValidClick(View v) {

    }
}
