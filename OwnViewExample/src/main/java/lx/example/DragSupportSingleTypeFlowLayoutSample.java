package lx.example;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

import lx.own.view.online.DragSupportSingleTypeFlowLayout;
import lx.own.view.online.SingleTypeFlowLayout;

/**
 * <p> </p><br/>
 *
 * @author Lx
 * @date 11/06/2017
 */

public class DragSupportSingleTypeFlowLayoutSample extends AppCompatActivity {

    private InnerAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_support_flow_layout);
        final DragSupportSingleTypeFlowLayout v = findViewById(R.id.dsstfl);
        mAdapter = new InnerAdapter();
        mAdapter.refreshData(new ArrayList<>(Arrays.asList(Arrays.copyOfRange(Datas.sCheeseStrings, 0, 40))));
        v.enableDragging(new DragSupportSingleTypeFlowLayout.DraggingHelper() {
            @Override
            public void onStartDragging(int i, View view) {

            }

            @Override
            public void onEndDragging(int i, int i1, View view) {
                mAdapter.swicthItem(i, i1, false);
            }
        }, new DragSupportSingleTypeFlowLayout.DefaultDraggingDecoration());
        v.setAdapter(mAdapter);
    }

    private class InnerAdapter extends SingleTypeFlowLayout.BaseFlowItemAdapter {

        private ArrayList<String> mDatas;

        public void refreshData(ArrayList<String> data) {
            mDatas = data;
        }

        @MainThread
        public void swicthItem(int oldPosition, int newPosition, boolean notify) {
            if (oldPosition != newPosition && mDatas != null && mDatas.size() > oldPosition && mDatas.size() > newPosition) {
                if (newPosition < oldPosition) {
                    mDatas.add(newPosition, mDatas.remove(oldPosition));
                } else {
                    mDatas.add(newPosition, mDatas.remove(oldPosition));
                }
                if (notify)
                    notifyDataSetChanged();
            }
        }

        @Override
        public View getView(View convertView, int position, ViewGroup parent) {
            SubInnerHolder holder;
            if (convertView == null) {
                holder = new SubInnerHolder(parent);
                convertView = holder.mRoot;
                convertView.setTag(holder);
            } else {
                holder = (SubInnerHolder) convertView.getTag();
            }
            holder.setData(mDatas.get(position));
            return convertView;
        }

        @Override
        public int getCount() {
            return mDatas == null ? 0 : mDatas.size();
        }
    }

    private class SubInnerHolder {
        View mRoot;
        TextView subItem_tv;

        public SubInnerHolder(ViewGroup parent) {
            initViews(parent);
        }

        private void initViews(ViewGroup parent) {
            mRoot = getLayoutInflater().inflate(R.layout.subitem_iv_tv, parent, false);
            subItem_tv = (TextView) mRoot.findViewById(R.id.subItem_tv);
        }

        void setData(String data) {
            subItem_tv.setText(data);
        }
    }
}
