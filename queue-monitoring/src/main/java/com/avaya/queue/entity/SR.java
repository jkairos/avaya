package com.avaya.queue.entity;

import java.util.List;

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
	private String productEntitled;
	private String description;
	private String fl;
	private String flName;
	private String country;
	private int otherSrs;
	private String account;
	private String severity;
	private List<CustomerContract> customerContracts;
	private List<Activity> caseEntries;
	private boolean securityRestricted;
	private String parentName;
	private String nameContact;
	private String phoneContact;
	private String emailContact;
	private String prefLanguage;
	private boolean sentBackToQueueByAccountTeam;
	private String sentBackToQueueHandle;
	private String lastStatusNote;
	private String owner;
	private String ownerName;
	private String lastUpdate;
	private int lastUpdateInterval;
	private IntervalUpdate intervalUpdate;
	
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
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
	
	public String getProductEntitled() {
		return productEntitled;
	}
	public void setProductEntitled(String productEntitled) {
		this.productEntitled = productEntitled;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	
	public List<CustomerContract> getCustomerContracts() {
		return customerContracts;
	}
	
	public void setCustomerContracts(List<CustomerContract> customerContracts) {
		this.customerContracts = customerContracts;
	}
	
	
	public boolean isSecurityRestricted() {
		return securityRestricted;
	}
	public void setSecurityRestricted(boolean securityRestricted) {
		this.securityRestricted = securityRestricted;
	}
	
	public List<Activity> getCaseEntries() {
		return caseEntries;
	}
	public void setCaseEntries(List<Activity> caseEntries) {
		this.caseEntries = caseEntries;
	}
	
	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	
	public String getNameContact() {
		return nameContact;
	}
	public void setNameContact(String nameContact) {
		this.nameContact = nameContact;
	}
	public String getPhoneContact() {
		return phoneContact;
	}
	public void setPhoneContact(String phoneContact) {
		this.phoneContact = phoneContact;
	}
	public String getEmailContact() {
		return emailContact;
	}
	public void setEmailContact(String emailContact) {
		this.emailContact = emailContact;
	}
	public String getPrefLanguage() {
		return prefLanguage;
	}
	public void setPrefLanguage(String prefLanguage) {
		this.prefLanguage = prefLanguage;
	}
	
	public boolean isSentBackToQueueByAccountTeam() {
		return sentBackToQueueByAccountTeam;
	}
	public void setSentBackToQueueByAccountTeam(boolean sentBackToQueueByAccountTeam) {
		this.sentBackToQueueByAccountTeam = sentBackToQueueByAccountTeam;
	}
	
	public String getSentBackToQueueHandle() {
		return sentBackToQueueHandle;
	}
	public void setSentBackToQueueHandle(String sentBackToQueueHandle) {
		this.sentBackToQueueHandle = sentBackToQueueHandle;
	}
	
	public String getLastStatusNote() {
		return lastStatusNote;
	}
	public void setLastStatusNote(String lastStatusNote) {
		this.lastStatusNote = lastStatusNote;
	}
	
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	
	public int getLastUpdateInterval() {
		return lastUpdateInterval;
	}
	public void setLastUpdateInterval(int lastUpdateInterval) {
		this.lastUpdateInterval = lastUpdateInterval;
	}
	
	public IntervalUpdate getIntervalUpdate() {
		return intervalUpdate;
	}
	public void setIntervalUpdate(IntervalUpdate intervalUpdate) {
		this.intervalUpdate = intervalUpdate;
	}
	@Override
	public String toString() { 
		String str="SR# " + number + " \nDescription " +description+"\nSev " + sev + "\nType "+type+"\nProduct Skill "
	     + productSkill + "\nFL " + fl +"" +"\nFL Name " + flName + "\nProduct " + productEntitled + "\nAccount " + account;
		return str;
	}
	
	
}
