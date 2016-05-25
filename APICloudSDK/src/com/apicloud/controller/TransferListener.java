package com.apicloud.controller;

import com.newland.mtype.module.common.emv.EmvControllerListener;
import com.newland.mtype.module.common.swiper.SwipResult;

public interface TransferListener extends EmvControllerListener{
	
	public void onSwipMagneticCard(SwipResult swipRslt);
	
	public void onOpenCardreaderCanceled();
	
	

}
