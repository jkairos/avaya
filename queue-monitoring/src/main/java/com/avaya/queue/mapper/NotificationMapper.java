package com.avaya.queue.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;

import com.avaya.queue.entity.Notification;

public class NotificationMapper implements RowMapper<Notification> {

	public Notification mapRow(ResultSet result, int rownum) throws SQLException {
		Notification notification = new Notification();
		DateTime notificationDate = new DateTime(result.getTimestamp("notificationDate"));
		
		notification.setSrNumber(result.getString("srNumber"));
		notification.setReminder(result.getInt("reminder"));
		notification.setNotificationDate(notificationDate);

		return notification;
	}

}
