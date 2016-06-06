package com.apicloud.index;

import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class MyCardAdapter extends BaseAdapter {
	Context mContext;
	List<Map<String, String>> cardList;
	LayoutInflater inflater;
	ViewHolder viewHolder;
	Handler handler;
	String cardNo;
	int del = 5;

	public MyCardAdapter(Context context, List<Map<String, String>> cardList, Handler handler) {
		this.mContext = context;
		this.cardList = cardList;
		this.handler = handler;
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
		Map<String, String> map = cardList.get(position);

		viewHolder = new ViewHolder();

		if (convertView == null) {
			convertView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("cardlist_item"), null);
			viewHolder.iv = (ImageView) convertView.findViewById(UZResourcesIDFinder.getResIdID("cardlist_iv"));
			viewHolder.bankName = (TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("cardlist_bankname"));
			viewHolder.bankNo = (TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("cardlist_bankno"));
			viewHolder.mainCard = (TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("cardlist_main"));
			viewHolder.delete = (TextView) convertView.findViewById(UZResourcesIDFinder.getResIdID("cardlist_delete"));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Picasso.with(mContext).load(cardList.get(position).get("card_img")).into(viewHolder.iv);
		viewHolder.bankName.setText(map.get("card_name"));

		String temp = map.get("card_num");
		String number = temp.substring(0, 5) + "*******" + temp.substring(12, temp.length());
		viewHolder.bankNo.setText(number);
		if (map.get("main_card").equals("1")) {
			viewHolder.mainCard.setText("主卡");
			viewHolder.mainCard.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("bg_tv_maincard"));
			viewHolder.mainCard.setEnabled(false);
		} else {
			viewHolder.mainCard.setText("设为主卡");
			viewHolder.mainCard.setBackgroundColor(Color.WHITE);
		}
		cardNo = map.get("card_id");
		Log.e("ViewPosition", "值是-click1->" + position);
		viewHolder.mainCard.setOnClickListener(new Listener(position, cardNo));
		viewHolder.delete.setOnClickListener(new MyListener(position, cardNo));
		return convertView;
	}

	class MyListener implements OnClickListener {
		int position;
		String cardNo;

		public MyListener(int position, String cardNo) {
			this.position = position;
			this.cardNo = cardNo;
		}

		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage("提示");
			builder.setMessage("你确定删除吗");
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Message msg = Message.obtain();
					msg.what = position;
					msg.obj = cardNo;
					msg.arg1 = del;
					handler.sendMessage(msg);

				}
			});
			builder.setNegativeButton("取消", null);
			builder.create();
			builder.show();

		}

	}

	class ViewHolder {
		ImageView iv;
		TextView bankName;
		TextView bankNo;
		TextView mainCard;
		TextView delete;
	}

	@Override
	public int getItemViewType(int position) {

		return super.getItemViewType(position);
	}

	@Override
	public void notifyDataSetChanged() {

		super.notifyDataSetChanged();
	}

	class Listener implements OnClickListener {

		int position;
		String cardNo;

		public Listener(int position, String cardNo) {
			this.position = position;
			this.cardNo = cardNo;

		}

		@Override
		public void onClick(View v) {

			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle("提示");
			builder.setMessage("你确定设定为主卡吗");
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.e("ViewPosition", "值是-click->" + position);
					Message msg = handler.obtainMessage();
					msg.what = position;
					msg.obj = cardNo;
					handler.sendMessage(msg);

				}
			});
			builder.setNegativeButton("取消", null);
			builder.create();
			builder.show();

		}

	}

}
