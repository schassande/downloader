package org.scb.downloader.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.scb.downloader.domain.DWHostAccount;
import org.scb.downloader.service.ConnectionService.ConnectionSession;
import org.scb.downloader.service.ConnectionService.FileEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.UserInfo;

/**
 * Service for managing SSH connection.
 * 
 * @author S.Chassande
 */
@Service("sshService")
public class SshService extends AbstractConnectionServiceImpl {
	protected static final Logger log = LoggerFactory.getLogger(SshService.class);

	/**
	 * Open session to an ssh account.
	 * 
	 * @param account
	 *            describes the connection info.
	 * @return the session object.
	 * @throws JSchException
	 *             if an error occurs.
	 */
	@Override
	public ConnectionSession connect(DWHostAccount account) throws JSchException {
		log.debug("Connecting to {}@{}:{} ...", account.getUserName(), account.getHost(), account.getPort());
		// JSch.setLogger(new JschLogger());
		JSch jsch = new JSch();
		Session session = jsch.getSession(account.getUserName(), account.getHost(),
				Integer.parseInt(account.getPort()));
		session.setPassword(account.getPassword());
		session.setUserInfo(new MyUserInfo(null, account.getHost()));
		java.util.Properties configuration = new java.util.Properties();
		configuration.put("kex",
				"diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256");
		configuration.put("StrictHostKeyChecking", "no");
		session.setConfig(configuration);
		session.connect();
		ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
		channel.connect();
		return new SftpConnectionSession(session, channel);
	}

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
	@Override
	public List<FileEntry> listDir(ConnectionSession session, String path, boolean directoryOnly)
			throws JSchException, SftpException {
		return toFileList(((SftpConnectionSession) session).sftpChannel.ls(path), directoryOnly);
	}

	@Override
	protected void downloadFile(String remoteSourceFile, File localTargetDirectory, ConnectionSession session,
			ProgressHandler handler) throws Exception {
		File localTargetFile = new File(localTargetDirectory, getLastPathElement(remoteSourceFile));
		log.debug("Download from the SFTP remote file '{}' to the local file '{}'", remoteSourceFile,
				localTargetFile.getAbsolutePath());
		localTargetDirectory.mkdirs();
		ChannelSftp sftpChannel = ((SftpConnectionSession) session).sftpChannel;
		sftpChannel.lstat(remoteSourceFile).getSize();
		sftpChannel.get(remoteSourceFile, localTargetFile.getAbsolutePath(), new MyProgressMonitor(handler));
	};

	@Override
	protected void uploadFile(File localFile, String remoteTargetDirectory, ConnectionSession session,
			ProgressHandler handler) throws Exception {
		log.debug("Upload the local file '{}' into the SFTP remote directory '{}'.", localFile.getAbsolutePath(),
				remoteTargetDirectory);
		String remoteName = addPathElement(remoteTargetDirectory, localFile.getName());
		FileInputStream fis = new FileInputStream(localFile);
		try {
			((SftpConnectionSession) session).sftpChannel.put(fis, remoteName, new MyProgressMonitor(handler),
					ChannelSftp.OVERWRITE);
		} finally {
			fis.close();
		}
	}

	@Override
	protected boolean changeRemoteDirectory(String directory, ConnectionSession session) throws Exception {
		return true;
	}

	@Override
	protected void createRemoteDirectory(String directory, ConnectionSession session) throws Exception {
		try {
			((SftpConnectionSession) session).sftpChannel.stat(directory);
			log.debug("Directory '{}' already exists.", directory);
		} catch (Exception e) {
			log.debug("Creating unexisting directory '{}' ", directory);
			((SftpConnectionSession) session).sftpChannel.mkdir(directory);
		}
	}

	@Override
	protected Logger log() {
		return log;
	}

