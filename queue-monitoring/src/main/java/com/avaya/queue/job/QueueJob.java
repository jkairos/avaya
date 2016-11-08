package com.avaya.queue.job;

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.avaya.queue.QueueMonitoringDownloader;
import com.avaya.queue.SRDetailsDownloader;
import com.avaya.queue.entity.CustomerContract;
import com.avaya.queue.entity.SR;
import com.avaya.queue.util.AsyncEmailer;
import com.avaya.queue.util.Constants;
import com.avaya.queue.util.QueueMonitoringProperties;

public class QueueJob extends QuartzJobBean {
	private final static Logger logger = Logger.getLogger(QueueJob.class);
	private SRDetailsDownloader srDetaildDownloader = new SRDetailsDownloader();
	private QueueMonitoringDownloader getUrlContent = new QueueMonitoringDownloader();
	private SolrClient solr;

	@Override
	protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
		// Downloader downloader = new Downloader();
		// downloader.downloadContractFile();
		this.cleanup();
		this.processQueue();
	}

	private void cleanup() {
		File file = new File(Constants.PROJECT_PATH + File.separator + "res");
		if (file.exists()) {
			File[] files = file.listFiles();
			for (File file2 : files) {
				file2.delete();
			}
		}
	}

	private void processQueue() {
		getUrlContent.readUrl();
		List<SR> queueList = getUrlContent.processFile();
		if (logger.isDebugEnabled()) {
			logger.debug("Current Queue Size: " + queueList.size());
		}
		if (queueList != null && !queueList.isEmpty()) {
			srDetaildDownloader.processSRDetails(queueList);
			for (SR sr : queueList) {
				this.setSRCustomerContracts(sr);
				if (logger.isDebugEnabled()) {
					logger.debug("SR INFORMATION: " + sr.toString());
				}
				this.sendEmail(sr);
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("...............QUEUE IS EMPTY.......... ");
				this.sendEmail(null);
			}
		}
	}

	private void setSRCustomerContracts(SR sr) {
		String queryString = "fl:'" + sr.getFl() + "' AND status:'Open'";
		QueryResponse response = this.getQueryResponse(queryString);
		if (logger.isDebugEnabled()) {
			logger.debug("Response from SOLR : " + response);
		}
		List<CustomerContract> customerContracts = new ArrayList<CustomerContract>();
		SolrDocumentList results = response.getResults();

		if (!results.isEmpty()) {
			for (int i = 0; i < results.size(); ++i) {
				if (logger.isDebugEnabled()) {
					logger.debug(results.get(i));
				}

				SolrDocument solrDocument = results.get(i);
				Set<String> keys = solrDocument.keySet();
				customerContracts.add((CustomerContract) this.instantiateObjectViaRefletion(new CustomerContract(),
						keys, solrDocument));
			}

		} else {// Could not find by FL, then tries by customer name, status and
				// solution @TODO
				// needs to be implemented
			queryString = "+'" + sr.getAccount() + "' AND status:'Open'";
			response = this.getQueryResponse(queryString);
			results = response.getResults();
			if (!results.isEmpty()) {
				for (int i = 0; i < results.size(); ++i) {
					if (logger.isDebugEnabled()) {
						logger.debug(results.get(i));
					}

					SolrDocument solrDocument = results.get(i);
					Set<String> keys = solrDocument.keySet();
					customerContracts.add((CustomerContract) this.instantiateObjectViaRefletion(new CustomerContract(),
							keys, solrDocument));
				}

			}
		}

		sr.setCustomerContracts(customerContracts);

	}

	private Object instantiateObjectViaRefletion(Object instance, Set<String> keys, SolrDocument solrDocument) {
		for (String key : keys) {
			this.set(instance, key, solrDocument.get(key));
		}
		return instance;
	}

	private boolean set(Object object, String fieldName, Object fieldValue) {
		Class<?> clazz = object.getClass();
		while (clazz != null) {
			try {
				Field field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				String val = null;
				if (fieldValue instanceof List) {
					val = ((List) fieldValue).get(0).toString();
				} else if (fieldValue instanceof String) {
					val = ((String) fieldValue);
				}
				val = val.replaceAll("'", "");
				field.set(object, val);
				return true;
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
				logger.error(e);
			} catch (Exception e) {
				logger.error(e);
				throw new IllegalStateException(e);
			}
		}
		return false;
	}

	public QueryResponse getQueryResponse(String queryString) {
		solr = new HttpSolrClient.Builder(QueueMonitoringProperties.getProperty("apache.solr.url")).build();
		SolrQuery query = new SolrQuery();
		query.setQuery(queryString);

		QueryResponse queryResponse = null;
		try {
			queryResponse = solr.query(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return queryResponse;
	}

	private void sendEmail(SR sr) {
		/**
		 * Initialize engine and get template
		 */
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
//		ve.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		ve.setProperty("file.resource.loader.path", Constants.PROJECT_PATH+"templates");
		ve.init();
		Template template = null;
		
		if(sr!=null && !sr.getCustomerContracts().isEmpty()){
			template=ve.getTemplate("customer-contracts.vm");
		}else if(sr!=null && sr.getCustomerContracts().isEmpty()){
			template=ve.getTemplate("no-contracts-found.vm");
		}else{
			template=ve.getTemplate("queue-empty.vm");
		}
		
		/**
		 * Prepare context data
		 */
		VelocityContext context = new VelocityContext();
		if(sr!=null){
			context.put("sr", sr);
			context.put("customerContracts", sr.getCustomerContracts());
		}
		
		/**
		 * Merge data and template
		 */
		StringWriter swOut = new StringWriter();
		template.merge(context, swOut);
		
        String message=swOut.toString();
        String subject=null;
        
		if(sr!=null){
			subject=sr.getNumber() + " - " + sr.getAccount() + " - " +sr.getProductEntitled();
		}else{
			subject="Queue is Empty";
		}
		
		AsyncEmailer.getInstance(subject, message).start();
		
		System.out.println(swOut);
	}

}
