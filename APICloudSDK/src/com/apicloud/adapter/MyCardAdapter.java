package com.apicloud.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.apicloud.module.NewCardBean;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class MyCardAdapter extends BaseAdapter{
	Context context;
	ArrayList<NewCardBean> cardList;
	LayoutInflater inflater;
	ViewHolder viewHolder; 
	public MyCardAdapter(Context context,ArrayList<NewCardBean> cardList){
		this.context=context;
		this.cardList=cardList;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return cardList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return cardList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		NewCardBean ncb = cardList.get(position);
	    viewHolder = new ViewHolder();
	    if(convertView==null){
	    	convertView = inflater.inflate(UZResourcesIDFinder.getResIdID("cardlist_item"), null);
	    	viewHolder.iv = (ImageView) convertView.findViewById(UZResourcesIDFinder.getResIdID("cardlist_iv"));
	    	viewHolder.bankName = (TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("cardlist_bankname"));
	    	viewHolder.bankNo= (TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("cardlist_bankno"));
	    	viewHolder.mainCard = (TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("cardlist_main"));
	    	viewHolder.delete = (TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("cardlist_delete"));
	    	convertView.setTag(viewHolder);
	    }else{
	    	viewHolder = (ViewHolder) convertView.getTag();
	    }
	    //获得数据填入ncb viewHolder.bankName.setText(ncb.name);.....
		return convertView;
	}
	class ViewHolder{
		ImageView iv;
		TextView bankName;
		TextView bankNo;
		TextView mainCard;
		TextView delete;
	}
}
