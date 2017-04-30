package org.scb.downloader.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.FileSystemView;
import org.apache.sshd.common.file.SshFile;
import org.apache.sshd.common.file.nativefs.NativeFileSystemFactory;
import org.apache.sshd.common.file.nativefs.NativeFileSystemView;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.apache.sshd.sftp.subsystem.SftpSubsystem;
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

import com.jcraft.jsch.JSchException;

public class SshServiceTest implements PasswordAuthenticator {

	protected static final Logger log = LoggerFactory.getLogger(SshService.class);

	private final static String LOGIN_1 = "Clive";
	private final static String PASSWORD_1 = "Johnson";

	private final static int SSHD_PORT = 2222;
	private Map<String, String> login2password = new HashMap<String, String>();
	private SshServer sshd;
	private SshService sshService;

	@Before
	public void init() throws IOException {
		log.debug("Creating SSHD server ...");
		// init SSHD server
		sshd = SshServer.setUpDefaultServer();
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
		sshd.setPasswordAuthenticator(this);
		sshd.setPort(SSHD_PORT);
		sshd.setShellFactory(new ProcessShellFactory(new String[] { "/bin/sh", "-i", "-l" }));
		sshd.setFileSystemFactory(new NativeFileSystemFactory() {
			@Override
			public FileSystemView createFileSystemView(final org.apache.sshd.common.Session session) {
				return new NativeFileSystemView(session.getUsername(), false) {
					@Override
					protected SshFile getFile(String dir, String file) {
						SshFile sshFile = super.getFile(dir, file);
						// log.debug("### getFile({},{})=>{}", dir, file,
						// sshFile.getAbsolutePath());
						return sshFile;
					}

					@Override
					public SshFile getFile(String file) {
						SshFile sshFile = super.getFile(file);
						// log.debug("### getFile({})=>{}", file,
						// sshFile.getAbsolutePath());
						return sshFile;
					}
				};
			};
		});
		sshd.setCommandFactory(new ScpCommandFactory());

		List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
		namedFactoryList.add(new SftpSubsystem.Factory());
		sshd.setSubsystemFactories(namedFactoryList);

		sshd.start();

		// Create account on SSH server
		login2password.put(LOGIN_1, PASSWORD_1);

		// create SshService
		sshService = new SshService();
	}

	@After
	public void tearDown() throws InterruptedException {
		log.debug("Killing SSHD server ...");
		sshd.stop(true);
		sshd = null;
		sshService = null;
	}

	@Override
	public boolean authenticate(String login, String password, ServerSession session) {
		return password != null && login != null && password.equals(login2password.get(login));
	}

	@Test
	public void testConnection() throws Exception {
		DWHostAccount account = buildAcount(LOGIN_1, PASSWORD_1);
		sshService.connect(account).close();
	}

	@Test
	public void testConnectionRealServer() throws Exception {
		DWHostAccount account = buildAcountRealServer();
		sshService.connect(account).close();
	}

	@Test
	public void testConnectionWrongPassword() throws Exception {
		DWHostAccount account = buildAcount1();
		account.setPassword("wrong");
		try {
			sshService.connect(account);
			fail("Connection should fail with wrong password");
		} catch (JSchException e) {
			log.debug(e.getMessage());
		}
	}

	@Test
	public void testConnectionWrongPasswordRealServer() throws Exception {
		DWHostAccount account = buildAcountRealServer();
		account.setPassword("wrong");
		try {
			sshService.connect(account);
			fail("Connection should fail with wrong password");
		} catch (JSchException e) {
			log.debug(e.getMessage());
		}
	}

