package com.apicloud.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class IdConfirmActivity extends Activity implements OnClickListener{
	private ImageView iv_return;
	private TextView tv_recommCode;
	private EditText et_realName;
	private EditText et_idNo;
	private ImageView iv_idCard;
	private ImageView iv_handIdCard;
	private ImageView iv_driver;
	private Button btn_ok;
	SlidePopWindow spw;
	public static final int REQUEST_CAMERA = 100;
	public static final int REQUEST_PHOTO = 101;
	public static final int FROM_ID = 102;
	public static final int FROM_HANDID = 103;
	public static final int FROM_DRIVER = 104;
	int fromWhere=-1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("id_confirm"));
		initView();
		
	}
	private void initView(){
		iv_return  = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_return"));
		tv_recommCode = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_recommCode"));
		et_realName = (EditText) findViewById(UZResourcesIDFinder.getResIdID("et_realName"));
		et_idNo = (EditText) findViewById(UZResourcesIDFinder.getResIdID("et_idNo"));
		iv_idCard = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_idCard"));
		iv_handIdCard = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_handIdCard"));
		iv_driver = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_driver"));
		btn_ok = (Button) findViewById(UZResourcesIDFinder.getResIdID("btn_ok"));
		iv_return.setOnClickListener(this);
		iv_idCard.setOnClickListener(this);
		iv_handIdCard.setOnClickListener(this);
		iv_driver.setOnClickListener(this);
		btn_ok.setOnClickListener(this);
		spw = new SlidePopWindow(IdConfirmActivity.this,itemOnClick);
	}
	@Override
	public void onClick(View v) {
		if(v==iv_return){
			finish();
		}
		if(v==iv_idCard){
			 
			spw.showAtLocation(IdConfirmActivity.this.findViewById(UZResourcesIDFinder.getResIdID("iv_idCard")), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
			backgroundAlpha(0.7f);
			spw.setOnDismissListener(new poponDismissListener());
			
			fromWhere =FROM_ID;
			//设置背景透明度
//			WindowManager.LayoutParams lp = getWindow().getAttributes();
//			lp.alpha=0.7f;
//			getWindow().setAttributes(lp);
		}
		if(v==iv_handIdCard){
			spw.showAtLocation(IdConfirmActivity.this.findViewById(UZResourcesIDFinder.getResIdID("iv_handIdCard")), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
			backgroundAlpha(0.7f);
			spw.setOnDismissListener(new poponDismissListener());
			
			fromWhere =FROM_HANDID;
		}
		if(v==iv_driver){
			spw.showAtLocation(IdConfirmActivity.this.findViewById(UZResourcesIDFinder.getResIdID("iv_driver")), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
			backgroundAlpha(0.7f);
			spw.setOnDismissListener(new poponDismissListener());
			fromWhere = FROM_DRIVER;
		}
		if(v==btn_ok){
			
		}
	}
	private OnClickListener itemOnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
		  if(v==spw.btn_camera&&fromWhere==FROM_ID){
			  Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			  startActivityForResult(intent, REQUEST_CAMERA);
			  spw.dismiss();
		  }
		  if(v==spw.btn_camera&&fromWhere==FROM_HANDID){
			  Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			  startActivityForResult(intent, REQUEST_CAMERA);
			  spw.dismiss();
		  }
		  if(v==spw.btn_camera&&fromWhere==FROM_DRIVER){
			  Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			  startActivityForResult(intent, REQUEST_CAMERA);
			  spw.dismiss();
		  }
		  if(v==spw.btn_photo&&fromWhere==FROM_ID){
			  Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			  intent.addCategory(Intent.CATEGORY_OPENABLE);
			  intent.setType("image/*");
			  startActivityForResult(intent, REQUEST_PHOTO);
			  spw.dismiss();
		  }
		  if(v==spw.btn_photo&&fromWhere==FROM_HANDID){
			  Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			  intent.addCategory(Intent.CATEGORY_OPENABLE);
			  intent.setType("image/*");
			  startActivityForResult(intent, REQUEST_PHOTO);
			  spw.dismiss();
		  }
		  if(v==spw.btn_photo&&fromWhere==FROM_DRIVER){
			  Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			  intent.addCategory(Intent.CATEGORY_OPENABLE);
			  intent.setType("image/*");
			  startActivityForResult(intent, REQUEST_PHOTO);
			  spw.dismiss();
		  }
		}
	};
	/**
	 * 设置添加屏幕的背景透明度
	 * @param bgAlpha
	 */
	public void backgroundAlpha(float bgAlpha){
		WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
	}
	class poponDismissListener implements SlidePopWindow.OnDismissListener{

		@Override
		public void onDismiss() {
			// TODO Auto-generated method stub
			
			backgroundAlpha(1f);
		}
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==REQUEST_CAMERA&&fromWhere==FROM_ID){
			Bitmap bitmap = data.getParcelableExtra("data");
			iv_idCard.setImageBitmap(bitmap);
		}
		if(requestCode==REQUEST_CAMERA&&fromWhere==FROM_HANDID){
			Bitmap bitmap = data.getParcelableExtra("data");
			iv_handIdCard.setImageBitmap(bitmap);
		}
		if(requestCode==REQUEST_CAMERA&&fromWhere==FROM_DRIVER){
			Bitmap bitmap = data.getParcelableExtra("data");
			iv_driver.setImageBitmap(bitmap);
		}
		if(requestCode==REQUEST_PHOTO&&fromWhere==FROM_ID){
			Uri uri = data.getData();
			String[] filePathColumn={MediaStore.Images.Media.DATA};
			//从系统表中查询指定uri对应的照片
			Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			//获取照片路径
			String picturePath = cursor.getString(columnIndex);
			Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
			iv_idCard.setImageBitmap(bitmap);
		}
		if(requestCode==REQUEST_PHOTO&&fromWhere==FROM_HANDID){
			Uri uri = data.getData();
			String[] filePathColumn={MediaStore.Images.Media.DATA};
			//从系统表中查询指定uri对应的照片
			Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			//获取照片路径
			String picturePath = cursor.getString(columnIndex);
			Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
			iv_handIdCard.setImageBitmap(bitmap);
		}
		if(requestCode==REQUEST_PHOTO&&fromWhere==FROM_DRIVER){
			Uri uri = data.getData();
			String[] filePathColumn={MediaStore.Images.Media.DATA};
			//从系统表中查询指定uri对应的照片
			Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			Log.v("系统表查询uri对应的照片索引", columnIndex+"");
			//获取照片路径
			String picturePath = cursor.getString(columnIndex);
			Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
			iv_driver.setImageBitmap(bitmap);
		}
	}
}
