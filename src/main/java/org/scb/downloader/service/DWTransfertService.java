package org.scb.downloader.service;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.scb.downloader.domain.DWFileInfo;
import org.scb.downloader.domain.DWHostAccount;
import org.scb.downloader.domain.DWTransfert;
import org.scb.downloader.domain.enumeration.DWTransfertScheduling;
import org.scb.downloader.domain.enumeration.TransfertStatus;
import org.scb.downloader.repository.DWHostAccountRepository;
import org.scb.downloader.repository.DWTransfertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing the DWTransfert instances.
 * 
 * @author S.Chassande
 */
@Service
@Transactional
public class DWTransfertService {

	private final Logger log = LoggerFactory.getLogger(DWTransfertService.class);

	/**
	 * The maximal of transfert attemp before setting the transfert status to
	 * error.
	 */
	private int nbMaxAttemp = 3;

	private int errorMessageMaxLength = 4999;

	@Autowired
	private DWTransfertRepository transfertRepository;
	@Autowired
	private DWHostAccountRepository hostAccountRepository;

	/**
	 * A transfert has been started. It registers the start time if not done and
	 * set the status to DOING.
	 * 
	 * @param transfert
	 *            is the started transfert.
	 */
	public void transfertStarted(DWTransfert transfert) {
		if (transfert.getStatus() != TransfertStatus.DOING) {
			log.debug("Change to DOING status of transfert: {}", transfert.getId());
			transfert.setStatus(TransfertStatus.DOING);
			transfert.setStart(new Date());
			transfertRepository.save(transfert);
		}
	}

	/**
	 * A transfert has been finished. It registers the end time if not done and
	 * set the status to DONE.
	 * 
	 * @param transfert
	 *            is the finished transfert.
	 */
	public void transfertFinished(DWTransfert transfert) {
		// change the status and the end time
		log.debug("Change to DONE status of transfert: {}", transfert.getId());
		transfert.setStatus(TransfertStatus.DONE);
		transfert.setEnd(new Date());
		transfertRepository.save(transfert);
	}

	/**
	 * An error occurs during a transfert . It registers the error message
	 * (append in new line) and increment the number of error. If the number of
	 * error reach the threshold of the number of attempt then the status of the
	 * transfert is changed to ERROR.
	 * 
	 * @param transfert
	 *            is the transfert having a new error.
	 */
	public void transfertOnError(DWTransfert transfert, String errorMessage) {
		String errorMessages = transfert.getErrorMessages();
		if (errorMessages == null) {
			errorMessages = "";
		} else {
			errorMessages += "\n";
		}
		int nbError = transfert.getNbError();
		nbError++;
		errorMessage = nbError + ':' + errorMessage;
		errorMessages = errorMessage + "\n" + errorMessages;

		// limit the error message length
		if (errorMessages.length() > this.errorMessageMaxLength) {
			errorMessages = errorMessages.substring(0, Math.min(errorMessageMaxLength, errorMessages.length()));
		}

		// Store data
		transfert.setNbError(nbError);
		transfert.setErrorMessages(errorMessages);

		if (transfert.getNbError() >= nbMaxAttemp) {
			log.debug("Change to ERROR status of transfert: {}", transfert.getId());
			transfert.setStatus(TransfertStatus.ERROR);
		}
		transfertRepository.save(transfert);
	}

	/**
	 * Find the next transfert to perform depending on the transfert status and
	 * its scheduling.
	 * 
	 * @param status
	 *            the
	 * @return
	 */
	public DWTransfert findNextToPerform() {
		DWTransfert transfert = findNextToPerform(TransfertStatus.DOING);
		if (transfert == null) {
			transfert = findNextToPerform(TransfertStatus.CREATED);
		}
		return transfert;
	}

	public DWTransfert getTransfert(Long transfertId) {
		return transfertRepository.getOne(transfertId);
	}

	private DWTransfert findNextToPerform(TransfertStatus status) {
		log.debug("Searching next transfert with status '{}' to perform...", status);
		// first search an immediat transfert to perform
		DWTransfert transfert = transfertRepository.findNext(status, DWTransfertScheduling.IMMEDIATLY);
		if (transfert == null) {
			log.debug("No immediat transfert with status to perform. Searching windowed transfert to perform...",
					status);
			transfert = transfertRepository.findNext(status, DWTransfertScheduling.EVERY_DAY_WINDOW,
					DWTransfert.getInstantOfDay(ZonedDateTime.now()));
		}
		if (transfert == null) {
			log.debug("No transfert to perform found with status {}.", status);
		} else {
			log.debug("Transfert to perform found with status {}: {}", status, transfert.getId());
		}
		return transfert;
	}

	public void create(List<DWTransfert> transferts) {
		for (DWTransfert transfert : transferts) {
			getAccount(transfert.getSource());
			getAccount(transfert.getTarget());
			log.info("Create transfert from {} to {}", transfert.getSource(), transfert.getTarget());
		}
		transfertRepository.save(transferts);
	}

	private void getAccount(DWFileInfo fi) {
		DWHostAccount account = hostAccountRepository.getOne(fi.getAccount().getId());
		fi.setAccount(account);
	}

	public void update(DWTransfert transfert) {
		transfertRepository.save(transfert);
	}
}
