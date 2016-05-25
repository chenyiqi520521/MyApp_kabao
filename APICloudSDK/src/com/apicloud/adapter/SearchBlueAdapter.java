package com.apicloud.adapter;

import java.util.ArrayList;















import com.apicloud.module.BluetoothDeviceContext;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SearchBlueAdapter extends BaseAdapter {
	
	private Activity mContext;
	private LayoutInflater mInflater;
	private ArrayList<BluetoothDeviceContext> blueToothList=null;
	private ListView lv;
	
	public SearchBlueAdapter(Activity mContext,ArrayList<BluetoothDeviceContext> blueToothList,ListView lv) {
		this.mContext=mContext;
		this.blueToothList=blueToothList;
		this.lv=lv;
	    mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
	}
	

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return blueToothList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return blueToothList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    BluetoothDeviceContext bdc=blueToothList.get(position);
	    ViewHolder viewHolder=null;
	    if(convertView==null){
	    	viewHolder=new ViewHolder();
	    	convertView= mInflater.inflate(UZResourcesIDFinder.getResLayoutID("bluetooth_item"), null);
	    	viewHolder.ll=(LinearLayout) convertView.findViewById(UZResourcesIDFinder.getResIdID("ll"));
	    	viewHolder.tv=(TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("blt"));
	    	convertView.setTag(viewHolder);
	    	
	    }else{
	    	viewHolder=(ViewHolder) convertView.getTag();
	    }
	    
	    viewHolder.tv.setText(bdc.name);
		return convertView;
	}
	
	 public static class ViewHolder{
			
		    public LinearLayout ll;
			public TextView tv;
	}
	 
	class choseClistener implements OnClickListener{

		
		public choseClistener() {
			// TODO Auto-generated constructor stub
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
