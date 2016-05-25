package com.apicloud.index;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class ChoiceItemView extends LinearLayout implements Checkable{
	private Context c;
	private View view;
	private View v_line;
	private ImageView iv_icon;
	private TextView tv_way;
	private CheckBox cb_radio;
	private LinearLayout item_lay;
	public ChoiceItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.c=context;
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(UZResourcesIDFinder.getResLayoutID("list_item_paystyle"),this,true);
		iv_icon = (ImageView) v.findViewById(UZResourcesIDFinder.getResIdID("item_iv_icon"));
		tv_way = (TextView) v.findViewById(UZResourcesIDFinder.getResIdID("item_tv_way"));
		cb_radio = (CheckBox) v.findViewById(UZResourcesIDFinder.getResIdID("item_cb_radio"));
		v_line = v.findViewById(UZResourcesIDFinder.getResIdID("item_v_line"));
		item_lay = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("top_layout_item"));
	}
	public void clearFocus() {
		item_lay.setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
	}

	public void getFocus() {
		item_lay.setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
	}
	public void setLineVisible() {
		v_line.setVisibility(View.VISIBLE);
	}

	public void setLineGone() {
		v_line.setVisibility(View.GONE);
	}
	public void setIcon(String path){
		Picasso.with(c).load(path).into(iv_icon);
	}
	public void setName(String text){
		tv_way.setText(text);
	}
	@Override
	public void setChecked(boolean checked) {
		cb_radio.setChecked(checked);
		if(checked){
			cb_radio.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("xuanzhong"));
		}else{
			cb_radio.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("weixuanzhong"));
		}
	}

	@Override
	public boolean isChecked() {
		
		return cb_radio.isChecked();
	}

	@Override
	public void toggle() {
		cb_radio.toggle();
		
	}
	
}
