package com.avaya.queue.job;

import com.avaya.queue.email.Settings;
import com.avaya.queue.util.Constants;

public class AdvAppSupportQueuePendingSrsJob extends QueuePendingSrsJob {
	public AdvAppSupportQueuePendingSrsJob(){
		this(Settings.getString(Constants.QUEUE_MONITORING_URL),"ADV_APP_SUPPORT",Constants.QUEUE_PENDING_FILE_NAME,"res_pending_adv_app_support");
	}
	
	public AdvAppSupportQueuePendingSrsJob(String url, String queueName,String fileName,String resDir) {
		super(url, queueName,fileName,resDir);
	}
}
