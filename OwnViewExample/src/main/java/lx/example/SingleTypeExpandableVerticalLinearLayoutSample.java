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

import lx.own.view.online.SingleTypeExpandableVerticalLinearLayout;
import lx.own.view.online.tools.SingleTypeViewRecyclePool;

/**
 * <p> </p><br/>
 *
 * @author Lx
 * @date 11/06/2017
 */

public class SingleTypeExpandableVerticalLinearLayoutSample extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView listView = new ListView(this);
        setContentView(listView);
        listView.setAdapter(new InnerAdapter());
    }

    private class InnerAdapter extends BaseAdapter{
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

    private class InnerHolder implements SingleTypeExpandableVerticalLinearLayout.SingleTypeExpandableVerticalLinearLayoutAdapter {
        private Random mRandom = new Random();
        private int mCount;
        private View mRoot;
        private TextView item_tv;
        private SingleTypeExpandableVerticalLinearLayout item_expandableVerticalLinearLayout;

        public InnerHolder(ViewGroup parent,SingleTypeViewRecyclePool pool){
            mCount = mRandom.nextInt(Datas.SENTENCES.length);
            initView(parent,pool);
        }

        private void initView(ViewGroup parent, SingleTypeViewRecyclePool pool) {
            mRoot = getLayoutInflater().inflate(R.layout.item_expandable_vertical_linear_layout,parent,false);
            item_tv = (TextView) mRoot.findViewById(R.id.item_tv);
            item_expandableVerticalLinearLayout = (SingleTypeExpandableVerticalLinearLayout) mRoot.findViewById(R.id.item_expandableVerticalLinearLayout);
            item_expandableVerticalLinearLayout.setRecyclePool(pool);
            item_expandableVerticalLinearLayout.setAdapter(this);
        }

        void setData(int position){
            item_tv.setText("This is item " + position + "in ListView.");
            item_expandableVerticalLinearLayout.shrink();
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public View getView(@Nullable View convertView, int position, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.subitem_comment,parent,false);
            }
            TextView subItem_tv = (TextView) convertView.findViewById(R.id.subItem_tv);
            subItem_tv.setText(Datas.SENTENCES[position]);
            return convertView;
        }
    }
}
