package lx.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Random;

import lx.own.view.online.SingleTypeFlowLayout;
import lx.own.view.online.tools.SingleTypeViewRecyclePool;

/**
 * <p> </p><br/>
 *
 * @author Lx
 * @date 11/06/2017
 */

public class FlowLayoutSample extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView lv = (ListView) getLayoutInflater().inflate(R.layout.activity_flow_layout, (ViewGroup) getWindow().getDecorView(), false);
        setContentView(lv);
        lv.setAdapter(new InnerAdapter());
    }

    private class InnerAdapter extends BaseAdapter {

        private SingleTypeViewRecyclePool mPool = new SingleTypeViewRecyclePool();

        @Override
        public int getCount() {
            return 30;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            InnerHolder holder;
            if (convertView == null) {
                holder = new InnerHolder(parent,mPool);
                convertView = holder.mRoot;
                convertView.setTag(holder);
            }else {
                holder = (InnerHolder) convertView.getTag();
            }
            holder.setData(position);
            return convertView;
        }

    }

    private class InnerHolder extends SingleTypeFlowLayout.BaseFlowItemAdapter {
        private Random mRandom = new Random();
        private int mCount;
        private View mRoot;
        private SingleTypeFlowLayout flowLayout;
        private TextView tv;

        public InnerHolder(ViewGroup parent, SingleTypeViewRecyclePool pool) {
            mCount = mRandom.nextInt(Datas.NAMES.length);
            initViews(parent, pool);
        }

        private void initViews(ViewGroup parent, SingleTypeViewRecyclePool pool) {
            mRoot = getLayoutInflater().inflate(R.layout.item_flow_layout, parent, false);
            tv = (TextView) mRoot.findViewById(R.id.item_tv);
            flowLayout = (SingleTypeFlowLayout) mRoot.findViewById(R.id.item_flowLayout);
            flowLayout.setSingleTypeViewRecyclePool(pool);
            flowLayout.setAdapter(this);
        }

        void setData(int position){
            tv.setText("This is item " + position + "in ListView.");
            notifyDataSetChanged();
        }

        @Override
        public View getView(View convertView, int position, ViewGroup parent) {
            SubInnerHolder holder;
            if (convertView == null) {
                holder = new SubInnerHolder(parent);
                convertView = holder.mRoot;
                convertView.setTag(holder);
            }else {
                holder = (SubInnerHolder) convertView.getTag();
            }
            holder.setData(Datas.sCheeseStrings[mRandom.nextInt(Datas.sCheeseStrings.length / mCount)]);
            return convertView;
        }

        @Override
        public int getCount() {
            return mCount;
        }
    }

    private class SubInnerHolder {
        View mRoot;
        TextView subItem_tv;

        public SubInnerHolder(ViewGroup parent) {
            initViews(parent);
        }

        private void initViews(ViewGroup parent) {
            mRoot = getLayoutInflater().inflate(R.layout.subitem_iv_tv,parent,false);
            subItem_tv = (TextView) mRoot.findViewById(R.id.subItem_tv);
        }

        void setData(String data){
            subItem_tv.setText(data);
        }
    }
}
