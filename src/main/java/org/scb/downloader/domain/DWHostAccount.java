package org.scb.downloader.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.scb.downloader.domain.enumeration.DWProtocol;

/**
 * A DWHostAccount.
 */
@Entity
@Table(name = "dw_host_account")
public class DWHostAccount implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "HOST")
	private String host;

	@Column(name = "PORT")
	private String port;

	@Column(name = "USER_NAME")
	private String userName;

	@Column(name = "PASSWORD")
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(name = "PROTOCOL")
	@NotNull
	private DWProtocol protocol;

	@Column(name = "SEPARATOR")
	private String pathSeparator = "/";

	@Column(name = "DEFAULT_PATH")
	private String defaultPath = ".";

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public DWProtocol getProtocol() {
		return protocol;
	}

	public void setProtocol(DWProtocol protocol) {
		this.protocol = protocol;
	}

	public String getPathSeparator() {
		return pathSeparator;
	}

	public void setPathSeparator(String pathSeparator) {
		this.pathSeparator = pathSeparator;
	}

	public String getDefaultPath() {
		return defaultPath;
	}

	public void setDefaultPath(String defaultPath) {
		this.defaultPath = defaultPath;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DWHostAccount dWHostAccount = (DWHostAccount) o;
		if (dWHostAccount.id == null || id == null) {
			return false;
		}
		return Objects.equals(id, dWHostAccount.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public String toString() {
		return "DWHostAccount{" + "id=" + id + ", host='" + host + "'" + ", port='" + port + "'" + ", userName='"
				+ userName + "'" + ", password='" + password + "'" + ", protocol='" + protocol + "'" + '}';
	}
}
