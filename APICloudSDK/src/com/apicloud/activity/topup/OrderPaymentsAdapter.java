package com.apicloud.activity.topup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apicloud.controller.Controller;
import com.apicloud.util.LibImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OrderPaymentsAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<Map<String, Object>> payList;
	private List<Integer> mPos = new ArrayList<Integer>();
	Controller controller;
	Handler handler = new Handler();
	private int index = -1;
	private ViewGroup v;

	public OrderPaymentsAdapter(Context c, List<Map<String, Object>> list, Controller controller) {
		this.context = c;
		this.payList = list;
		this.controller = controller;
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
	public View getView(final int arg0, View arg1, final ViewGroup arg2) {
		final ViewHolder holder;
		HashMap<String, Object> map = (HashMap<String, Object>) payList.get(arg0);
		if (arg1 == null) {
			holder = new ViewHolder();
			arg1 = inflater.inflate(UZResourcesIDFinder.getResLayoutID("list_item_for_paystyle"), null, false);
			v = arg2;
			holder.iv_icon = (ImageView) arg1.findViewById(UZResourcesIDFinder.getResIdID("iv_icon"));
			holder.tv_way = (TextView) arg1.findViewById(UZResourcesIDFinder.getResIdID("tv_way"));
			holder.cb_radio = (CheckBox) arg1.findViewById(UZResourcesIDFinder.getResIdID("cb_radio"));
			holder.et_code = (EditText) arg1.findViewById(UZResourcesIDFinder.getResIdID("et_code"));
			holder.tv_code = (TextView) arg1.findViewById(UZResourcesIDFinder.getResIdID("tv_code"));
			holder.v_line = arg1.findViewById(UZResourcesIDFinder.getResIdID("v_line"));
			holder.layout_item = (LinearLayout) arg1.findViewById(UZResourcesIDFinder.getResIdID("layout_item"));
			holder.layout_code = (LinearLayout) arg1.findViewById(UZResourcesIDFinder.getResIdID("layout_code"));
			holder.btn_refresh = (Button) arg1.findViewById(UZResourcesIDFinder.getResIdID("btn_refresh"));
			arg1.setTag(holder);
		} else {
			holder = (ViewHolder) arg1.getTag();
		}

		if (arg0 == getCount() - 1)
			holder.v_line.setVisibility(View.GONE);
		Picasso.with(context).load(map.get("icon").toString()).into(holder.iv_icon);
		
		holder.tv_way.setText(map.get("payNames").toString());

		

		holder.layout_item.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (arg2.getTag(UZResourcesIDFinder.getResIdID("tag_first")) != null && arg2.getTag(UZResourcesIDFinder.getResIdID("tag_first")) != holder.cb_radio) {
					((CheckBox) (arg2.getTag(UZResourcesIDFinder.getResIdID("tag_first")))).setChecked(false);
					((LinearLayout) (arg2.getTag(UZResourcesIDFinder.getResIdID("tag_second")))).setVisibility(View.GONE);
				}
				if (holder.cb_radio.isChecked()) {
					arg2.setTag(UZResourcesIDFinder.getResIdID("tag_second"), holder.layout_code);
					arg2.setTag(UZResourcesIDFinder.getResIdID("tag_first"), holder.cb_radio);
					index = arg0;
					// mPos.add(arg0);
					ifNeedShowCode(holder, arg0, true);
				} else {
					// mPos.remove((Integer) arg0);
					index = -1;
					ifNeedShowCode(holder, arg0, false);
				}

			}
		});
		return arg1;
	}

	private void ifNeedShowCode(final ViewHolder holder, int positon, boolean isChecked) {
		if (positon == getCount() - 2) {
			v.setTag(UZResourcesIDFinder.getResIdID("tag_third"), holder.et_code);
			if (isChecked) {
				holder.layout_code.setVisibility(View.VISIBLE);
			} else {
				holder.layout_code.setVisibility(View.GONE);
			}
			holder.btn_refresh.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new Thread() {
						public void run() {
							/*final String code = controller.getCode();

							handler.post(new Runnable() {

								@Override
								public void run() {
									holder.tv_code.setText(code);
								}
							});*/

						};

					}.start();

				}
			});

		} else {
			holder.layout_code.setVisibility(View.GONE);
		}
	}

	public int getChckedPosition() {
		// if (mPos.size() <= 0) {
		// return -1;
		// }
		// return mPos.get(0);
		return index;
	}

	public EditText getSelectCodeEdit() {
		return (EditText) v.getTag(UZResourcesIDFinder.getResIdID("tag_third"));
	}

	private class ViewHolder {
		private ImageView iv_icon;
		private TextView tv_way;
		private CheckBox cb_radio;
		private View v_line;
		private EditText et_code;
		private LinearLayout layout_item;
		private LinearLayout layout_code;
		private Button btn_refresh;
		private TextView tv_code;
	}

}
