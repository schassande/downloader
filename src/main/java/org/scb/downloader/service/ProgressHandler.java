package org.scb.downloader.service;

public interface ProgressHandler {
	void start(String file, long max);

	boolean count(long count);

	void end();
}