/**   
 * Copyright © 2015 公司名. All rights reserved.
 * 
 * @Title: BankListAdapter.java 
 * @Prject: APICloudSDK
 * @Package: com.apicloud.activity 
 * @Description: TODO
 * @author: user20   
 * @date: 2015年8月4日 下午1:37:35 
 * @version: V1.0   
 */
package com.apicloud.activity;

import java.util.ArrayList;













import java.util.HashMap;

import com.apicloud.module.BankBean;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;









import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** 
 * @ClassName: BankListAdapter 
 * @Description: TODO
 * @author: user20
 * @date: 2015年8月4日 下午1:37:35  
 */
public class BankListAdapter extends BaseAdapter {
	
	private Activity mContext;
	private LayoutInflater mInflater;
	private ArrayList<BankBean> bankListData=null;
	private String from_search;
	public static HashMap<String ,BankBean> check_map=new HashMap<String, BankBean>();
	public BankListAdapter(Activity mContext,ArrayList<BankBean> bankListData,String from_search) {
		// TODO Auto-generated constructor stub
		this.mContext=mContext;
		this.bankListData=bankListData;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
		this.from_search=from_search;
	}

	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return bankListData.size();
	}

	
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return bankListData.get(position);
	}

	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BankBean bankName=bankListData.get(position);
		 ViewHolder vh=null;
	     if(convertView==null){
	    	 vh=new ViewHolder();
	    	 convertView=mInflater.inflate(UZResourcesIDFinder.getResLayoutID("bank_list_item"),null);
	    	 vh.tv_bank=(TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("tv_bank"));
	    	 vh.ll_item=(LinearLayout) convertView.findViewById(UZResourcesIDFinder.getResIdID("ll_item"));
	    	 convertView.setTag(vh);
	     }else{
	    	 vh= (ViewHolder) convertView.getTag();
	     }
	     
	     if(bankName!=null&&vh.tv_bank!=null){
	    	 vh.tv_bank.setText(bankName.getName());
	     }
	     vh.ll_item.setOnClickListener(new itemClickListener(bankName, from_search, mContext));
		return convertView;
	}
	
	class itemClickListener implements OnClickListener{
       private BankBean bb;
       private String from_search;
       private Activity mContext;
		public itemClickListener(BankBean bb,String from_search,Activity mContext) {
			this.bb=bb;
			this.from_search=from_search;
			this.mContext=mContext;
		}
		@Override
		public void onClick(View v) {
			 Intent it=new Intent(mContext,AddCradActivity.class);
			 Bundle data=new Bundle();
			 data.putSerializable("bank_data", bb);
			 it.putExtras(data);
			 mContext.setResult(0, it);
			 mContext.finish();
			/* if(from_search.equals(AddCradActivity.CHOSE_PARENT)){
				
			 }else{
				 
			 }*/
			
		}
		
	}
	 public static class ViewHolder{
			public LinearLayout ll_item;
			public TextView tv_bank;
	}
	 
	 public void AddData(ArrayList<BankBean> moreListData){
		 if(moreListData!=null&&moreListData.size()>0){
			 for(int i=0;i<moreListData.size();i++){
				 final BankBean item=moreListData.get(i);
				 if(!check_map.containsKey(item.getId())){
					 bankListData.add(item);
					 check_map.put(item.getId(),item);
				 }else{
					 BankListActivity.handler.post(new Runnable(){

						@Override
						public void run() {
						    Toast.makeText(mContext, item.getId()+"", Toast.LENGTH_SHORT).show();
							
						}
						 
					 });
				 }
				
			 }
		 }
	 }
}
