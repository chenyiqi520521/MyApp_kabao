package com.apicloud.activity.topup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.apicloud.index.ChoiceItemView;


public class OrderPaymentsAdapter3 extends BaseAdapter{
	private Context context;
	private LayoutInflater inflater;
	private List<Map<String, Object>> payList;

	public OrderPaymentsAdapter3(Context c, List<Map<String, Object>> list) {
		this.context = c;
		this.payList = list;
		inflater = LayoutInflater.from(c);
	}

	@Override
	public int getCount() {
		return payList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return payList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		HashMap<String, Object> map = (HashMap<String, Object>) payList.get(arg0);
		Log.e("position",arg0+"");
		ChoiceItemView choiceListItemView = new ChoiceItemView(context, null);
		Log.e("得到的数据",map.get("payNames").toString());
		choiceListItemView.setIcon((map.get("icon").toString()));
		choiceListItemView.setName((map.get("payNames").toString()));
		if (arg0 == getCount() - 1) {
			choiceListItemView.setLineGone();
		} else {
			choiceListItemView.setLineVisible();
		}
		
		return choiceListItemView;
	
	}

}
