package com.avaya.queue.entity;

public class SR {
	private String number;
	private String sev;
	private String type;
	private String status;
	private String age;
	private String tscs;
	private String ncs;
	private String backlogState;
	private boolean mea;
	private boolean DispOrPart;
	private String productSkill;
	private String fl;
	private String flName;
	private String country;
	private int otherSrs;
	
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getSev() {
		return sev;
	}
	public void setSev(String sev) {
		this.sev = sev;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public String getTscs() {
		return tscs;
	}
	public void setTscs(String tscs) {
		this.tscs = tscs;
	}
	public String getNcs() {
		return ncs;
	}
	public void setNcs(String ncs) {
		this.ncs = ncs;
	}
	public String getBacklogState() {
		return backlogState;
	}
	public void setBacklogState(String backlogState) {
		this.backlogState = backlogState;
	}
	public boolean isMea() {
		return mea;
	}
	public void setMea(boolean mea) {
		this.mea = mea;
	}
	public boolean isDispOrPart() {
		return DispOrPart;
	}
	public void setDispOrPart(boolean dispOrPart) {
		DispOrPart = dispOrPart;
	}
	public String getProductSkill() {
		return productSkill;
	}
	public void setProductSkill(String productSkill) {
		this.productSkill = productSkill;
	}
	public String getFl() {
		return fl;
	}
	public void setFl(String fl) {
		this.fl = fl;
	}
	public String getFlName() {
		return flName;
	}
	public void setFlName(String flName) {
		this.flName = flName;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public int getOtherSrs() {
		return otherSrs;
	}
	public void setOtherSrs(int otherSrs) {
		this.otherSrs = otherSrs;
	}
	
	@Override
	public String toString() { 
		String str="SR# " + number + ", Sev " + sev + ", Type "+type+", Product Skill "
	     + productSkill + ", FL " + fl + " FL Name, " + flName;
		return str;
	}
	
}
