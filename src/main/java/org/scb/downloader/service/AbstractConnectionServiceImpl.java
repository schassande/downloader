package org.scb.downloader.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.scb.downloader.domain.DWFileInfo;
import org.slf4j.Logger;

/**
 * Helper class used to implement the ConnectionService interface. It provides
 * basic algorythm to tranfert data from or to a remote server. In particular it
 * manages directories hierarchy. This class must be sub classed in order to
 * implement the following basic operations:
 * <ul>
 * <li>create a directory on remote server</li>
 * <li>change the current directory on server</li>
 * <li>upload a local file on the remote server</li>
 * <li>download a file from remote server</li>
 * </ul>
 * 
 * @author S.Chassande
 *
 * @param <S>
 *            is the specialization of the ConnectionSession.
 */
public abstract class AbstractConnectionServiceImpl implements ConnectionService {

	/**
	 * @return the logger of the sub classes.
	 */
	protected abstract Logger log();

	/**
	 * Creates a directory on remote server.
	 * 
	 * @param directory
	 *            is the directory to create on remote server
	 * @param session
	 *            is the session establish with the remote server.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	protected abstract void createRemoteDirectory(String directory, ConnectionSession session) throws Exception;

	/**
	 * Changes the current directory on the remote server.
	 * 
	 * @param directory
	 *            is the new current directory on remote server
	 * @param session
	 *            is the session establish with the remote server.
	 * @throws Exception
	 *             if a problem occurs.
	 * @return true if the current remote directory has been changed with
	 *         success, false otherwise.
	 */
	protected abstract boolean changeRemoteDirectory(String directory, ConnectionSession session) throws Exception;

