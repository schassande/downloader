package org.scb.downloader.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import org.scb.downloader.domain.DWFileInfo;
import org.scb.downloader.domain.DWHostAccount;
import org.scb.downloader.domain.DWTransfert;
import org.scb.downloader.domain.enumeration.TransfertStatus;
import org.scb.downloader.repository.DWHostAccountRepository;
import org.scb.downloader.repository.DWTransfertRepository;
import org.scb.downloader.service.DWTransfertService;
import org.scb.downloader.service.TransfertSchedulerService;
import org.scb.downloader.service.bo.MultipleTransferts;
import org.scb.downloader.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

/**
 * REST controller for managing DWTransfert.
 */
@RestController
@RequestMapping("/api")
public class DWTransfertResource {

	private final Logger log = LoggerFactory.getLogger(DWTransfertResource.class);

	@Inject
	private DWTransfertRepository dWTransfertRepository;
	@Inject
	private TransfertSchedulerService transfertSchedulerService;
	@Inject
	private DWTransfertService transfertService;
	@Inject
	private DWHostAccountRepository hostAccountRepository;

	/**
	 * POST /d-w-transferts : Create a new dWTransfert.
	 *
	 * @param dWTransfert
	 *            the dWTransfert to create
	 * @return the ResponseEntity with status 201 (Created) and with body the
	 *         new dWTransfert, or with status 400 (Bad Request) if the
	 *         dWTransfert has already an ID
	 * @throws URISyntaxException
	 *             if the Location URI syntax is incorrect
	 */
	@RequestMapping(value = "/d-w-transferts", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<DWTransfert> createDWTransfert(@RequestBody DWTransfert dWTransfert)
			throws URISyntaxException {
		log.debug("REST request to save DWTransfert : {}", dWTransfert);
		if (dWTransfert.getId() != null) {
			return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("dWTransfert", "idexists",
					"A new dWTransfert cannot already have an ID")).body(null);
		}
		getAccount(dWTransfert.getSource());
		getAccount(dWTransfert.getTarget());
		log.info("Create transfert from {} to {}", dWTransfert.getSource(), dWTransfert.getTarget());
		DWTransfert result = dWTransfertRepository.save(dWTransfert);
		return ResponseEntity.created(new URI("/api/d-w-transferts/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert("dWTransfert", result.getId().toString())).body(result);
	}

	private void getAccount(DWFileInfo fi) {
		DWHostAccount account = hostAccountRepository.getOne(fi.getAccount().getId());
		fi.setAccount(account);
	}

	/**
	 * POST /d-w-transferts : Create a new dWTransfert.
	 *
	 * @param dWTransfert
	 *            the dWTransfert to create
	 * @return the ResponseEntity with status 201 (Created) and with body the
	 *         new dWTransfert, or with status 400 (Bad Request) if the
	 *         dWTransfert has already an ID
	 * @throws URISyntaxException
	 *             if the Location URI syntax is incorrect
	 */
	@RequestMapping(value = "/multiple-d-w-transferts", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<MultipleTransferts> createMultipleDWTransfert(@RequestBody MultipleTransferts transferts)
			throws URISyntaxException {
		log.debug("REST request to save DWTransfert : {}", transferts);
		if (transferts.getTransferts().stream().anyMatch(t -> t.getId() != null)) {
			return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("dWTransfert", "idexists",
					"A new dWTransfert cannot already have an ID")).body(null);
		}
		transfertService.create(transferts.getTransferts());
		return ResponseEntity.ok(transferts);
	}

	/**
	 * PUT /d-w-transferts : Updates an existing dWTransfert.
	 *
	 * @param dWTransfert
	 *            the dWTransfert to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         dWTransfert, or with status 400 (Bad Request) if the dWTransfert
	 *         is not valid, or with status 500 (Internal Server Error) if the
	 *         dWTransfert couldnt be updated
	 * @throws URISyntaxException
	 *             if the Location URI syntax is incorrect
	 */
	@RequestMapping(value = "/d-w-transferts", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<DWTransfert> updateDWTransfert(@RequestBody DWTransfert dWTransfert)
			throws URISyntaxException {
		log.debug("REST request to update DWTransfert : {}", dWTransfert);
		if (dWTransfert.getId() == null) {
			return createDWTransfert(dWTransfert);
		}
		DWTransfert result = dWTransfertRepository.save(dWTransfert);
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityUpdateAlert("dWTransfert", dWTransfert.getId().toString()))
				.body(result);
	}

	/**
	 * GET /d-w-transferts : get all the dWTransferts.
	 *
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         dWTransferts in body
	 */
	@RequestMapping(value = "/d-w-transferts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public List<DWTransfert> getAllDWTransferts() {
		log.debug("REST request to get all DWTransferts");
		List<DWTransfert> dWTransferts = dWTransfertRepository.findAll();
		return dWTransferts;
	}

	/**
	 * GET /d-w-transferts/:id : get the "id" dWTransfert.
	 *
	 * @param id
	 *            the id of the dWTransfert to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         dWTransfert, or with status 404 (Not Found)
	 */
	@RequestMapping(value = "/d-w-transferts/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<DWTransfert> getDWTransfert(@PathVariable Long id) {
		log.debug("REST request to get DWTransfert : {}", id);
		DWTransfert dWTransfert = dWTransfertRepository.findOne(id);
		return Optional.ofNullable(dWTransfert).map(result -> new ResponseEntity<>(result, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * Launch a transfert. /d-w-transferts/:id : get the "id" dWTransfert.
	 *
	 * @param id
	 *            the id of the dWTransfert to launch
	 */
	@RequestMapping(value = "/d-w-transferts-run/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public void launchRunDWTransfert(@PathVariable Long id) {
		log.debug("REST request to run asynchronously Transfert : {}", id);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			try {
				transfertSchedulerService.runPersistentTransfert(id);
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		});
	}

	/**
	 * Launch a transfert. /d-w-transferts/:id : get the "id" dWTransfert.
	 *
	 * @param id
	 *            the id of the dWTransfert to launch
	 */
	@RequestMapping(value = "/d-w-transferts-max-rank", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<MaxRankHolder> getMaxRank() {
		log.debug("REST request to get Max Rank Transfert");
		Integer maxRank = dWTransfertRepository.findMaxRank();
		MaxRankHolder mrh = new MaxRankHolder(maxRank == null ? 0 : maxRank);
		log.debug("REST request to get Max Rank Transfert=>{}", maxRank);
		return Optional.ofNullable(mrh).map(result -> new ResponseEntity<>(result, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * DELETE /d-w-transferts/:id : delete the "id" dWTransfert.
	 *
	 * @param id
	 *            the id of the dWTransfert to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@RequestMapping(value = "/d-w-transferts/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<Void> deleteDWTransfert(@PathVariable Long id) {
		log.debug("REST request to delete DWTransfert : {}", id);
		dWTransfertRepository.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("dWTransfert", id.toString())).build();
	}

        @RequestMapping(value = "/deleteTransfertsByStatus/{status}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
        @Timed
        public ResponseEntity<Void> deleteDWTransfertByStatus(@PathVariable TransfertStatus status) {
                log.debug("REST request to delete DWTransfert by status: {}", status);
                dWTransfertRepository.deleteByStatus(status);
                return ResponseEntity.ok().headers(HeaderUtil.createTransfertDoneDeletionAlert(status.toString())).build();
        }

}
