package org.scb.downloader.service;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.scb.downloader.domain.DWTransfert;
import org.scb.downloader.domain.enumeration.DWProtocol;
import org.scb.downloader.service.ConnectionService.ConnectionSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.io.Files;

@Service
public class TransfertSchedulerService {

	/** Logger */
	protected static final Logger log = LoggerFactory.getLogger(TransfertSchedulerService.class);

	@Autowired
	DWTransfertService transfertService;
	@Autowired
	private ConnectionService localFileSystemService;
	@Autowired
	private ConnectionService sshService;
	@Autowired
	private ConnectionService ftpService;

	private String tempDirectory = null;

	private static Object synchro = new Object();
	private boolean transfering = false;

	/**
	 * Lookup up transferts to perform and execute them one by one until no more
	 * transfert can be launch now. This method is scheduled every 5 minutes
	 * after the end of the last completion.
	 */
	@Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 20 * 1000)
	public void runPersistentTransferts() {
		beginRun();
		try {
			log.debug("Lookup transferts to perform");
			DWTransfert transfert = null;
			do {
				transfert = transfertService.findNextToPerform();
				if (transfert != null) {
					runPersistentTransfert(transfert);
				}
			} while (transfert != null);
		} finally {
			endRun();
		}
	}

	public DWTransfert runPersistentTransfert(Long transfertId) throws Exception {
		beginRun();
		try {
			DWTransfert transfert = transfertService.getTransfert(transfertId);
			if (transfert == null) {
				throw new Exception("Transfert '" + transfertId + "' does not exist.");
			}
			runPersistentTransfert(transfert);
			return transfert;
		} finally {
			endRun();
		}
	}

	private void runPersistentTransfert(DWTransfert transfert) {
		transfertService.transfertStarted(transfert);
		try {
			// perform the transfert
			performTransfert(transfert);

			transfertService.transfertFinished(transfert);
		} catch (Exception e) {
			log.warn("Error during transfert '" + transfert.getId() + "': " + e.getMessage(), e);
			transfertService.transfertOnError(transfert, e.getMessage());
		}
	}

	private void beginRun() {
		synchronized (synchro) {
			if (transfering) {
				log.warn("Ttransferts is already running.");
			} else {
				transfering = true;
			}
		}
	}

	private void endRun() {
		synchronized (synchro) {
			transfering = false;
		}
	}

	static class TransfertProgressHandler implements ProgressHandler {
		DWTransfert transfert;
		long cumulSize = 0;
		long size = 0;
		long previousDownloaded = 0;
		int percent = 0;
		DWTransfertService transfertService;

		public TransfertProgressHandler(DWTransfert transfert, DWTransfertService transfertService) {
			this.transfert = transfert;
			this.transfertService = transfertService;
		}

		@Override
		public void start(String file, long max) {
			this.cumulSize += max;
			this.size = max;
			transfert.setFileSize(cumulSize);
			String files = transfert.getDownloadedFiles();
			if (files == null) {
				files = file;
			} else {
				files += ", " + file;
			}
			transfert.setDownloadedFiles(files);
			if (transfert.getId() != null) {
				transfertService.update(transfert);
			}
		}

		@Override
		public boolean count(long count) {
			int newPercent = (int) ((count * 100l) / size);
			if (percent < newPercent) {
				transfert.setDownloaded(previousDownloaded + count);
				if (transfert.getId() != null) {
					transfertService.update(transfert);
				}
			}
			return false;
		}

		@Override
		public void end() {
			this.previousDownloaded += size;
		}
	}

	/**
	 * Performs a transfert.
	 * 
	 * @param transfert
	 * @throws Exception
	 */
	public <S extends ConnectionSession> void performTransfert(DWTransfert transfert) throws Exception {
		checkTransfert(transfert);
		final DWProtocol srcProtocol = transfert.getSource().getAccount().getProtocol();
		final DWProtocol targetProtocol = transfert.getTarget().getAccount().getProtocol();

		log.info("Perform transfert: {} between {} and {}", transfert.getId(), //
				transfert.getSource(), transfert.getTarget());
		DateTime begin = new DateTime();

		if (srcProtocol == DWProtocol.LOCAL_FILE_SYSTEM) {
			// Source file is on local file system
			ConnectionService targetCS = getConnectionService(targetProtocol);
			ConnectionSession session = targetCS.connect(transfert.getTarget().getAccount());
			try {
				targetCS.upload(session, transfert.getSource().getPath(), transfert.getTarget(),
						new TransfertProgressHandler(transfert, transfertService));
			} finally {
				if (session != null) {
					session.close();
				}
			}
		} else if (targetProtocol == DWProtocol.LOCAL_FILE_SYSTEM) {
			// target is the local file system
			ConnectionService sourceCS = getConnectionService(srcProtocol);
			ConnectionSession session = sourceCS.connect(transfert.getSource().getAccount());
			try {
				sourceCS.download(session, transfert.getSource(), transfert.getTarget().getPath(),
						new TransfertProgressHandler(transfert, transfertService));
			} finally {
				if (session != null) {
					session.close();
				}
			}
		} else {
			// source and target are on remote systems.
			// => Use a local temporary copy.
			ConnectionService sourceCS = getConnectionService(srcProtocol);
			ConnectionService targetCS = getConnectionService(targetProtocol);
			transfertViaLocal(transfert, sourceCS, targetCS);
		}
		DateTime end = new DateTime();
		log.info("Transfert perfomed with success: {} between {} and {} in {} days, {} hours, {} minutes, {} seconds.",
				transfert.getId(), //
				transfert.getSource(), transfert.getTarget(), //
				Days.daysBetween(begin, end).getDays(), //
				Hours.hoursBetween(begin, end).getHours() % 24, //
				Minutes.minutesBetween(begin, end).getMinutes() % 60, //
				Seconds.secondsBetween(begin, end).getSeconds() % 60);
	}

	private void checkTransfert(DWTransfert transfert) throws Exception {
		if (transfert.getSource() == null) {
			throw new Exception("transfert(" + transfert.getId() + ").source field is null");
		}
		if (transfert.getSource().getAccount() == null) {
			throw new Exception("transfert(" + transfert.getId() + ").source.account field is null");
		}
		if (transfert.getSource().getPath() == null) {
			throw new Exception("transfert(" + transfert.getId() + ").source.path field is null");
		}
		if (transfert.getTarget() == null) {
			throw new Exception("transfert(" + transfert.getId() + ").target field is null");
		}
		if (transfert.getTarget().getAccount() == null) {
			throw new Exception("transfert(" + transfert.getId() + ").target.account field is null");
		}
		if (transfert.getTarget().getPath() == null) {
			throw new Exception("transfert(" + transfert.getId() + ").target.path field is null");
		}
	}

	private void transfertViaLocal(DWTransfert transfert, ConnectionService srcCS, ConnectionService targetCS)
			throws Exception {
		DateTime begin = new DateTime();

		// Define a local tmp file
		final String localTmpFile = generateTmpFileName(transfert.getId().toString());

		// transfert from the source remote file to local tmp file
		log.debug("Perform 1st transfert part: {} between {} and {}", transfert.getId(), //
				transfert.getSource().getPath(), localTmpFile);
		ConnectionSession srcSession = srcCS.connect(transfert.getTarget().getAccount());
		try {
			srcCS.download(srcSession, transfert.getSource(), localTmpFile,
					new TransfertProgressHandler(transfert, transfertService));
		} finally {
			if (srcSession != null) {
				srcSession.close();
			}
		}
		DateTime end = new DateTime();
		log.info(
				"1st transfert part perfomed with success: {} between {} and {} in {} days, {} hours, {} minutes, {} seconds.",
				transfert.getId(), //
				transfert.getSource(), localTmpFile, //
				Days.daysBetween(begin, end).getDays(), //
				Hours.hoursBetween(begin, end).getHours() % 24, //
				Minutes.minutesBetween(begin, end).getMinutes() % 60, //
				Seconds.secondsBetween(begin, end).getSeconds() % 60);

		// transfert from the local tmp file to the target remote account
		log.debug("Perform 2nd transfert part: {} between {} and {}", transfert.getId(), //
				localTmpFile, transfert.getSource().getPath());
		ConnectionSession targetSession = targetCS.connect(transfert.getTarget().getAccount());
		try {
			targetCS.upload(targetSession, localTmpFile, transfert.getTarget(),
					new TransfertProgressHandler(transfert, transfertService));
		} finally {
			if (targetSession != null) {
				targetSession.close();
			}
		}
	}

	private String generateTmpFileName(String prefix) {
		if (tempDirectory == null) {
			tempDirectory = Files.createTempDir().getAbsolutePath();
		}
		return new File(this.tempDirectory, prefix + '_' + ZonedDateTime.now().toEpochSecond()).getAbsolutePath();
	}

	private ConnectionService getConnectionService(DWProtocol protocol) throws Exception {
		switch (protocol) {
		case LOCAL_FILE_SYSTEM:
			return localFileSystemService;
		case SSH:
			return sshService;
		case FTP:
			return ftpService;
		default:
			throw new Exception("Protocol is not managed: " + protocol);
		}
	}

	private String displayErrorForWeb(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		String stackTrace = sw.toString();
		return stackTrace.replace(System.getProperty("line.separator"), "<br/>\n");
	}

	public DWTransfertService getTransfertService() {
		return transfertService;
	}

	public void setTransfertService(DWTransfertService transfertService) {
		this.transfertService = transfertService;
	}

	public ConnectionService getLocalFileSystemService() {
		return localFileSystemService;
	}

	public void setLocalFileSystemService(ConnectionService localFileSystemService) {
		this.localFileSystemService = localFileSystemService;
	}

	public ConnectionService getSshService() {
		return sshService;
	}

	public void setSshService(ConnectionService sshService) {
		this.sshService = sshService;
	}

	public ConnectionService getFtpService() {
		return ftpService;
	}

	public void setFtpService(FtpService ftpService) {
		this.ftpService = ftpService;
	}

	public String getTempDirectory() {
		return tempDirectory;
	}

	public void setTempDirectory(String tempDirectory) {
		this.tempDirectory = tempDirectory;
	}
}
