package com.apicloud.module;

import java.io.Serializable;
import java.util.List;

public class ReChargeStyleBean implements Serializable {

	private String A_fee;
	private String B_fee;
	private String B_top;
	private List<RechargeItemBean> list;
	private String word;

	public String getA_fee() {
		return A_fee;
	}

	public void setA_fee(String a_fee) {
		A_fee = a_fee;
	}

	public String getB_fee() {
		return B_fee;
	}

	public void setB_fee(String b_fee) {
		B_fee = b_fee;
	}

	public String getB_top() {
		return B_top;
	}

	public void setB_top(String b_top) {
		B_top = b_top;
	}

	public List<RechargeItemBean> getList() {
		return list;
	}

	public void setList(List<RechargeItemBean> list) {
		this.list = list;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

}
