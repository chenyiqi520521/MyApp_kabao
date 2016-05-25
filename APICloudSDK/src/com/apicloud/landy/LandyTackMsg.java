package com.apicloud.landy;

import java.io.Serializable;


/**
 * 联迪磁道信息
 * @author Administrator
 *
 */
public class LandyTackMsg implements Serializable{
	public LandyTackMsg(){
		
	}
	/** 021：磁条卡 051：IC卡 */
	public String pointService="";
	public String cardNo;
	public String ksn;
	public String track1;//一磁道信息
	public String track2;//二磁道信息
	public String track3;//三磁道信息
	public String expireDate;//过期日期
	public String Data55;//55域
	public String amount;//金额
	public String enworkingKey="";
	public String password="";//密码
	public String pinBlock="";//密文
	public String cardSn;

}
