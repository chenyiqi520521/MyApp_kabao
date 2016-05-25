/**   
 * Copyright © 2015 公司名. All rights reserved.
 * 
 * @Title: BankBean.java 
 * @Prject: APICloudSDK
 * @Package: com.apicloud.module 
 * @Description: TODO
 * @author: user20   
 * @date: 2015年8月4日 下午3:03:09 
 * @version: V1.0   
 */
package com.apicloud.module;

import java.io.Serializable;

/** 
 * @ClassName: 添加银行卡的开户银行选择
 * @Description: TODO
 * @author: user20
 * @date: 2015年8月4日 下午3:03:09  
 */
public class BankBean implements Serializable{
	
	public BankBean(){
		
	}
	
	private String id;
	private String name;
	private String code;
	private String childrenBankCode;
	/**
	 * @return the childrenBankCode
	 */
	public String getChildrenBankCode() {
		return childrenBankCode;
	}
	/**
	 * @param childrenBankCode the childrenBankCode to set
	 */
	public void setChildrenBankCode(String childrenBankCode) {
		this.childrenBankCode = childrenBankCode;
	}
	/**
	 * @return the provinceCode
	 */
	public String getProvinceCode() {
		return provinceCode;
	}
	/**
	 * @param provinceCode the provinceCode to set
	 */
	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

	private String provinceCode;
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	
	

}