	/**
	 * Upload a local file into a remote directory.
	 * 
	 * @param localFile
	 *            is the local file on the local file system.
	 * @param remoteTargetDirectory
	 *            is an existing directory on the remote server.
	 * @param session
	 *            is the session establish with the remote server.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	protected abstract void uploadFile(File localFile, String remoteTargetDirectory, ConnectionSession session,
			ProgressHandler handler) throws Exception;

	/**
	 * Download a file from the remote server and store it into a local
	 * directory.
	 * 
	 * @param remoteSourceFile
	 *            is the path of the remote file to download.
	 * @param localTargetDirectory
	 *            is the directory where to store the downloaded file.
	 * @param session
	 *            is the session establish with the remote server.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	protected abstract void downloadFile(String remoteSourceFile, File localTargetDirectory, ConnectionSession session,
			ProgressHandler handler) throws Exception;

	@Override
	public void upload(ConnectionSession session, String srcPath, DWFileInfo target, ProgressHandler handler)
			throws Exception {
		log().info("Transfering '{}' to '{}' on server ...", srcPath, target.getPath());
		session.used();
		try {
			// check the item to upload exists locally.
			File source = new File(srcPath).getCanonicalFile();
			if (!source.exists()) {
				throw new FileNotFoundException(srcPath);
			}

			// create the remote directory
			createRemoteDirectory(target.getPath(), session);
			// go inside remote directory
			changeRemoteDirectory(target.getPath(), session);

			if (source.isDirectory()) {
				// compute the remote destination with the name of the directory
				// to send
				String remoteName = addPathElement(target.getPath(), source.getName());
				// ask the upload of the content of the directory
				uploadDirectory(source, remoteName, session, handler);
			} else {
				uploadFile(source, target.getPath(), session, handler);
			}
		} finally {
			session.unused();
		}
	}

	/**
	 * Uploads the content a local directory in a remote directory.
	 * 
	 * @param sourceDirectory
	 *            is the local directory.
	 * @param remoteTargetDirectory
	 *            is the path of the remote directory.
	 * @param session
	 *            is the session establish with the remote server.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	protected void uploadDirectory(File sourceDirectory, String remoteTargetDirectory, ConnectionSession session,
			ProgressHandler handler) throws Exception {
		log().debug("Upload the local directory '{}' content to the remote directory '{}'...",
				sourceDirectory.getAbsolutePath(), remoteTargetDirectory);
		// List the directory content to upload
		File[] files = sourceDirectory.listFiles();
		if (files == null || files.length == 0) {
			return;
		}
		// create the remote directory
		createRemoteDirectory(remoteTargetDirectory, session);
		// change the remote directory
		changeRemoteDirectory(remoteTargetDirectory, session);
		// Iterate over local directory content.
		for (File file : files) {
			if (".".equals(file.getName()) || "..".equals(file.getName())) {
				log().debug("Ingoring '{}'.", file.getName());

			} else if (file.isFile()) {
				uploadFile(file, remoteTargetDirectory, session, handler);

			} else if (file.isDirectory()) {
				// build the new sub directory with the item name
				String remoteName = addPathElement(remoteTargetDirectory, file.getName());
				uploadDirectory(file, remoteName, session, handler);
			}
		}
	}

	@Override
	public void download(ConnectionSession session, DWFileInfo source, String targetPath, ProgressHandler handler)
			throws Exception {
		log().info("Transfering '{}' from remote server to the local directory '{}' ...", source.getPath(), targetPath);
		session.used();
		try {
			// create the target directory
			File targetDirectory = new File(targetPath).getCanonicalFile();
			targetDirectory.mkdirs();

			// Find if the source remote path is a file or a directory
			List<FileEntry> files = listDir(session, source.getPath(), false);
			if (files.isEmpty()) {
				throw new IOException("Cannot download an unexisting remote path '" + source.getPath() + "'.");
			}
			if (files.size() == 1 && files.get(0).getFilename().equals(getLastPathElement(source.getPath()))) {
				// Remote source path is a file
				downloadFile(source.getPath(), targetDirectory, session, handler);
			} else {
				// Remote source path is a directory => create locally the
				// directory which has been to transfer
				File targetSubDirectory = new File(targetDirectory, getLastPathElement(source.getPath()))//
						.getAbsoluteFile().getCanonicalFile();

				// download the directory content inside.
				downloadDirectory(source.getPath(), targetSubDirectory, session, handler);
			}
		} finally {
			session.unused();
		}
	}

	/**
	 * Download a remote directory content into a local directory.
	 * 
	 * @param sourceRemoteDirectory
	 *            is the path of the remote directory.
	 * @param targetLocalDirectory
	 *            is the local directory
	 * @param session
	 *            is the session establish with the remote server.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	protected void downloadDirectory(String sourceRemoteDirectory, File targetLocalDirectory, ConnectionSession session,
			ProgressHandler handler) throws Exception {
		log().debug("Download the remote directory '{}' content into the local directory '{}'...",
				sourceRemoteDirectory, targetLocalDirectory.getAbsolutePath());
		List<FileEntry> files = listDir(session, sourceRemoteDirectory, false);
		if (files == null || files.isEmpty()) {
			return;
		}
		targetLocalDirectory.mkdirs();
		for (FileEntry file : files) {
			String sourceRemotePath = addPathElement(sourceRemoteDirectory, file.getFilename());
			if (".".equals(file.getFilename()) || "..".equals(file.getFilename())) {
				log().debug("Ingoring '{}'.", file.getFilename());

			} else if (file.isDir()) {
				File targetSubDirectory = new File(targetLocalDirectory, file.getFilename());
				downloadDirectory(sourceRemotePath, targetSubDirectory, session, handler);
			} else {
				downloadFile(sourceRemotePath, targetLocalDirectory, session, handler);
			}
		}
	}

	/**
	 * Extracts the last element from a path.
	 * 
	 * @param path
	 *            is file/directory path
	 * @return the file name of the last element.
	 */
	protected String getLastPathElement(String path) {
		int idx = path.lastIndexOf('/');
		if (idx >= 0) {
			return path.substring(idx + 1);
		} else {
			return path;
		}
	}

	/**
	 * Helper method to add a single path element at the end of a path.
	 * 
	 * @param begin
	 *            is an existing path
	 * @param newOne
	 *            is the new element to add to the existing path.
	 * @return the new path
	 */
	protected String addPathElement(String begin, String newOne) {
		return begin + (begin.endsWith("/") ? "" : "/") + newOne;
	}
}
