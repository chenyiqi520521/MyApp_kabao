package com.apicloud.activity.topup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apicloud.view.ChoiceListItemView;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class OrderPaymentsAdapter2 extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<Map<String, Object>> payList;

	public OrderPaymentsAdapter2(Context c, List<Map<String, Object>> list) {
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
		ChoiceListItemView choiceListItemView = new ChoiceListItemView(context, null);
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
