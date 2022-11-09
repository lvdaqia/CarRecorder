package com.example.dell.carrecorder.adapter;

/**
 * Created by Administrator on 2021/5/10.
 */

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.administrator.mocam.R;
import com.example.dell.carrecorder.bean.TitleBean;

import java.util.List;

public class TitleAdapter extends BaseAdapter {
    private List<TitleBean> mBeans;
    private Context ctx;
    private LayoutInflater inflater;
    private static final int inputOver = 1;
    private UpdateSetting updateSetting;
    private Handler handler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);

            switch (msg.what) {
                case inputOver:
//                    callback();
                    break;
            }
        }
    };

    public TitleAdapter(Context context, List<TitleBean> mBeans,UpdateSetting updateSetting) {
        this.ctx = context;
        this.mBeans = mBeans;
        this.updateSetting = updateSetting;
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
    public TitleBean getItem(int position) {
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
        convertView = inflater.inflate(R.layout.setting_list_item, null, false);
        holder = new mViewHolder();
        convertView.setTag(holder);
        holder.value = (EditText) convertView.findViewById(R.id.value);
        holder.value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("onTextChanged", s.toString());
                mBeans.get(position).setValue(s.toString());
                if(s.toString().length()>0&&!s.toString().equalsIgnoreCase(""))
                updateSetting.onStateChange(mBeans,position);
            }
        });
        holder.name = (TextView) convertView.findViewById(R.id.title);
        initViewDta(holder, position);
        return convertView;
    }

    public void initViewDta(mViewHolder mholder, int postioin) {
        Log.e("initViewDta", mBeans.get(postioin).getName());
        Log.e("initViewDta2", mBeans.get(postioin).getValue());
        mholder.name.setText(mBeans.get(postioin).getName());
        mholder.value.setText(mBeans.get(postioin).getValue());

    }


    public class mViewHolder {
        private TextView name;
        private EditText value;
    }

    public interface UpdateSetting {
      void  onStateChange(List<TitleBean> mBeans,int postion);
    }
}
