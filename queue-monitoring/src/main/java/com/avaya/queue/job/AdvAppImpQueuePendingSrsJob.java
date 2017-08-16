package com.avaya.queue.job;

import com.avaya.queue.email.Settings;
import com.avaya.queue.util.Constants;

public class AdvAppImpQueuePendingSrsJob extends QueuePendingSrsJob {
	
	public AdvAppImpQueuePendingSrsJob(){
		this(Settings.getString(Constants.QUEUE_MONITORING_IMP_URL),"ADV_APP_IMP",Constants.IMP_QUEUE_PENDING_FILE_NAME,"res_pending_adv_app_imp");
	}
	
	public AdvAppImpQueuePendingSrsJob(String url, String queueName,String fileName,String resDir) {
		super(url, queueName,fileName,resDir);
	}

}