	@Test
	public void testList() throws Exception {
		final String targetDirStr = buildTempDirString();
		log.debug("Creating 4 tmp files in directory '{}' ...", targetDirStr);
		final File targetDir = new File("." + targetDirStr);
		final List<File> tmpFiles = createTempFiles(targetDir, 50, 100, 150, 200);
		DWHostAccount account = buildAcount1();
		ConnectionSession session = sshService.connect(account);
		try {
			List<FileEntry> files = sshService.listDir(session, targetDirStr, false);
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
	public void testList_RealServer() throws Exception {
		DWHostAccount account = buildAcountRealServer();
		ConnectionSession session = sshService.connect(account);
		try {
			List<FileEntry> files = sshService.listDir(session, ".", false);
			if (files.isEmpty()) {
				log.warn("No file found !!");
			} else {
				for (FileEntry file : files) {
					log.debug(file.getFilename() + "\t" + file.geLastModified() + "\t" + file.getSize() + "\t"
							+ file.isDir());
				}
			}
		} finally {
			session.close();
		}
	}

	@Test
	public void testTransfertTo_OneFile_RealServer() throws Exception {
		final File tmpDir = new File("." + buildTempDirString()).getAbsoluteFile();
		final File tmpDir2 = new File("." + buildTempDirString()).getAbsoluteFile();
		final File sourceFile = createTempFile(tmpDir, 2000);
		log.debug("Source file: '{}' ", sourceFile.getAbsolutePath());

		DWHostAccount account = buildAcountRealServer();

		String remotePath = "./tmp/" + ((int) (Math.random() * 10000000)) + "/";
		ConnectionSession session = null;
		try {
			session = sshService.connect(account);
			sshService.upload(session, sourceFile.getAbsolutePath(), buildFileInfo(account, remotePath));

			sshService.download(session, buildFileInfo(account, remotePath + sourceFile.getName()),
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

	@Test
	public void testTransfertTo_OneFile_Sshd() throws Exception {
		final File tmpDir = new File("." + buildTempDirString()).getAbsoluteFile();
		final File tmpDir2 = new File("." + buildTempDirString()).getAbsoluteFile();
		final File sourceFile = createTempFile(tmpDir, 2000);
		log.debug("Source file: '{}' ", sourceFile.getAbsolutePath());

		DWHostAccount account = buildAcount1();

		String remotePath = buildTempDirString() + '/';
		ConnectionSession session = null;
		try {
			session = sshService.connect(account);
			sshService.upload(session, sourceFile.getAbsolutePath(), buildFileInfo(account, remotePath));

			sshService.download(session, buildFileInfo(account, remotePath + sourceFile.getName()),
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

	@Test
	public void testTransfertTo_Directory_RealServer() throws Exception {
		final File sendDir = new File("." + buildTempDirString()).getAbsoluteFile();
		final File receivDir = new File("." + buildTempDirString()).getAbsoluteFile();
		final List<File> sourceFiles = createTempFiles(sendDir, 2000, 3000, 4000);
		log.debug("Send dir: '{}' ", sendDir.getAbsolutePath());

		DWHostAccount account = buildAcountRealServer();

		String remotePath = "./tmp/" + ((int) (Math.random() * 10000000)) + "/";
		ConnectionSession session = null;
		try {
			session = sshService.connect(account);
			sshService.upload(session, sendDir.getAbsolutePath(), buildFileInfo(account, remotePath));

			sshService.download(session, buildFileInfo(account, remotePath + sendDir.getName()),
					receivDir.getAbsolutePath());

			for (File sourceFile : sourceFiles) {
				File receivedFile = new File(new File(receivDir, sendDir.getName()), sourceFile.getName());
				assertTrue("Received file '" + receivedFile.getAbsolutePath() + "' does not exist",
						receivedFile.exists());
				assertEquals("Bad file length ", sourceFile.length(), receivedFile.length());
			}
		} finally {
			try {
				session.close();
			} catch (Throwable t) {
			}
			log.debug("Removing tmp files");
			try {
				FileUtils.deleteDirectory(sendDir);
			} catch (Throwable t) {
			}
			try {
				FileUtils.deleteDirectory(receivDir);
			} catch (Throwable t) {
			}
		}
	}

	@Test
	public void testTransfertTo_Directory_Shhd() throws Exception {
		final File sendDir = new File("." + buildTempDirString()).getAbsoluteFile();
		final File receivDir = new File("." + buildTempDirString()).getAbsoluteFile();
		final List<File> sourceFiles = createTempFiles(sendDir, 2000, 3000, 4000);
		log.debug("Send dir: '{}' ", sendDir.getAbsolutePath());

		DWHostAccount account = buildAcount1();

		String remotePath = buildTempDirString() + '/';
		ConnectionSession session = null;
		try {
			session = sshService.connect(account);
			sshService.upload(session, sendDir.getAbsolutePath(), buildFileInfo(account, remotePath));

			sshService.download(session, buildFileInfo(account, remotePath + sendDir.getName()),
					receivDir.getAbsolutePath());
			checkReceivedFiles(sourceFiles, sendDir.getName(), receivDir);
		} finally {
			try {
				session.close();
			} catch (Throwable t) {
			}
			log.debug("Removing tmp files");
			try {
				FileUtils.deleteDirectory(sendDir);
			} catch (Throwable t) {
			}
			try {
				FileUtils.deleteDirectory(receivDir);
			} catch (Throwable t) {
			}
		}
	}

	@Test
	public void testTransfertTo_Tree_Shhd() throws Exception {
		final File sendDir = new File("." + buildTempDirString()).getAbsoluteFile();
		final List<File> sourceFiles = createTempFiles(sendDir, 2000, 3000, 4000);
		File fooDir = new File(sendDir, "foo");
		final List<File> fooFiles = createTempFiles(fooDir, 2100, 3100, 4100);
		File barDir = new File(sendDir, "bar");
		final List<File> barFiles = createTempFiles(barDir, 2200, 3200, 4200);
		File joeDir = new File(fooDir, "joe");
		final List<File> joeFiles = createTempFiles(joeDir, 2300, 3300, 4300);

		final File receivDir = new File("." + buildTempDirString()).getAbsoluteFile();

		DWHostAccount account = buildAcount1();

		String remotePath = buildTempDirString() + '/';
		log.debug("Send dir: '{}' ", sendDir.getAbsolutePath());
		log.debug("Remote dir: '{}' ", remotePath);
		log.debug("Receiv dir: '{}' ", receivDir.getAbsolutePath());
		ConnectionSession session = null;
		try {
			session = sshService.connect(account);
			sshService.upload(session, sendDir.getAbsolutePath(), buildFileInfo(account, remotePath));

			sshService.download(session, buildFileInfo(account, remotePath + sendDir.getName()),
					receivDir.getAbsolutePath());
			checkReceivedFiles(sourceFiles, sendDir.getName(), receivDir);
			checkReceivedFiles(fooFiles, sendDir.getName() + '/' + fooDir.getName(), receivDir);
			checkReceivedFiles(barFiles, sendDir.getName() + '/' + barDir.getName(), receivDir);
			checkReceivedFiles(joeFiles, sendDir.getName() + '/' + fooDir.getName() + '/' + joeDir.getName(),
					receivDir);
		} finally {
			try {
				session.close();
			} catch (Throwable t) {
			}
			log.debug("Removing tmp files");
			try {
				FileUtils.deleteDirectory(sendDir);
			} catch (Throwable t) {
			}
			try {
				FileUtils.deleteDirectory(receivDir);
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
		return buildAcount("localhost", SSHD_PORT, login, password);
	}

	private DWHostAccount buildAcount1() {
		return buildAcount("localhost", SSHD_PORT, LOGIN_1, PASSWORD_1);
	}

	private DWHostAccount buildAcountRealServer() {
		return buildAcount("brontes.feralhosting.com", 26975, "chassande", "CoS6q4oWgFLdfK8cNOGt");
	}

	private DWHostAccount buildAcount(String host, int port, String login, String password) {
		DWHostAccount account = new DWHostAccount();
		account.setProtocol(DWProtocol.SSH);
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
		return "/target/test/SshdService/" + ((int) (Math.random() * 10000));
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
