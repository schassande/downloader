package org.scb.downloader.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

/**
 * A DWFileInfo.
 */
@Embeddable
public class DWFileInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String path;

	@ManyToOne
	private DWHostAccount account;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public DWHostAccount getAccount() {
		return account;
	}

	public void setAccount(DWHostAccount dwHostAccount) {
		this.account = dwHostAccount;
	}

	@Override
	public String toString() {
		return "DWFileInfo{" + "path='" + path + "'" + ", account='" + account + "'" + '}';
	}
}
