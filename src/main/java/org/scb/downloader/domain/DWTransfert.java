package org.scb.downloader.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.scb.downloader.domain.enumeration.DWTransfertScheduling;
import org.scb.downloader.domain.enumeration.TransfertStatus;

/**
 * A file Transfert between one source file and one target path.
 */
@Entity
@Table(name = "dw_transfert")
public class DWTransfert implements Serializable {

	private static final long serialVersionUID = 1L;

	public static Long getInstantOfDay(ZonedDateTime dateTime) {
		return dateTime == null ? null
				: new Long(dateTime.getHour() * 3600 + dateTime.getMinute() * 60 + dateTime.getSecond());
	}

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/** The status of the transfert */
	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS")
	@NotNull
	private TransfertStatus status;

	/** The rank of the transfert */
	@Column(name = "rank")
	private long rank;

	/** The source of the transfert */
	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "path", column = @Column(name = "SOURCE_PATH")) })
	@AssociationOverrides({
			@AssociationOverride(name = "account", joinColumns = @JoinColumn(name = "SOURCE_ACCOUNT")) })
	private DWFileInfo source;

	/** The target of the transgfert */
	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "path", column = @Column(name = "TARGET_PATH")) })
	@AssociationOverrides({
			@AssociationOverride(name = "account", joinColumns = @JoinColumn(name = "TARGET_ACCOUNT")) })
	private DWFileInfo target;

	/** The scheduliing of the transfert */
	@Enumerated(EnumType.STRING)
	@Column(name = "SCHEDULING")
	@NotNull
	private DWTransfertScheduling scheduling;

	@Column(name = "DAY_BEGIN")
	private Long dayBegin;

	@Column(name = "DAY_END")
	private Long dayEnd;

	/** The start time of the transfert */
	@Column(name = "START_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date start;

	/** The end time of the transfert */
	@Column(name = "END_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date end;

	/** The list of error messages (one per line) */
	@Column(name = "ERRORS")
	private String errorMessages;

	/** The number of errors */
	@Column(name = "NB_ERROR")
	private int nbError = 0;

	/** The file size */
	@Column(name = "FILE_SIZE")
	private Long fileSize = 0l;

	/** The size of the downloaded data */
	@Column(name = "DOWNLOADED")
	private Long downloaded = 0l;

	/** The list of error messages (one per line) */
	@Column(name = "DOWNLOADED_FILES")
	private String downloadedFiles;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public DWFileInfo getSource() {
		return source;
	}

	public void setSource(DWFileInfo dwFileInfo) {
		this.source = dwFileInfo;
	}

	public DWFileInfo getTarget() {
		return target;
	}

	public void setTarget(DWFileInfo dwFileInfo) {
		this.target = dwFileInfo;
	}

	public DWTransfertScheduling getScheduling() {
		return scheduling;
	}

	public void setScheduling(DWTransfertScheduling scheduling) {
		this.scheduling = scheduling;
	}

	public TransfertStatus getStatus() {
		return status;
	}

	public void setStatus(TransfertStatus status) {
		this.status = status;
	}

	public long getRank() {
		return rank;
	}

	public void setRank(long rank) {
		this.rank = rank;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public Long getDayBegin() {
		return dayBegin;
	}

	public void setDayBegin(Long dayBegin) {
		this.dayBegin = dayBegin;
	}

	public Long getDayEnd() {
		return dayEnd;
	}

	public void setDayEnd(Long dayEnd) {
		this.dayEnd = dayEnd;
	}

	public String getErrorMessages() {
		return errorMessages;
	}

	public void setErrorMessages(String errorMessages) {
		this.errorMessages = errorMessages;
	}

	public int getNbError() {
		return nbError;
	}

	public void setNbError(int nbError) {
		this.nbError = nbError;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public Long getDownloaded() {
		return downloaded;
	}

	public void setDownloaded(Long downloaded) {
		this.downloaded = downloaded;
	}

	public String getDownloadedFiles() {
		return downloadedFiles;
	}

	public void setDownloadedFiles(String downloadedFiles) {
		this.downloadedFiles = downloadedFiles;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DWTransfert dWTransfert = (DWTransfert) o;
		if (dWTransfert.id == null || id == null) {
			return false;
		}
		return Objects.equals(id, dWTransfert.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public String toString() {
		return "DWTransfert{" + "id=" + id + ", status='" + status + "'" + ", rank='" + rank + "'"//
				+ ", scheduling='" + scheduling + "'" + ", dayBegin='" + dayBegin + "'" + ", dayEnd='" + dayEnd + "'" //
				+ ", start='" + start + "'" + ", end='" + end + "'" //
				+ ", source='" + source + ", target='" + target //
				+ ", errorMessages='" + errorMessages + "'" + ", nbError='" + nbError + "'" + '}';
	}
}
