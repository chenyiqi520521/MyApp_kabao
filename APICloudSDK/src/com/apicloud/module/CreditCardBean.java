/**
 * Project Name:CardPay
 * File Name:CreditCardBean.java
 * Package Name:com.apicloud.module
 * Date:2015-4-27上午10:09:07
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.module;

/**
 * ClassName:CreditCardBean <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-27 上午10:09:07 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see 请求数据封装
 */
public class CreditCardBean {
	/** 021：磁条卡 051：IC卡 */
	public String pointService = "";
	/** 主账户（卡号 这里尽量用银行接口名称） */
	public String acctNo = "";
	/** 交易金额 以分为单位 */
	public String transAmt = "";
	/** 加密的磁道2、3数据（磁条卡为扩展信息从16位之后的，IC卡为二磁道信息从16位之后的） */
	public String trackdatas = "";
	
	/** 密码（卡号异或后DES加密） */
	public String pin = "";
	/*加密前的密码*/
	public String password="";
	/** 信用卡卡号 */
	public String credit = "";
	/** 55域 */
	public String ic = "";
	/** 序列号 */
	public String cardEXPDate = "";
	/** 卡序列号 */
	public String cardSN = "";
	/** 设备序列号csn */
	public String ksn = "";
	/** 磁条卡为扩展信息前16位，IC卡为二磁道信息前16位 */
	public String encWorkingKey = "";
	/** 转入银行卡号 */
	public String cardNo = "";
	/** 运营商编号：01 移动 02 联通 03 电信 */
	public String operator = "";
	/** 充值号码 */
	public String mobile = "";
	/** 交易流水号 */
	public String tranxSN = "";
	/** A:固定扣率，B为封顶扣率 */
	public String bizCode = "A";
	/** 用户 ID */
	public String lkey = "";
	/** 0是转账,1是即时到帐**/
	public String type="";
	
	/*二磁道和三磁道*/
	public String acctNoT2="";
	public String acctNoT3="";
	
	public String gps="";
	
	
	/**
	 * 只有转账汇款用到的字段
	 */
	public String idCard="";
	public String userName="";
	
	/**快联用到的字段**/
	public String kuailian_username="";
	public String kuailian_pwd="";

}
