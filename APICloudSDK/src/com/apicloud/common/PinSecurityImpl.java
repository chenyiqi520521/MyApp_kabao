package com.apicloud.common;


/**
 * 
 * ClassName: PinSecurityImpl <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2015-4-24 上午11:46:15 <br/>
 * blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @author zhuxiaohao
 * @version   加密抑或算法实现类
 * @since JDK 1.6
 */
public class PinSecurityImpl implements PinSecurity{

	@Override
	public String desSecurity(String acctNo,String Password,String PINKey) {
		String DES = null;
		int i = acctNo.length() - 13;
		String PAN = "0000" + acctNo.substring(i, i + 12);
		String PIN = "06" + Password + "FFFFFFFF";
		/**
		 * 异或算法
		 */
		String XOR = MacGenerate.xOr(PAN, PIN);
		/**
		 * DES 加密 encryption
		 */
		System.out.println(XOR);
		DES = MacGenerate.encryption(XOR, PINKey);
		return DES;
	}

}
