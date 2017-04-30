package org.scb.downloader.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.ftpserver.usermanager.impl.WriteRequest;
import org.bouncycastle.util.Arrays;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.scb.downloader.domain.DWFileInfo;
import org.scb.downloader.domain.DWHostAccount;
import org.scb.downloader.domain.enumeration.DWProtocol;
import org.scb.downloader.service.ConnectionService.ConnectionSession;
import org.scb.downloader.service.ConnectionService.FileEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpServiceTest {

	protected static final Logger log = LoggerFactory.getLogger(FtpServiceTest.class);

	private final static String LOGIN_1 = "Clive";
	private final static String PASSWORD_1 = "Johnson";

	private final static int FTPD_PORT = 2121;

	FtpServer ftpd;
	FtpService ftpService;

	@Before
	public void init() throws Exception {
		log.debug("Creating FTPD server ...");
		// init FTPD server
		FtpServerFactory serverFactory = new FtpServerFactory();
		ListenerFactory factory = new ListenerFactory();
		// set the port of the listener
		factory.setPort(FTPD_PORT);
		// replace the default listener
		serverFactory.addListener("default", factory.createListener());

		// Add users
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		// userManagerFactory.setFile(new File("ftpusers.properties"));
		userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());
		UserManager um = userManagerFactory.createUserManager();
		BaseUser user = new BaseUser();
		user.setName(LOGIN_1);
		user.setPassword(PASSWORD_1);
		user.setEnabled(true);
		user.authorize(new WriteRequest());
		List<Authority> authorities = new ArrayList<Authority>();
		authorities.add(new WritePermission());
		user.setAuthorities(authorities);
		user.setHomeDirectory(new File(".").getCanonicalPath());
		um.save(user);

		serverFactory.setUserManager(um);

		// start the server
		ftpd = serverFactory.createServer();
		ftpd.start();

		ftpService = new FtpService();
	}

	@After
	public void tearDown() throws InterruptedException {
		if (ftpd != null) {
			log.debug("Stopping FTPD server ...");
			ftpd.stop();
			ftpd = null;
		}
		ftpService = null;
	}

	@Test
	public void testConnectionFreebox() {
		ConnectionSession session = null;
		try {
			session = ftpService.connect(buildAcount("mafreebox.freebox.fr", 21, "anonymous", "anonymous"));
		} catch (Exception e) {
			log.error("Connection fail: " + e.getMessage(), e);
			fail("Connection fail: " + e.getMessage());
		}
		try {
			session.close();
		} catch (Exception e) {
			log.error("Disconnection fail: " + e.getMessage(), e);
			fail("Disconnection fail: " + e.getMessage());
		}
	}

	@Test
	public void testConnection() {
		ConnectionSession session = null;
		try {
			session = ftpService.connect(buildAcount1());
		} catch (Exception e) {
			log.error("Connection fail: " + e.getMessage(), e);
			fail("Connection fail: " + e.getMessage());
		}
		try {
			session.close();
		} catch (Exception e) {
			log.error("Disconnection fail: " + e.getMessage(), e);
			fail("Disconnection fail: " + e.getMessage());
		}
	}

	@Test
	public void testConnectionWrongPassword() throws Exception {
		DWHostAccount account = buildAcount1();
		account.setPassword("wrong");
		try {
			ftpService.connect(account);
			fail("Connection should fail with wrong password");
		} catch (IOException e) {
			log.debug(e.getMessage());
		}
	}

	@Test
	public void testList() throws Exception {
		final String targetDirStr = buildTempDirString();
		log.debug("Creating 4 tmp files in directory '{}' ...", targetDirStr);
		final File targetDir = new File("." + targetDirStr);
		final List<File> tmpFiles = createTempFiles(targetDir, 50, 100, 150, 200, 400);
		DWHostAccount account = buildAcount1();
		ConnectionSession session = ftpService.connect(account);
		try {
			List<FileEntry> files = ftpService.listDir(session, targetDirStr, false);
			assertNotNull("File list should not be null.", files);
			Assert.assertEquals("Wrong number of files", tmpFiles.size(), files.size());
			for (FileEntry file : files) {
				log.debug(file.getFilename() + "\t" + file.geLastModified() + "\t" + file.getSize() + "\t"
						+ file.isDir());
			}
		} finally {
			session.close();
			log.debug("Removing tmp files in directory '{}' ...", targetDirStr);
			for (File tmpFile : tmpFiles) {
				tmpFile.delete();
			}
			targetDir.delete();
		}
	}

	@Test
	public void testTransfertTo_OneFile_Ftpd() throws Exception {
		final File tmpDir = new File("." + buildTempDirString()).getCanonicalFile();
		final File tmpDir2 = new File("." + buildTempDirString()).getCanonicalFile();
		final File sourceFile = createTempFile(tmpDir, 2000);
		log.debug("Source file: '{}' ", sourceFile.getAbsolutePath());

		DWHostAccount account = buildAcount1();

		String remotePath = buildTempDirString() + "/";
		new File("." + remotePath).getCanonicalFile().mkdirs();
		ConnectionSession session = null;
		try {
			session = ftpService.connect(account);
			ftpService.upload(session, sourceFile.getCanonicalPath(), buildFileInfo(account, remotePath.substring(1)));

			ftpService.download(session, buildFileInfo(account, remotePath + sourceFile.getName()),
					tmpDir2.getAbsolutePath());
			File receivedFile = new File(tmpDir2, sourceFile.getName());
			assertTrue("Received file does not exist", receivedFile.exists());
			assertEquals("Bad file length", sourceFile.length(), receivedFile.length());
		} finally {
			try {
				session.close();
			} catch (Throwable t) {
			}

			log.debug("Removing tmp files");
			try {
				FileUtils.deleteDirectory(tmpDir);
			} catch (Throwable t) {
			}
			try {
				FileUtils.deleteDirectory(tmpDir2);
			} catch (Throwable t) {
			}
		}
	}

	private void checkReceivedFiles(List<File> sourceFiles, String hierarchy, File receivDir) {
		for (File sourceFile : sourceFiles) {
			File receivedFile = new File(new File(receivDir, hierarchy), sourceFile.getName());
			assertTrue("Received file '" + receivedFile.getAbsolutePath() + "' does not exist", receivedFile.exists());
			assertEquals("Bad file length ", sourceFile.length(), receivedFile.length());
		}
	}

	private DWHostAccount buildAcount(String login, String password) {
		return buildAcount("localhost", FTPD_PORT, login, password);
	}

	private DWHostAccount buildAcount1() {
		return buildAcount("localhost", FTPD_PORT, LOGIN_1, PASSWORD_1);
	}

	private DWHostAccount buildAcountRealServer() {
		return buildAcount("brontes.feralhosting.com", 26975, "chassande", "CoS6q4oWgFLdfK8cNOGt");
	}

	private DWHostAccount buildAcount(String host, int port, String login, String password) {
		DWHostAccount account = new DWHostAccount();
		account.setProtocol(DWProtocol.FTP);
		account.setHost(host);
		account.setPort(Integer.toString(port));
		account.setUserName(login);
		account.setPassword(password);
		return account;
	}

	private File createTempFile(File dir, Integer length) throws IOException {
		return createTempFiles(dir, length).get(0);
	}

	private DWFileInfo buildFileInfo(DWHostAccount account, String path) {
		DWFileInfo fi = new DWFileInfo();
		fi.setAccount(account);
		fi.setPath(path);
		return fi;
	}

	private String buildTempDirString() {
		return "/target/test/FtpService/" + ((int) (Math.random() * 10000));
	}

	private List<File> createTempFiles(File dir, Integer... lengths) throws IOException {
		dir.mkdirs();
		List<File> files = new ArrayList<>();
		for (Integer length : lengths) {
			File file = new File(dir, "temp_" + length + ".tmp");
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			byte[] data = new byte[length];
			Arrays.fill(data, length.byteValue());
			fos.write(data);
			fos.close();
			files.add(file);
		}
		return files;
	}
}
