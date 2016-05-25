package com.apicloud.view;

import com.apicloud.util.LibImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChoiceListItemView extends LinearLayout implements Checkable {

	private ImageView iv_icon;
	private TextView tv_way;
	private CheckBox cb_radio;
	private View v_line;
	private EditText et_code;
	private LinearLayout layout_item;
	private Context c;

	public ChoiceListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.c = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(UZResourcesIDFinder.getResLayoutID("list_item_for_paystyle2"), this, true);
		iv_icon = (ImageView) v.findViewById(UZResourcesIDFinder.getResIdID("iv_icon"));
		tv_way = (TextView) v.findViewById(UZResourcesIDFinder.getResIdID("tv_way"));
		cb_radio = (CheckBox) v.findViewById(UZResourcesIDFinder.getResIdID("cb_radio"));
		et_code = (EditText) v.findViewById(UZResourcesIDFinder.getResIdID("et_code"));
		v_line = v.findViewById(UZResourcesIDFinder.getResIdID("v_line"));
		layout_item = (LinearLayout) v.findViewById(UZResourcesIDFinder.getResIdID("layout_item"));
	}

	public void setIcon(String img) {
		//LibImageLoader.instance().loadImg(img, iv_icon);
		Picasso.with(c).load(img).into(iv_icon);
		// ImageLoader.getInstance().displayImage(img, iv_icon);
		// iv_icon.setImageResource(img);
	}

	public void setName(String text) {
		tv_way.setText(text);
	}

	public void setLineVisible() {
		v_line.setVisibility(View.VISIBLE);
	}

	public void setLineGone() {
		v_line.setVisibility(View.GONE);
	}

	@Override
	public boolean isChecked() {
		return cb_radio.isChecked();
	}

	public void clearFocus() {
		layout_item.setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
	}

	public void getFocus() {
		layout_item.setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
	}

	@Override
	public void setChecked(boolean checked) {
		cb_radio.setChecked(checked);
		if (checked) {
			cb_radio.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("xuanzhong"));
		} else {
			cb_radio.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("weixuanzhong"));
		}
	}

	@Override
	public void toggle() {
		cb_radio.toggle();
	}

}
