package lx.own.view.online.tools;

import android.view.View;

import java.util.Collection;
import java.util.LinkedList;

/**
 * <b>单种类View复用池</b>
 * Created on 2017/5/27.
 *
 * @author LeiXun
 */

public class SingleTypeViewRecyclePool {
    private LinkedList<View> mRecycledViews;

    public SingleTypeViewRecyclePool() {
        mRecycledViews = new LinkedList<>();
    }

    public View obtain() {
        return mRecycledViews.poll();
    }

    public void recycle(View view) {
        if (view != null)
            mRecycledViews.offer(view);
    }

    public void recycle(Collection<View> collection) {
        if (collection != null && collection.size() > 0)
            mRecycledViews.addAll(collection);
    }

    public void clear() {
        mRecycledViews.clear();
    }


}
