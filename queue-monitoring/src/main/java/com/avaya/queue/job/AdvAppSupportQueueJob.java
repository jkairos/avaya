package com.avaya.queue.job;

import com.avaya.queue.email.Settings;
import com.avaya.queue.util.Constants;

public class AdvAppSupportQueueJob extends QueueJob{
	
	public AdvAppSupportQueueJob(){
		this(Settings.getString(Constants.QUEUE_MONITORING_URL),"ADV_APP_SUPPORT",Constants.QUEUE_FILE_NAME,"res_queue_adv_app_support");
	}
	
	public AdvAppSupportQueueJob(String url, String queueName,String fileName,String resDir) {
		super(url, queueName,fileName,resDir);
	}

}
