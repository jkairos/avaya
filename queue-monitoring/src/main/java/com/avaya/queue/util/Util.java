package com.avaya.queue.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class Util {
	public static String stackTraceToString(Throwable t) {
		StringWriter writer = new StringWriter();
		t.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	public static String getReport(Throwable exception) {
		StringBuffer report = new StringBuffer("");
		report.append("Problem occured on SGE at " + new Date() + "\n");
		report.append("\n");
		while (true) {
			String stackTrace = stackTraceToString(exception);
			if (stackTrace.length() > 30000)
				stackTrace = stackTrace.substring(0, 30000) + " [CROPPED FROM " + stackTrace.length() + " CHARS]";
			report.append("Exception message/stack... " + "\n");
			report.append("===============================================\n");
			report.append(exception.getMessage() + "\n");
			report.append("-----------------------------------------------\n");
			report.append(stackTrace + "\n");
			report.append("===============================================\n");
			report.append("\n");
			break;
		}
		return report.toString();
	}

}
