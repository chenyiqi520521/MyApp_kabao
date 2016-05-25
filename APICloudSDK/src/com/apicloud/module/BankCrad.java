/**
 * Project Name:CardPay
 * File Name:CradString.java
 * Package Name:com.apicloud.moduleDemo
 * Date:2015-4-22下午5:38:27
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.module;

/**
 * ClassName:CradString <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-22 下午5:38:27 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see IC 卡和磁条卡数据拼装
 */
public class BankCrad {
	public String acctHashId = "";
	public String ExtInfo = "";
    public String accNoT2="";//第二磁道信息
    public String accNoT3="";//第三磁道信息
	public String account = "";// 卡号
	public String acctNo = "";// 主账户（卡号 这里尽量用银行接口名称）
	public String pointService = "";// 021：磁条卡 051：IC卡
	public String transAmt = "";// 充值金额
	public String trackdatas = "";// 扩展信息
	public String pin = "";// 密码
	public String tranxSN = "";// 交易流水号
	public String ic = "";// 55域
	public String cardEXPDate = "";// 有效期
	public String cardSN = "";// 卡序列号
	public String ksn = "";// 设备序列号csn
	public String EncWorkingKey = "";// 磁条卡为扩展信息前16位，IC卡为二磁道信息前16位
	
}
