package com.apicloud.module;

import java.io.Serializable;

public class LoginBean implements Serializable{
	public String msg;
	public String error;
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	
}
