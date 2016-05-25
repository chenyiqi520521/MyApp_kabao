//package com.apicloud.view;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.app.Dialog;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Bitmap.Config;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Path;
//import android.os.Bundle;
//import android.view.Display;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager.LayoutParams;
//import android.widget.Button;
//import android.widget.FrameLayout;
//
//import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
//
///**
// * 签名 对话框
// * 
// * @author chenhao
// * 
// */
//public class WritePadDialog extends Dialog {
//
//	/** 上下文 */
//	Context context;
//	LayoutParams p;
//	DialogListener dialogListener;
//
//	public WritePadDialog(Context context, DialogListener dialogListener) {
//		super(context);
//		this.context = context;
//		this.dialogListener = dialogListener;
//	}
//
//	static final int BACKGROUND_COLOR = Color.WHITE;
//
//	static final int BRUSH_COLOR = Color.BLACK;
//
//	PaintView mView;
//	FrameLayout frameLayout;
//	Button btnClear;
//	Button btnOk;
//	Button btnCancel;
//	/** The index of the current color to use. */
//	int mColorIndex;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		requestWindowFeature(Window.FEATURE_PROGRESS);
//		int layoutId = UZResourcesIDFinder.getResLayoutID("write_pad");
//		if (layoutId > 0) {
//			setContentView(layoutId);
//		}
//		// setContentView(R.layout.write_pad);
//		p = getWindow().getAttributes();
//		Display mDisplay = ((Activity) context).getWindowManager()
//				.getDefaultDisplay();
//		p.height = mDisplay.getHeight();
//		p.width = mDisplay.getWidth();
//		getWindow().setAttributes(p);
//
//		mView = new PaintView(context);
//		int frameLayouts = UZResourcesIDFinder.getResIdID("tablet_view");
//		if (frameLayouts > 0) {
//			frameLayout = (FrameLayout) findViewById(frameLayouts);
//			frameLayout.addView(mView);
//		}
//		mView.requestFocus();
//
//		int btnClears = UZResourcesIDFinder.getResIdID("tablet_clear");
//		if (btnClears > 0) {
//			btnClear = (Button) findViewById(btnClears);
//		}
//		btnClear.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				mView.clear();
//			}
//		});
//
//		int btnOks = UZResourcesIDFinder.getResIdID("tablet_ok");
//		if (btnOks > 0) {
//			btnOk = (Button) findViewById(btnOks);
//		}
//		btnOk.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				try {
//					dialogListener.refreshActivity(mView.getCachebBitmap());
//					WritePadDialog.this.dismiss();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//
//		int btnCancels = UZResourcesIDFinder.getResIdID("tablet_cancel");
//		if (btnCancels > 0) {
//			btnCancel = (Button) findViewById(btnCancels);
//		}
//		btnCancel.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				cancel();
//			}
//		});
//	}
//
//	/**
//	 * 这个视图实现绘图画布。
//	 * 
//	 * 它处理所有的输入事件和绘图功能。
//	 */
//	class PaintView extends View {
//		private Paint paint;
//		private Canvas cacheCanvas;
//		private Bitmap cachebBitmap;
//		private Path path;
//
//		public Bitmap getCachebBitmap() {
//			return cachebBitmap;
//		}
//
//		public PaintView(Context context) {
//			super(context);
//			init();
//		}
//
//		private void init() {
//			paint = new Paint();
//			paint.setAntiAlias(true);
//			paint.setStrokeWidth(3);
//			paint.setStyle(Paint.Style.STROKE);
//			paint.setColor(Color.BLACK);
//			path = new Path();
//			cachebBitmap = Bitmap.createBitmap(p.width, (int) (p.height * 0.8),
//					Config.ARGB_8888);
//			cacheCanvas = new Canvas(cachebBitmap);
//			cacheCanvas.drawColor(Color.WHITE);
//		}
//
//		public void clear() {
//			if (cacheCanvas != null) {
//				paint.setColor(BACKGROUND_COLOR);
//				cacheCanvas.drawPaint(paint);
//				paint.setColor(Color.BLACK);
//				cacheCanvas.drawColor(Color.WHITE);
//				invalidate();
//			}
//		}
//
//		@Override
//		protected void onDraw(Canvas canvas) {
//			// canvas.drawColor(BRUSH_COLOR);
//			canvas.drawBitmap(cachebBitmap, 0, 0, null);
//			canvas.drawPath(path, paint);
//		}
//
//		@Override
//		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//			int curW = cachebBitmap != null ? cachebBitmap.getWidth() : 0;
//			int curH = cachebBitmap != null ? cachebBitmap.getHeight() : 0;
//			if (curW >= w && curH >= h) {
//				return;
//			}
//			if (curW < w)
//				curW = w;
//			if (curH < h)
//				curH = h;
//			Bitmap newBitmap = Bitmap.createBitmap(curW, curH,
//					Bitmap.Config.ARGB_8888);
//			Canvas newCanvas = new Canvas();
//			newCanvas.setBitmap(newBitmap);
//			if (cachebBitmap != null) {
//				newCanvas.drawBitmap(cachebBitmap, 0, 0, null);
//			}
//			cachebBitmap = newBitmap;
//			cacheCanvas = newCanvas;
//		}
//
//		private float cur_x, cur_y;
//
//		@SuppressLint("ClickableViewAccessibility")
//		@Override
//		public boolean onTouchEvent(MotionEvent event) {
//			float x = event.getX();
//			float y = event.getY();
//			switch (event.getAction()) {
//			case MotionEvent.ACTION_DOWN: {
//				cur_x = x;
//				cur_y = y;
//				path.moveTo(cur_x, cur_y);
//				break;
//			}
//			case MotionEvent.ACTION_MOVE: {
//				path.quadTo(cur_x, cur_y, x, y);
//				cur_x = x;
//				cur_y = y;
//				break;
//			}
//			case MotionEvent.ACTION_UP: {
//				cacheCanvas.drawPath(path, paint);
//				path.reset();
//				break;
//			}
//			}
//			invalidate();
//			return true;
//		}
//	}
//
//}
