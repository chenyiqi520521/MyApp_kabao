package com.apicloud.common;

/**
 * 
 * ClassName: PinSecurity <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2015-4-24 上午11:46:36 <br/>
 * blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @author zhuxiaohao
 * @version  加密运算接口
 * @since JDK 1.6
 */
public interface PinSecurity {

	public String desSecurity(String accNo,String Password,String Pinkey);
}
