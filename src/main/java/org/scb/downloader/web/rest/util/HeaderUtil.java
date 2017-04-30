package org.scb.downloader.web.rest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

/**
 * Utility class for HTTP headers creation.
 */
public class HeaderUtil {

	private static final Logger log = LoggerFactory.getLogger(HeaderUtil.class);

	public static HttpHeaders createAlert(String message, String param) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-downloaderApp-alert", message);
		headers.add("X-downloaderApp-params", param);
		return headers;
	}

	public static HttpHeaders createEntityCreationAlert(String entityName, String param) {
		return createAlert("A new " + entityName + " is created with identifier " + param, param);
	}

	public static HttpHeaders createEntityUpdateAlert(String entityName, String param) {
		return createAlert("A " + entityName + " is updated with identifier " + param, param);
	}

	public static HttpHeaders createEntityDeletionAlert(String entityName, String param) {
		return createAlert("A " + entityName + " is deleted with identifier " + param, param);
	}

	public static HttpHeaders createTransfertDoneDeletionAlert(String param) {
		return createAlert("All DWTransfert with DONE status are deleted", param);
	}

	public static HttpHeaders createFailureAlert(String entityName, String errorKey, String defaultMessage) {
		log.error("Entity creation failed, {}", defaultMessage);
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-downloaderApp-error", defaultMessage);
		headers.add("X-downloaderApp-params", entityName);
		return headers;
	}
}
