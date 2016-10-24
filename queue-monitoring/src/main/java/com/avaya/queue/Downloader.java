package com.avaya.queue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import com.avaya.queue.security.PKIXAuthenticator;

public class Downloader {

	public void downloadContractFile() {
		try {
			URL url = new URL(Constants.URL_XLSB);
			File destination = new File(Constants.CONTRACTS_XLSB);
			if (!destination.exists()) {
				PKIXAuthenticator.authenticate();
				url.openConnection();

				// Copy bytes from the URL to the destination file.
				FileUtils.copyURLToFile(url, destination);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
