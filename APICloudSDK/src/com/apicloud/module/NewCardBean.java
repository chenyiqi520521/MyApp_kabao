package com.apicloud.module;

import java.io.Serializable;

public class NewCardBean implements Serializable{
	String cardIco;
	String cardName;
	String cradNo;
	public String getCardIco() {
		return cardIco;
	}
	public void setCardIco(String cardIco) {
		this.cardIco = cardIco;
	}
	public String getCardName() {
		return cardName;
	}
	public void setCardName(String cardName) {
		this.cardName = cardName;
	}
	public String getCradNo() {
		return cradNo;
	}
	public void setCradNo(String cradNo) {
		this.cradNo = cradNo;
	}
	
}
