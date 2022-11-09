package com.example.dell.carrecorder.adapter;

/**
 * Created by Administrator on 2021/5/10.
 */

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.administrator.mocam.R;
import com.example.dell.carrecorder.bean.MainBean;

import java.util.List;

public class MainAdapter extends BaseAdapter {
    private List<MainBean> mBeans;
    private Context ctx;
    private LayoutInflater inflater;

    public MainAdapter(Context context, List<MainBean> mBeans) {
        this.ctx = context;
        this.mBeans = mBeans;
        inflater = LayoutInflater.from(ctx);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mBeans.size();
    }

    @Override
    public MainBean getItem(int position) {
        return mBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mViewHolder holder = null;
        convertView = inflater.inflate(R.layout.item_main_adapter, null, false);
        holder = new mViewHolder();
        convertView.setTag(holder);
        holder.mitem =(LinearLayout)convertView.findViewById(R.id.mitem);
        holder.name = (TextView)convertView.findViewById(R.id.name);

        initViewDta(holder,position);
        return convertView;
    }

    public void initViewDta(mViewHolder mholder, int postioin) {
        Log.e("initViewDta",mBeans.get(postioin).getName());
        mholder.name.setText(mBeans.get(postioin).getName());
        mholder.mitem.setBackgroundColor(Color.parseColor(mBeans.get(postioin).getColor()));
}


    public class mViewHolder {
        private TextView name;
        private LinearLayout mitem;
    }

}
