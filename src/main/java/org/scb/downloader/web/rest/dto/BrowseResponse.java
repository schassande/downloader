package org.scb.downloader.web.rest.dto;

import java.util.ArrayList;
import java.util.List;

public class BrowseResponse extends BrowseRequest {

	private List<FileInfo> files = new ArrayList<FileInfo>();

	public BrowseResponse(BrowseRequest req, List<FileInfo> files) {
		super(req);
		this.files = files;
	}

	public List<FileInfo> getFiles() {
		return files;
	}

	public void setFiles(List<FileInfo> files) {
		this.files = files;
	}
}
