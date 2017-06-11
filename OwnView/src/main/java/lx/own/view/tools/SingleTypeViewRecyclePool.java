package lx.own.view.tools;

import android.view.View;

import java.util.LinkedList;

/**
 * <b>单种类View复用池</b>
 * Created on 2017/5/27.
 *
 * @author LeiXun
 */

public class SingleTypeViewRecyclePool {
    private LinkedList<View> mRecycledViews;

    public SingleTypeViewRecyclePool()
    {
        mRecycledViews = new LinkedList<>();
    }

    public View obtain()
    {
        return mRecycledViews.poll();
    }

    public void recycle(View view)
    {
        if (view != null)
            mRecycledViews.offer(view);
    }

    public void clear(){
        mRecycledViews.clear();
    }
}
