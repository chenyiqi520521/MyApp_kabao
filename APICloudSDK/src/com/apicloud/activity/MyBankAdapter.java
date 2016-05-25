package com.apicloud.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apicloud.module.BankBean;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class MyBankAdapter extends BaseAdapter{
	private Activity context;
	private ArrayList<BankBean> bankList;
	private String fromWhat;
	LayoutInflater inflater;
	ViewHolder viewHolder;
	public MyBankAdapter(Activity context,ArrayList<BankBean> bankList,String fromWhat){
		this.context=context;
		this.bankList=bankList;
		this.fromWhat=fromWhat;
		inflater =(LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount() {
		
		return bankList.size();
	}

	@Override
	public Object getItem(int position) {
		
		return bankList.get(position);
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BankBean bankName = bankList.get(position);
		viewHolder = new ViewHolder();
		if(convertView==null){
			convertView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("banklist_item"), null);
			viewHolder.ll_item = (LinearLayout) convertView.findViewById(UZResourcesIDFinder.getResIdID("ll_item"));
			viewHolder.tv_bank = (TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("tv_bank"));
			
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if(bankName.getName()!=null){
			viewHolder.tv_bank.setText(bankName.getName());
		}
		viewHolder.ll_item.setOnClickListener(new ItemClickListener(context,bankName,fromWhat){});
		return convertView;
	}
	
	class ItemClickListener implements OnClickListener{
		private BankBean bb;
		private Activity context;
		private String fromSearch;
		public ItemClickListener(Activity context,BankBean bb,String fromSearch){
			this.bb=bb;
			this.context=context;
			this.fromSearch=fromWhat;
		}
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(context,AddNewCardActivity.class);
			Bundle data = new Bundle();
			data.putSerializable("bank_data", bb);
			intent.putExtras(data);
			context.setResult(0, intent);
			context.finish();
		}
		
	}
	class ViewHolder{
		LinearLayout ll_item;
		TextView tv_bank;
	}
}
