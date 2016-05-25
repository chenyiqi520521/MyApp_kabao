/**   
 * Copyright © 2015 公司名. All rights reserved.
 * 
 * @Title: BluetoothDeviceContext.java 
 * @Prject: APICloudSDK
 * @Package: com.apicloud.module 
 * @Description: TODO
 * @author: user20   
 * @date: 2015年8月11日 上午10:27:26 
 * @version: V1.0   
 */
package com.apicloud.module;

import java.io.Serializable;

/** 
 * @ClassName: BluetoothDeviceContext 
 * @Description: TODO
 * @author: user20
 * @date: 2015年8月11日 上午10:27:26  
 */
public class BluetoothDeviceContext implements Serializable{
	public String name = "";
	public String address = "";

	public BluetoothDeviceContext(String name, String address) {
		this.name = name;
		this.address = address;
	}

}
