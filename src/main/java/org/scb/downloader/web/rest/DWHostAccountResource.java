package org.scb.downloader.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.scb.downloader.domain.DWHostAccount;
import org.scb.downloader.repository.DWHostAccountRepository;
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
 * REST controller for managing DWHostAccount.
 */
@RestController
@RequestMapping("/api")
public class DWHostAccountResource {

	private final Logger log = LoggerFactory.getLogger(DWHostAccountResource.class);

	@Inject
	private DWHostAccountRepository dWHostAccountRepository;

	/**
	 * POST /d-w-host-accounts : Create a new dWHostAccount.
	 *
	 * @param dWHostAccount
	 *            the dWHostAccount to create
	 * @return the ResponseEntity with status 201 (Created) and with body the
	 *         new dWHostAccount, or with status 400 (Bad Request) if the
	 *         dWHostAccount has already an ID
	 * @throws URISyntaxException
	 *             if the Location URI syntax is incorrect
	 */
	@RequestMapping(value = "/d-w-host-accounts", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<DWHostAccount> createDWHostAccount(@RequestBody DWHostAccount dWHostAccount)
			throws URISyntaxException {
		log.debug("REST request to save DWHostAccount : {}", dWHostAccount);
		if (dWHostAccount.getId() != null) {
			return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("dWHostAccount", "idexists",
					"A new dWHostAccount cannot already have an ID")).body(null);
		}
		DWHostAccount result = dWHostAccountRepository.save(dWHostAccount);
		return ResponseEntity.created(new URI("/api/d-w-host-accounts/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert("dWHostAccount", result.getId().toString())).body(result);
	}

	/**
	 * PUT /d-w-host-accounts : Updates an existing dWHostAccount.
	 *
	 * @param dWHostAccount
	 *            the dWHostAccount to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         dWHostAccount, or with status 400 (Bad Request) if the
	 *         dWHostAccount is not valid, or with status 500 (Internal Server
	 *         Error) if the dWHostAccount couldnt be updated
	 * @throws URISyntaxException
	 *             if the Location URI syntax is incorrect
	 */
	@RequestMapping(value = "/d-w-host-accounts", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<DWHostAccount> updateDWHostAccount(@RequestBody DWHostAccount dWHostAccount)
			throws URISyntaxException {
		log.debug("REST request to update DWHostAccount : {}", dWHostAccount);
		if (dWHostAccount.getId() == null) {
			return createDWHostAccount(dWHostAccount);
		}
		DWHostAccount result = dWHostAccountRepository.save(dWHostAccount);
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityUpdateAlert("dWHostAccount", dWHostAccount.getId().toString()))
				.body(result);
	}

	/**
	 * GET /d-w-host-accounts : get all the dWHostAccounts.
	 *
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         dWHostAccounts in body
	 */
	@RequestMapping(value = "/d-w-host-accounts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public List<DWHostAccount> getAllDWHostAccounts() {
		log.debug("REST request to get all DWHostAccounts");
		List<DWHostAccount> dWHostAccounts = dWHostAccountRepository.findAll();
		return dWHostAccounts;
	}

	/**
	 * GET /d-w-host-accounts/:id : get the "id" dWHostAccount.
	 *
	 * @param id
	 *            the id of the dWHostAccount to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         dWHostAccount, or with status 404 (Not Found)
	 */
	@RequestMapping(value = "/d-w-host-accounts/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<DWHostAccount> getDWHostAccount(@PathVariable Long id) {
		log.debug("REST request to get DWHostAccount : {}", id);
		DWHostAccount dWHostAccount = dWHostAccountRepository.findOne(id);
		return Optional.ofNullable(dWHostAccount).map(result -> new ResponseEntity<>(result, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * DELETE /d-w-host-accounts/:id : delete the "id" dWHostAccount.
	 *
	 * @param id
	 *            the id of the dWHostAccount to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@RequestMapping(value = "/d-w-host-accounts/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<Void> deleteDWHostAccount(@PathVariable Long id) {
		log.debug("REST request to delete DWHostAccount : {}", id);
		dWHostAccountRepository.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("dWHostAccount", id.toString()))
				.build();
	}

}
