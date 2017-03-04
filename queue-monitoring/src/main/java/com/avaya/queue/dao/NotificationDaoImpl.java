package com.avaya.queue.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.avaya.queue.entity.Notification;
import com.avaya.queue.mapper.NotificationMapper;

public class NotificationDaoImpl implements NotificationDao {
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public void insert(String srNumber) {
		String sql=" INSERT INTO Notification(srNumber,notificationDate,reminder) VALUES (:srNumber,:notificationDate,:reminder) ";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("srNumber", srNumber);
		params.put("reminder", 0);
		params.put("notificationDate", new Date());
		namedParameterJdbcTemplate.update(sql, params);
	}

	public void update(String srNumber, int reminder) {
		String sql="UPDATE Notification set notificationDate=:notificationDate,reminder=:reminder where srNumber=:srNumber";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("srNumber", srNumber);
		params.put("reminder", reminder);
		params.put("notificationDate", new Date());
		namedParameterJdbcTemplate.update(sql, params);
	}

	public Notification findBySr(String srNumber) {
		Map<String, Object> params = new HashMap<String, Object>();
		Notification notification=null;
		params.put("srNumber", srNumber);
		String sql=" SELECT srNumber,notificationDate,reminder from Notification WHERE srNumber=:srNumber";
		try{
			notification = namedParameterJdbcTemplate.queryForObject(sql, params, new NotificationMapper());
		}catch(EmptyResultDataAccessException e){
			notification=null;
		}
		return notification;
	}

}
