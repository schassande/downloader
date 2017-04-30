package org.scb.downloader.web.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scb.downloader.domain.DWHostAccount;
import org.scb.downloader.domain.enumeration.DWProtocol;
import org.scb.downloader.service.ConnectionService;
import org.scb.downloader.service.ConnectionService.ConnectionSession;
import org.scb.downloader.web.rest.dto.BrowseRequest;
import org.scb.downloader.web.rest.dto.BrowseResponse;
import org.scb.downloader.web.rest.dto.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

/**
 * REST controller for managing DWTransfert.
 */
@RestController
@RequestMapping("/file-browser")
public class FileBrowserResource {

	private final Logger log = LoggerFactory.getLogger(FileBrowserResource.class);

	@Autowired
	private ConnectionService localFileSystemService;
	@Autowired
	private ConnectionService sshService;
	@Autowired
	private ConnectionService ftpService;

	private Map<Long, SessionCacheEntry> account2session = new HashMap<Long, SessionCacheEntry>();

	/**
	 * GET /connect
	 */
	@RequestMapping(value = "/connect", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public void connect(@RequestBody DWHostAccount account) {
		log.debug("REST request to connect to {} as {}", account.getHost(), account.getUserName());
		try {
			ConnectionService cs = getConnectionService(account.getProtocol());
			SessionCacheEntry ce = account2session.get(account.getId());
			if (ce == null) {
				log.debug("Establish a new connection session");
				ce = new SessionCacheEntry(cs.connect(account));
				log.debug("Register the new connection session");
				account2session.put(account.getId(), ce);
			} else {
				log.debug("Connection session fetched.");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * GET /connect
	 */
	@RequestMapping(value = "/disconnect", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public void disconnect(@RequestBody DWHostAccount account) {
		log.debug("REST request to disconnect to {} as {}", account.getHost(), account.getUserName());
		try {
			SessionCacheEntry ce = account2session.get(account.getId());
			if (ce != null) {
				// forget session
				account2session.remove(account.getId());
				// Close connection session
				ce.closeOrMarkAsToClose();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * GET /browse :
	 */
	@RequestMapping(value = "/browse", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public BrowseResponse browse(@RequestBody BrowseRequest browseRequest) {
		DWHostAccount account = browseRequest.getAccount();
		log.debug("REST request to browse file on {}:{}", browseRequest.getAccount().getHost(),
				browseRequest.getPath());
		try {
			ConnectionService cs = getConnectionService(account.getProtocol());
			SessionCacheEntry ce = account2session.get(account.getId());
			if (ce == null) {
				log.debug("Establish a new connection session");
				ce = new SessionCacheEntry(cs.connect(account));
				account2session.put(account.getId(), ce);
			} else {
				log.debug("Connection session fetched.");
			}
			try {
				log.debug("Listing files in path {}", browseRequest.getPath());
				List<ConnectionService.FileEntry> files = cs.listDir(ce.getSession(), browseRequest.getPath(),
						browseRequest.getDirectoryOnly());
				log.debug("{} files found.", files.size());
				return new BrowseResponse(browseRequest, FileInfo.toFileInfo(files));
			} catch (Exception e) {
				account2session.remove(account.getId());
				ce.closeOrMarkAsToClose();
				throw e;
			}
		} catch (Exception e) {
			return null;
		}
	}

	@Scheduled(fixedDelay = 60 * 1000)
	public void cleanCache() {
		long limit = System.currentTimeMillis() - (10 * 60 * 1000);
		// find sessions to close
		Map<Long, SessionCacheEntry> toRemove = new HashMap<Long, SessionCacheEntry>();
		for (Map.Entry<Long, SessionCacheEntry> me : this.account2session.entrySet()) {
			if (me.getValue().getLastUsed() < limit) {
				toRemove.put(me.getKey(), me.getValue());
			}
		}

		// Close the sessions.
		for (Map.Entry<Long, SessionCacheEntry> me : toRemove.entrySet()) {
			account2session.remove(me.getKey());
			// Now the session cannot be used by a new asker
			// But the session could be still used => try to close the session
			// or mark it as it has to be closed.
			me.getValue().closeOrMarkAsToClose();
			log.info("Cache entry {} cleaned.", me.getKey());
		}
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

	public void setFtpService(ConnectionService ftpService) {
		this.ftpService = ftpService;
	}
}

class SessionCacheEntry {
	private long lastUsed = System.currentTimeMillis();
	private ConnectionSession session;

	public SessionCacheEntry(ConnectionSession session) {
		super();
		this.session = session;
	}

	public ConnectionSession getSession() {
		lastUsed = System.currentTimeMillis();
		return session;
	}

	public long getLastUsed() {
		return lastUsed;
	}

	public void closeOrMarkAsToClose() {
		session.closeOrMarkAsToClose();
	}
}