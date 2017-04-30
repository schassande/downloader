package org.scb.downloader.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.scb.downloader.domain.DWHostAccount;
import org.scb.downloader.service.ConnectionService.ConnectionSession;
import org.scb.downloader.service.ConnectionService.FileEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("ftpService")
public class FtpService extends AbstractConnectionServiceImpl {

	protected static final Logger log = LoggerFactory.getLogger(FtpService.class);

	@Override
	public ConnectionSession connect(DWHostAccount account) throws Exception {
		log.debug("Connecting on FTP server '{}:{}'...", account.getHost(), account.getPort());

		FTPClient ftpClient = new FTPClient();
		ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
		ftpClient.setControlKeepAliveTimeout(300);
		FTPClientConfig config = new FTPClientConfig();
		config.setServerLanguageCode("fr");
		ftpClient.configure(config);
		// Step 1: connect to server
		ftpClient.connect(account.getHost(), Integer.parseInt(account.getPort()));
		int reply = ftpClient.getReplyCode();

		// After connection attempt, you should check the reply code to
		// verify success.
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftpClient.disconnect();
			throw new IOException("FTP server refused connection: " + reply);
		}

		// Step2: login with user credential
		if (ftpClient.login(account.getUserName(), account.getPassword())) {
			log.debug("Log on FTP server '{}' as '{}' with success ({}).", account.getHost(), account.getUserName(),
					reply);
		} else {
			throw new IOException(
					"Fail to Log on FTP server '" + account.getHost() + "' as '" + account.getUserName() + "'.");
		}
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		ftpClient.enterLocalPassiveMode();
		return new FtpConnectionSession(ftpClient);
	}

	@Override
	public List<FileEntry> listDir(ConnectionSession session, String path, boolean directoryOnly) throws Exception {
		log.debug("Listing remote directory content '{}'...", path);
		FTPClient ftp = ((FtpConnectionSession) session).getFtp();
		List<FileEntry> result = new ArrayList<>();
		for (FTPFile ftpFile : ftp.listFiles(path)) {
			if (!directoryOnly || ftpFile.isDirectory()) {
				result.add(new FtpFileEntry(ftpFile));
			}
		}
		return result;
	}

	@Override
	protected void downloadFile(String remoteSourceFile, File localTargetDirectory, ConnectionSession session,
			ProgressHandler handler) throws Exception {
		InputStream in = null;
		try {
			in = ((FtpConnectionSession) session).getFtp().retrieveFileStream(remoteSourceFile);
			if (in == null) {
				throw new IOException("Remote file does not exist: " + remoteSourceFile);
			}

			// build full file name
			Path targetFile = Paths.get(localTargetDirectory.getAbsolutePath(), getLastPathElement(remoteSourceFile));

			long length = Files.copy(in, targetFile);
			if (length == 0) {
				throw new IOException("Transfert interrupted.");
			} else {
				log.debug("File '{}' transfered from FTP server with success.", remoteSourceFile);
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	@Override
	protected void uploadFile(File localFile, String remoteTargetDirectory, ConnectionSession session,
			ProgressHandler handler) throws Exception {
		log.debug("Send local file '{}' into remote directory '{}'", localFile.getAbsolutePath(),
				remoteTargetDirectory);
		InputStream in = null;
		try {
			in = new FileInputStream(localFile);
			if (((FtpConnectionSession) session).getFtp().storeFile(localFile.getName(), in)) {
				log.debug("File '{}' transfered to FTP server with success.", localFile.getAbsolutePath());
			} else {
				throw new IOException("Upload of local file '" + localFile.getAbsolutePath()
						+ "' into the remote directory '" + remoteTargetDirectory + "' failed.");
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Throwable t) {
					log.warn("Error when closing FileInutStream of file '" + localFile.getAbsolutePath() + "': "
							+ t.getMessage(), t);
				}
			}
		}

	}

	@Override
	protected boolean changeRemoteDirectory(String directory, ConnectionSession session) throws Exception {
		return ((FtpConnectionSession) session).getFtp().changeWorkingDirectory(directory);
	}

	@Override
	protected void createRemoteDirectory(String directory, ConnectionSession session) throws Exception {
		log.debug("Creating remote FTP directory '{}' ", directory);
		try {
			((FtpConnectionSession) session).getFtp().makeDirectory(directory);
		} catch (Exception e) {
			log.debug("Directory '{}' already exists.", directory);
		}
	}

	@Override
	protected Logger log() {
		return log;
	}
}

class FtpConnectionSession extends ConnectionSession {

	private FTPClient ftpClient;

	public FtpConnectionSession(FTPClient ftp) {
		this.ftpClient = ftp;
	}

	@Override
	protected void forceClose() {
		synchronized (this) {
			if (used) {
				markAsToClose = true;
				return;
			}
		}
		try {
			ftpClient.logout();
		} catch (Exception e) {
			// do nothing
		}
		if (ftpClient.isConnected()) {
			try {
				ftpClient.disconnect();
			} catch (IOException ioe) {
				// do nothing
			}
		}
	}

	public FTPClient getFtp() {
		return ftpClient;
	}
}

class FtpFileEntry implements FileEntry {
	FTPFile ftpFile;

	public FtpFileEntry(FTPFile ftpFile) {
		this.ftpFile = ftpFile;
	}

	@Override
	public String getFilename() throws IOException {
		return ftpFile.getName();
	}

	@Override
	public boolean isDir() throws IOException {
		return ftpFile.isDirectory();
	}

	@Override
	public long getSize() throws IOException {
		return ftpFile.getSize();
	}

	@Override
	public long geLastModified() throws IOException {
		return ftpFile.getTimestamp().getTimeInMillis();
	}

}