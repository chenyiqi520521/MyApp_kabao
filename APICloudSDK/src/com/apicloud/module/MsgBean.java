/**
 * Project Name:CardPay
 * File Name:MsgBean.java
 * Package Name:com.apicloud.module
 * Date:2015-4-27上午10:05:12
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.module;

import java.io.Serializable;

/**
 * ClassName:MsgBean <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-27 上午10:05:12 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see 返回对象
 */
@SuppressWarnings("serial")
public class MsgBean  implements Serializable{

	/** 银联答应码 */
	public String RspCd = "";
	/** 交易参考号 */
	public String ReferNO = "";
	/** 银联返回信息 */
	public String RspMsg = "";;
	/** 银联返回的余额 */
	public String Balance = "";
	/***/
	public String TransDate="";
	public String TransTime="";
	public String SettDate="";
	public String orderno="";//订单号
	@Override
	public int hashCode() {
		
		// TODO Auto-generated method stub
		return super.hashCode();
	}
	@Override
	public String toString() {
		
		String result="";
		if(RspCd!=null&&RspCd.length()>0){
			result=result+"rspCd-->"+RspCd;
		}
		
		if(ReferNO!=null&&ReferNO.length()>0){
			result=result+"referNo-->"+ReferNO;
		}
		
		if(RspMsg!=null&&RspMsg.length()>0){
			result=result+"RspMsp-->"+RspMsg;
		}
		
		if(TransDate!=null&&TransDate.length()>0){
			result=result+"TansDate-->"+TransDate;
		}
		if(TransTime!=null&&TransTime.length()>0){
			result=result+"TransTime-->"+TransTime;
		}
		
		if(orderno!=null&&orderno.length()>0){
			result=result+"oderno-->"+orderno;
		}
		return result;
	}
	
	

}
