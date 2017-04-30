package org.scb.downloader.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.scb.downloader.DownloaderApp;
import org.scb.downloader.domain.DWHostAccount;
import org.scb.downloader.domain.enumeration.DWProtocol;
import org.scb.downloader.repository.DWHostAccountRepository;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test class for the DWHostAccountResource REST controller.
 *
 * @see DWHostAccountResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DownloaderApp.class)
@WebAppConfiguration
@IntegrationTest
public class DWHostAccountResourceIntTest {

	private static final String DEFAULT_HOST = "AAAAA";
	private static final String UPDATED_HOST = "BBBBB";
	private static final String DEFAULT_PORT = "AAAAA";
	private static final String UPDATED_PORT = "BBBBB";
	private static final String DEFAULT_USER_NAME = "AAAAA";
	private static final String UPDATED_USER_NAME = "BBBBB";
	private static final String DEFAULT_PASSWORD = "AAAAA";
	private static final String UPDATED_PASSWORD = "BBBBB";

	private static final DWProtocol DEFAULT_PROTOCOL = DWProtocol.SSH;
	private static final DWProtocol UPDATED_PROTOCOL = DWProtocol.LOCAL_FILE_SYSTEM;

	@Inject
	private DWHostAccountRepository dWHostAccountRepository;

	@Inject
	private MappingJackson2HttpMessageConverter jacksonMessageConverter;

	@Inject
	private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

	private MockMvc restDWHostAccountMockMvc;

	private DWHostAccount dWHostAccount;

	@PostConstruct
	public void setup() {
		MockitoAnnotations.initMocks(this);
		DWHostAccountResource dWHostAccountResource = new DWHostAccountResource();
		ReflectionTestUtils.setField(dWHostAccountResource, "dWHostAccountRepository", dWHostAccountRepository);
		this.restDWHostAccountMockMvc = MockMvcBuilders.standaloneSetup(dWHostAccountResource)
				.setCustomArgumentResolvers(pageableArgumentResolver).setMessageConverters(jacksonMessageConverter)
				.build();
	}

	@Before
	public void initTest() {
		dWHostAccount = new DWHostAccount();
		dWHostAccount.setHost(DEFAULT_HOST);
		dWHostAccount.setPort(DEFAULT_PORT);
		dWHostAccount.setUserName(DEFAULT_USER_NAME);
		dWHostAccount.setPassword(DEFAULT_PASSWORD);
		dWHostAccount.setProtocol(DEFAULT_PROTOCOL);
	}

	@Test
	@Transactional
	public void createDWHostAccount() throws Exception {
		int databaseSizeBeforeCreate = dWHostAccountRepository.findAll().size();

		// Create the DWHostAccount

		restDWHostAccountMockMvc.perform(post("/api/d-w-host-accounts").contentType(TestUtil.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(dWHostAccount))).andExpect(status().isCreated());

		// Validate the DWHostAccount in the database
		List<DWHostAccount> dWHostAccounts = dWHostAccountRepository.findAll();
		assertThat(dWHostAccounts).hasSize(databaseSizeBeforeCreate + 1);
		DWHostAccount testDWHostAccount = dWHostAccounts.get(dWHostAccounts.size() - 1);
		assertThat(testDWHostAccount.getHost()).isEqualTo(DEFAULT_HOST);
		assertThat(testDWHostAccount.getPort()).isEqualTo(DEFAULT_PORT);
		assertThat(testDWHostAccount.getUserName()).isEqualTo(DEFAULT_USER_NAME);
		assertThat(testDWHostAccount.getPassword()).isEqualTo(DEFAULT_PASSWORD);
		assertThat(testDWHostAccount.getProtocol()).isEqualTo(DEFAULT_PROTOCOL);
	}

