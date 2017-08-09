package com.avaya.queue.job;

import com.avaya.queue.email.Settings;
import com.avaya.queue.util.Constants;

public class AdvAppImpQueueJob extends QueueJob{
	public AdvAppImpQueueJob(){
		this(Settings.getString(Constants.QUEUE_MONITORING_IMP_URL),"ADV_APP_IMP",Constants.QUEUE_IMP_FILE_NAME,"res_queue_adv_app_imp");
	}
	
	public AdvAppImpQueueJob(String url, String queueName,String fileName,String resDir) {
		super(url, queueName,fileName,resDir);
	}

}
