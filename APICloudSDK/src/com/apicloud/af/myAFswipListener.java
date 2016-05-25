package com.apicloud.af;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.anfu.anf01.lib.bluetooth4.AfBleDevice;
import com.anfu.anf01.lib.inter.AFSwiperControllerListener;

public class myAFswipListener implements AFSwiperControllerListener{
	
	private Handler handler;
	private Context mContext;
	
	public static final int ERROR=-1;
	public static final int CONNECTED_SUCCESS=200;
	
	public static final int GET_DEVICE_INFO_SUCCESS=201;
	public static final int GET_CARD_INFO_SUCCESS=202;
	public static final int CONNECTED_FAILED=203;
	public static final int WAITE_SWIP=204;
	
	 public myAFswipListener(Handler handler,Context mContext) {
		this.handler=handler;
		this.mContext=mContext;
	}

	@Override
	public void onDeviceConnected() {
		
		Message msg=handler.obtainMessage();
		msg.what=CONNECTED_SUCCESS;
		handler.sendMessage(msg);
	}

	@Override
	public void onDeviceConnectedFailed() {
		Log.v("af1", "connectedfialed->");
		Message msg=handler.obtainMessage();
		msg.what=CONNECTED_FAILED;
		handler.sendMessage(msg);
	}

	@Override
	public void onDeviceDisconnected() {
		Log.v("af1", "onDeviceDisconnected->");
		
	}

	@Override
	public void onDeviceListRefresh(List<AfBleDevice> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeviceScanStopped() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeviceScanning() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(int arg0) {
		Message msg=handler.obtainMessage();
		msg.obj=arg0+"";
		msg.what=ERROR;
		handler.sendMessage(msg);
		
	}

	@Override
	public void onNeedInsertICCard() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReturnCardInfo(Map<String, String> map) {
		Message msg=handler.obtainMessage();
		msg.what=GET_CARD_INFO_SUCCESS;
		msg.obj=map;
		handler.sendMessage(msg);
		
		
	}

	@Override
	public void onReturnDeviceInfo(Map<String, String> map) {
		// TODO Auto-generated method stub
		Message msg=handler.obtainMessage();
		msg.what=GET_DEVICE_INFO_SUCCESS;
		msg.obj=map;
		handler.sendMessage(msg);
		
	}

	@Override
	public void onTimeout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWaitingForCardSwipe() {
		Message msg=handler.obtainMessage();
		msg.what=WAITE_SWIP;
		
		handler.sendMessage(msg);
		
	}

	@Override
	public void onWaitingForDevice() {
		Log.v("af1", "onWaitingForDevice->");
		
	}

}
