package com.avaya.queue.entity;

public class Activity {
	private String type;
	private String createdBy;
	private String dateCreated;
	private String status;
	private String privateNote;
	private String assignmentTime;
	private String owner;
	private String description;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getPrivateNote() {
		return privateNote;
	}
	public void setPrivateNote(String privateNote) {
		this.privateNote = privateNote;
	}
	public String getAssignmentTime() {
		return assignmentTime;
	}
	public void setAssignmentTime(String assignmentTime) {
		this.assignmentTime = assignmentTime;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}
