package com.apicloud.module;

import java.util.List;

public class CardBean {
	/**
	 * data :
	 * [{"id":"368","userno":"51668315705815842","cardno":"6217004168231",
	 * "cardname":"建设银行","uname":"丁攀","cardimg":"bank_js.png","type":"1","main":
	 * "1","province":"浙江省","city":"杭州","branch":"中国建设银行股份有限公司杭州文西支行","branchno"
	 * :"105331008029","typename":"借记卡","cname":"","tel":"95533","ctime":
	 * "1441082333","utime":null,"status":"0"},{"id":"370","userno":
	 * "51668315705815842","cardno":"62495731973","cardname":"农业银行","uname":"丁攀"
	 * ,"cardimg":"bank_ny.png","type":"1","main":"0","province":"浙江省","city":
	 * "杭州","branch":"中国农业银行杭州学院路支行","branchno":"103331000099","typename":"借记卡",
	 * "cname":"","tel":"95599","ctime":"1441082515","utime":null,"status":"0"},
	 * {"id":"374","userno":"51668315705815842","cardno":"528933884960",
	 * "cardname":"广发银行","uname":"丁攀","cardimg":"bank_gfa.png","type":"2","main"
	 * :"0","province":"","city":"","branch":null,"branchno":null,"typename":
	 * "贷记卡","cname":"","tel":"95508","ctime":"1441082696","utime":null,"status"
	 * :"0"},{"id":"375","userno":"51668315705815842","cardno":"6224278708",
	 * "cardname":"招商银行","uname":"丁攀","cardimg":"bank_zsh.png","type":"2","main"
	 * :"0","province":"","city":"","branch":null,"branchno":null,"typename":
	 * "贷记卡","cname":"","tel":"95555","ctime":"1441082722","utime":null,"status"
	 * :"0"},{"id":"376","userno":"51668315705815842","cardno":"62257664067",
	 * "cardname":"招商银行","uname":"丁攀","cardimg":"bank_zsh.png","type":"2","main"
	 * :"0","province":"","city":"","branch":null,"branchno":null,"typename":
	 * "贷记卡","cname":"","tel":"95555","ctime":"1441082752","utime":null,"status"
	 * :"0"},{"id":"377","userno":"51668315705815842","cardno":"622575036010",
	 * "cardname":"招商银行","uname":"丁攀","cardimg":"bank_zsh.png","type":"2","main"
	 * :"0","province":"暂无 查询不到"
	 * ,"city":"","branch":null,"branchno":null,"typename":"贷记卡","cname":"",
	 * "tel":"95555","ctime":"1441082770","utime":null,"status":"0"},{"id":
	 * "107396","userno":"51668315705815842","cardno":"6226556259","cardname":
	 * "光大银行","uname":"丁攀","cardimg":"bank_yl.png","type":"1","main":"0",
	 * "province":"","city":"","branch":"中国光大银行杭州文二路支行","branchno":
	 * "303331077423","typename":"借记卡","cname":"阳光卡(银联卡)","tel":"","ctime":
	 * "1450276564","utime":null,"status":"0"},{"id":"107398","userno":
	 * "51668315705815842","cardno":"62262197844","cardname":"中国光大银行","uname":
	 * "丁攀","cardimg":"bank_zg.png","type":"2","main":"0","province":"","city":
	 * "","branch":null,"branchno":null,"typename":"贷记卡","cname":"炎黄卡金卡","tel":
	 * "","ctime":"1450276646","utime":null,"status":"0"},{"id":"107399",
	 * "userno":"51668315705815842","cardno":"458123054679","cardname":"交通银行",
	 * "uname":"丁攀","cardimg":"bank_jt.png","type":"2","main":"0","province":"",
	 * "city":"","branch":null,"branchno":null,"typename":"贷记卡","cname":
	 * "太平洋双币贷记卡","tel":"","ctime":"1450276722","utime":null,"status":"0"}] zp :
	 * 62170004168231 error : 0
	 */

	private String zp;
	private int error;
	public String getZp_img() {
		return zp_img;
	}

	public void setZp_img(String zp_img) {
		this.zp_img = zp_img;
	}

	private String zp_img;
	
	/**
	 * id : 368 userno : 51668315705815842 cardno : 6217004168231 cardname :
	 * 建设银行 uname : 丁攀 cardimg : bank_js.png type : 1 main : 1 province : 浙江省
	 * city : 杭州 branch : 中国建设银行股份有限公司杭州文西支行 branchno : 105331008029 typename :
	 * 借记卡 cname : tel : 95533 ctime : 1441082333 utime : null status : 0
	 */

	private List<DataBean> data;

	public String getZp() {
		return zp;
	}

	public void setZp(String zp) {
		this.zp = zp;
	}

	public int getError() {
		return error;
	}

	public void setError(int error) {
		this.error = error;
	}

	public List<DataBean> getData() {
		return data;
	}

	public void setData(List<DataBean> data) {
		this.data = data;
	}

	public static class DataBean {
		private String id;
		private String userno;
		private String cardno;
		private String cardname;
		private String uname;
		private String cardimg;
		private String type;
		private String main;
		private String province;
		private String city;
		private String branch;
		private String branchno;
		private String typename;
		private String cname;
		private String tel;
		private String ctime;
		private String utime;
		private String status;
	

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getUserno() {
			return userno;
		}

		public void setUserno(String userno) {
			this.userno = userno;
		}

		public String getCardno() {
			return cardno;
		}

		public void setCardno(String cardno) {
			this.cardno = cardno;
		}

		public String getCardname() {
			return cardname;
		}

		public void setCardname(String cardname) {
			this.cardname = cardname;
		}

		public String getUname() {
			return uname;
		}

		public void setUname(String uname) {
			this.uname = uname;
		}

		public String getCardimg() {
			return cardimg;
		}

		public void setCardimg(String cardimg) {
			this.cardimg = cardimg;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getMain() {
			return main;
		}

		public void setMain(String main) {
			this.main = main;
		}

		public String getProvince() {
			return province;
		}

		public void setProvince(String province) {
			this.province = province;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getBranch() {
			return branch;
		}

		public void setBranch(String branch) {
			this.branch = branch;
		}

		public String getBranchno() {
			return branchno;
		}

		public void setBranchno(String branchno) {
			this.branchno = branchno;
		}

		public String getTypename() {
			return typename;
		}

		public void setTypename(String typename) {
			this.typename = typename;
		}

		public String getCname() {
			return cname;
		}

		public void setCname(String cname) {
			this.cname = cname;
		}

		public String getTel() {
			return tel;
		}

		public void setTel(String tel) {
			this.tel = tel;
		}

		public String getCtime() {
			return ctime;
		}

		public void setCtime(String ctime) {
			this.ctime = ctime;
		}

		public String getUtime() {
			return utime;
		}

		public void setUtime(String utime) {
			this.utime = utime;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
	}
}
