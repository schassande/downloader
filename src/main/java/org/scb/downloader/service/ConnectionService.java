package org.scb.downloader.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.scb.downloader.domain.DWFileInfo;
import org.scb.downloader.domain.DWHostAccount;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

/**
 * Defines a service able to connect to remote server. After the connection
 * step, it is possible :
 * <ul>
 * <li>to list a remote directory</li>
 * <li>upload a file or a directory</li>
 * <li>download a file or a directory</li>
 * </ul>
 * 
 * @author S.Chassande
 *
 * @param <CS>
 */
public interface ConnectionService {

	/**
	 * Connects to a remote server.
	 * 
	 * @param account
	 *            is the connection information
	 * @return a session object to use for each any operation.
	 * @throws Exception
	 */
	ConnectionSession connect(DWHostAccount account) throws Exception;

	/**
	 * List directory content.
	 * 
	 * @param session
	 *            is the SSH session.
	 * @param path
	 *            is the directory path
	 * @param directoryOnly
	 *            true if the result must contain only directories.
	 * @return a list of
	 * @throws JSchException
	 * @throws SftpException
	 */
	List<FileEntry> listDir(ConnectionSession session, String path, boolean directoryOnly) throws Exception;

	/**
	 * Transfert a file from a local file to a remote path
	 * 
	 * @param srcPath
	 *            is the local file
	 * @param target
	 *            is the remote account/path
	 */
	void upload(ConnectionSession session, String srcPath, DWFileInfo target, ProgressHandler handler) throws Exception;

	/**
	 * Transfert a file from a remote account to a local file system path
	 * 
	 * @param source
	 *            is the remote account
	 * @param targetPath
	 *            is the local file system path
	 */
	void download(ConnectionSession session, DWFileInfo source, String targetPath, ProgressHandler handler)
			throws Exception;

	/**
	 * Defines a file on the data provider.
	 * 
	 * @author S.Chassande
	 */
	interface FileEntry {

		/**
		 * @return the name of the file.
		 * @throws IOException
		 */
		String getFilename() throws IOException;

		/**
		 * @return true if the file entry is a directory.
		 */
		boolean isDir() throws IOException;

		/**
		 * @return the size of the file.
		 */
		long getSize() throws IOException;

		/**
		 * @return the last modified time as long value.
		 * @throws IOException
		 */
		long geLastModified() throws IOException;
	}

	public abstract class ConnectionSession implements Closeable {

		boolean used = false;
		boolean markAsToClose = false;

		public boolean isUsed() {
			return used;
		}

		public synchronized void used() {
			used = true;
		}

		public void unused() {
			boolean mustClose = false;
			synchronized (this) {
				used = false;
				mustClose = markAsToClose;
			}
			if (mustClose) {
				forceClose();
			}
		}

		public void closeOrMarkAsToClose() {
			synchronized (this) {
				if (used) {
					markAsToClose = true;
					return;
				}
			}
			forceClose();
		}

		@Override
		public void close() {
			closeOrMarkAsToClose();
		}

		protected abstract void forceClose();
	}
}
