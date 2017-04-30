package org.scb.downloader.web.rest.dto;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.scb.downloader.service.ConnectionService.FileEntry;
import org.scb.downloader.service.LocalFileSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileInfo implements org.scb.downloader.service.ConnectionService.FileEntry {
	protected static final Logger log = LoggerFactory.getLogger(LocalFileSystemService.class);

	private long lastModified = 0;
	private long fileTime = 0;
	private String filename = "";
	private long size = 0;
	private boolean isDir = false;

	public FileInfo() {
	}

	public FileInfo(FileEntry fe) {
		try {
			this.lastModified = fe.geLastModified();
			this.fileTime = lastModified;
		} catch (IOException e) {
			this.lastModified = -1;
			log.warn(e.getMessage(), e);
		}
		try {
			this.filename = fe.getFilename();
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}
		try {
			this.size = fe.getSize();
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}
		try {
			this.isDir = fe.isDir();
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}
	}

	@Override
	public boolean isDir() throws IOException {
		return isDir;
	}

	@Override
	public long geLastModified() throws IOException {
		return lastModified;
	}

	public long getFileTime() {
		return fileTime;
	}

	@Override
	public String getFilename() throws IOException {
		return filename;
	}

	@Override
	public long getSize() throws IOException {
		return size;
	}

	public static List<FileInfo> toFileInfo(List<FileEntry> fes) throws IOException {
		return fes.stream().map(FileInfo::new).collect(Collectors.toList());
	}
}
