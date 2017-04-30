package org.scb.downloader.web.rest.dto;

import org.scb.downloader.domain.DWHostAccount;

public class BrowseRequest {

	private DWHostAccount account;
	private String path;
	private Boolean directoryOnly;

	public BrowseRequest() {
	}

	public BrowseRequest(BrowseRequest req) {
		this.account = req.account;
		this.path = req.path;
		this.directoryOnly = req.directoryOnly;
	}

	public DWHostAccount getAccount() {
		return account;
	}

	public void setAccount(DWHostAccount account) {
		this.account = account;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Boolean getDirectoryOnly() {
		return directoryOnly;
	}

	public void setDirectoryOnly(Boolean directoryOnly) {
		this.directoryOnly = directoryOnly;
	}
}