	private List<FileEntry> toFileList(Collection<?> vv, boolean directoryOnly) {
		List<FileEntry> fileList = new ArrayList<>();
		for (Iterator<?> it = vv.iterator(); it.hasNext();) {
			Object obj = it.next();
			if (obj instanceof LsEntry) {
				LsEntry lse = (LsEntry) obj;
				if (!directoryOnly || lse.getAttrs().isDir()) {
					fileList.add(new SftpFileEntry((LsEntry) obj));
				} else {
					log.debug("File entry rejected: " + lse.getFilename());
				}
			} else {
				log.debug("Unknown file entry: " + obj);
			}
		}
		return fileList;
	}
}

class SftpConnectionSession extends ConnectionSession {
	protected Session session;
	protected ChannelSftp sftpChannel;

	public SftpConnectionSession(Session session, ChannelSftp sftpChannel) {
		this.session = session;
		this.sftpChannel = sftpChannel;
	}

	@Override
	protected void forceClose() {
		try {
			if (sftpChannel != null) {
				sftpChannel.disconnect();
			}
		} catch (Throwable t) {
			SshService.log.warn("Error when closing SFTP channel: " + t.getMessage(), t);
		}
		try {
			if (session != null) {
				session.disconnect();
			}
		} catch (Throwable t) {
			SshService.log.warn("Error when closing SFTP session: " + t.getMessage(), t);
		}
	}
}

class MyProgressMonitor implements SftpProgressMonitor {
	private ProgressHandler handler;

	public MyProgressMonitor(ProgressHandler handler) {
		this.handler = handler;
	}

	@Override
	public void init(int op, String src, String dest, long max) {
		if (handler != null) {
			handler.start(src, max);
		}
	}

	@Override
	public boolean count(long count) {
		return handler != null && handler.count(count);
	}

	@Override
	public void end() {
		if (handler != null) {
			handler.end();
		}
	}
}

/**
 * Defines a fie descriptor from Sftp LsEntry.
 * 
 * @author S.Chassande
 */
class SftpFileEntry implements FileEntry {

	private LsEntry sftpEnry;

	public SftpFileEntry(LsEntry sftpEnry) {
		this.sftpEnry = sftpEnry;
	}

	@Override
	public String getFilename() throws IOException {
		return sftpEnry.getFilename();
	}

	@Override
	public boolean isDir() throws IOException {
		return sftpEnry.getAttrs().isDir();
	}

	@Override
	public long getSize() throws IOException {
		return sftpEnry.getAttrs().getSize();
	}

	@Override
	public long geLastModified() throws IOException {
		return (sftpEnry.getAttrs().getMTime()) * 1000l;
	}
}

/**
 * Holds user secret information.
 * 
 * @author S.Chassande
 */
class MyUserInfo implements UserInfo {
	String passPhrase;
	String password;

	public MyUserInfo(String passPhrase, String password) {
		super();
		this.passPhrase = passPhrase;
		this.password = password;
	}

	@Override
	public String getPassphrase() {
		return passPhrase;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean promptPassword(String message) {
		return false;
	}

	@Override
	public boolean promptPassphrase(String message) {
		return false;
	}

	@Override
	public boolean promptYesNo(String message) {
		return false;
	}

	@Override
	public void showMessage(String message) {
		SshService.log.debug(message);
	}
}

class JschLogger implements com.jcraft.jsch.Logger {

	@Override
	public boolean isEnabled(int level) {
		switch (level) {
		default:
		case FATAL:
		case ERROR:
			return SshService.log.isErrorEnabled();
		case WARN:
			return SshService.log.isWarnEnabled();
		case INFO:
			return SshService.log.isInfoEnabled();
		case DEBUG:
			return SshService.log.isDebugEnabled();
		}
	}

	@Override
	public void log(int level, String message) {
		switch (level) {
		case FATAL:
		case ERROR:
			SshService.log.error(message);
			break;
		case WARN:
			SshService.log.warn(message);
			break;
		case INFO:
			SshService.log.info(message);
			break;
		case DEBUG:
			SshService.log.debug(message);
			break;
		}
	}
}
