package com.avaya.queue.dao;

import com.avaya.queue.entity.Notification;

public interface NotificationDao {
	
	public void insert(String srNumber);
	public void update(String srNumber,int reminder);
	public Notification findBySr(String srNumber);
}
