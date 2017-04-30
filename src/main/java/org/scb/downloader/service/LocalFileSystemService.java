package org.scb.downloader.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.scb.downloader.domain.DWFileInfo;
import org.scb.downloader.domain.DWHostAccount;
import org.scb.downloader.service.ConnectionService.ConnectionSession;
import org.scb.downloader.service.ConnectionService.FileEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("localFileSystemService")
public class LocalFileSystemService implements ConnectionService {

	protected static final Logger log = LoggerFactory.getLogger(LocalFileSystemService.class);

	@Override
	public LocalFileSystemConnectionSession connect(DWHostAccount account) throws Exception {
		return new LocalFileSystemConnectionSession();
	}

	@Override
	public List<FileEntry> listDir(ConnectionSession session, String pathStr, boolean directoryOnly) throws Exception {
		log.debug("Listing local directory content: {}", pathStr);
		List<FileEntry> result = new ArrayList<>();
		if (pathStr == null) {
			return result;
		}
		Path path = FileSystems.getDefault().getPath(pathStr);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				if (!directoryOnly || Files.isDirectory(entry)) {
					result.add(new LocalFileSystemFileEntry(entry));
				}
			}
		}
		return result;
	}

	private void transfert(String src, String target) throws IOException {
		File srcF = new File(src);
		File targetF = new File(target);
		if (srcF.isDirectory()) {
			if (targetF.isDirectory()) {
				FileUtils.copyDirectory(srcF, targetF);
			} else {
				throw new IOException("Cannot copy the directory '" + src + "' into the file '" + target + "'.");
			}

		} else if (targetF.isDirectory()) {
			FileUtils.copyFileToDirectory(srcF, targetF, true);

		} else {
			FileUtils.copyFile(srcF, targetF, true);
		}
	}

	@Override
	public void upload(ConnectionSession session, String srcPath, DWFileInfo target, ProgressHandler handler)
			throws Exception {
		transfert(srcPath, target.getPath());
	}

	@Override
	public void download(ConnectionSession session, DWFileInfo source, String targetPath, ProgressHandler handler)
			throws Exception {
		transfert(source.getPath(), targetPath);
	}
}

class LocalFileSystemConnectionSession extends ConnectionSession {
	@Override
	protected void forceClose() {
	}
}

class LocalFileSystemFileEntry implements FileEntry {

	private Path path;

	public LocalFileSystemFileEntry(Path path) {
		this.path = path;
	}

	@Override
	public String getFilename() throws IOException {
		return path.toFile().getName();
	}

	@Override
	public boolean isDir() throws IOException {
		return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
	}

	@Override
	public long getSize() throws IOException {
		return Files.size(path);
	}

	@Override
	public long geLastModified() throws IOException {
		return Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis();
	}
}
