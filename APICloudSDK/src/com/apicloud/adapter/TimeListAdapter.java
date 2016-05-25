package com.apicloud.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apicloud.activity.timely.TimelyAccountTopUpActivity;
import com.apicloud.module.BluetoothDeviceContext;
import com.squareup.picasso.Picasso;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimeListAdapter extends BaseAdapter {
	
	private Activity mContext;
	private LayoutInflater mInflater;
	private List<Map<String, String>> items;
	public TimeListAdapter(Activity mContext,List<Map<String, String>> items) {
		this.mContext=mContext;
		this.items=items;
		Log.v("param2", "adapter-->"+items.size());
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HashMap<String, String> map = (HashMap<String, String>) items.get(position);
		convertView= mInflater.inflate(UZResourcesIDFinder.getResLayoutID("item_card"), null);
		ImageView iv=(ImageView) convertView.findViewById(UZResourcesIDFinder.getResIdID("iv_crad"));
		TextView tv_name=(TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("txt_crad_name"));
		TextView tv_cardno=(TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("txt_crad_number"));
		
		Picasso.with(mContext).load(items.get(position).get("card_img")).into(iv);
		tv_name.setText(items.get(position).get("uname"));
		tv_cardno.setText(items.get(position).get("card_no"));
		return convertView;
	}
	
}
	