	@Test
	@Transactional
	public void getAllDWHostAccounts() throws Exception {
		// Initialize the database
		dWHostAccountRepository.saveAndFlush(dWHostAccount);

		// Get all the dWHostAccounts
		restDWHostAccountMockMvc.perform(get("/api/d-w-host-accounts?sort=id,desc")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.[*].id").value(hasItem(dWHostAccount.getId().intValue())))
				.andExpect(jsonPath("$.[*].host").value(hasItem(DEFAULT_HOST.toString())))
				.andExpect(jsonPath("$.[*].port").value(hasItem(DEFAULT_PORT.toString())))
				.andExpect(jsonPath("$.[*].userName").value(hasItem(DEFAULT_USER_NAME.toString())))
				.andExpect(jsonPath("$.[*].password").value(hasItem(DEFAULT_PASSWORD.toString())))
				.andExpect(jsonPath("$.[*].protocol").value(hasItem(DEFAULT_PROTOCOL.toString())));
	}

	@Test
	@Transactional
	public void getDWHostAccount() throws Exception {
		// Initialize the database
		dWHostAccountRepository.saveAndFlush(dWHostAccount);

		// Get the dWHostAccount
		restDWHostAccountMockMvc.perform(get("/api/d-w-host-accounts/{id}", dWHostAccount.getId()))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(dWHostAccount.getId().intValue()))
				.andExpect(jsonPath("$.host").value(DEFAULT_HOST.toString()))
				.andExpect(jsonPath("$.port").value(DEFAULT_PORT.toString()))
				.andExpect(jsonPath("$.userName").value(DEFAULT_USER_NAME.toString()))
				.andExpect(jsonPath("$.password").value(DEFAULT_PASSWORD.toString()))
				.andExpect(jsonPath("$.protocol").value(DEFAULT_PROTOCOL.toString()));
	}

	@Test
	@Transactional
	public void getNonExistingDWHostAccount() throws Exception {
		// Get the dWHostAccount
		restDWHostAccountMockMvc.perform(get("/api/d-w-host-accounts/{id}", Long.MAX_VALUE))
				.andExpect(status().isNotFound());
	}

	@Test
	@Transactional
	public void updateDWHostAccount() throws Exception {
		// Initialize the database
		dWHostAccountRepository.saveAndFlush(dWHostAccount);
		int databaseSizeBeforeUpdate = dWHostAccountRepository.findAll().size();

		// Update the dWHostAccount
		DWHostAccount updatedDWHostAccount = new DWHostAccount();
		updatedDWHostAccount.setId(dWHostAccount.getId());
		updatedDWHostAccount.setHost(UPDATED_HOST);
		updatedDWHostAccount.setPort(UPDATED_PORT);
		updatedDWHostAccount.setUserName(UPDATED_USER_NAME);
		updatedDWHostAccount.setPassword(UPDATED_PASSWORD);
		updatedDWHostAccount.setProtocol(UPDATED_PROTOCOL);

		restDWHostAccountMockMvc.perform(put("/api/d-w-host-accounts").contentType(TestUtil.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(updatedDWHostAccount))).andExpect(status().isOk());

		// Validate the DWHostAccount in the database
		List<DWHostAccount> dWHostAccounts = dWHostAccountRepository.findAll();
		assertThat(dWHostAccounts).hasSize(databaseSizeBeforeUpdate);
		DWHostAccount testDWHostAccount = dWHostAccounts.get(dWHostAccounts.size() - 1);
		assertThat(testDWHostAccount.getHost()).isEqualTo(UPDATED_HOST);
		assertThat(testDWHostAccount.getPort()).isEqualTo(UPDATED_PORT);
		assertThat(testDWHostAccount.getUserName()).isEqualTo(UPDATED_USER_NAME);
		assertThat(testDWHostAccount.getPassword()).isEqualTo(UPDATED_PASSWORD);
		assertThat(testDWHostAccount.getProtocol()).isEqualTo(UPDATED_PROTOCOL);
	}

	@Test
	@Transactional
	public void deleteDWHostAccount() throws Exception {
		// Initialize the database
		dWHostAccountRepository.saveAndFlush(dWHostAccount);
		int databaseSizeBeforeDelete = dWHostAccountRepository.findAll().size();

		// Get the dWHostAccount
		restDWHostAccountMockMvc.perform(
				delete("/api/d-w-host-accounts/{id}", dWHostAccount.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());

		// Validate the database is empty
		List<DWHostAccount> dWHostAccounts = dWHostAccountRepository.findAll();
		assertThat(dWHostAccounts).hasSize(databaseSizeBeforeDelete - 1);
	}
}
