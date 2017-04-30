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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.scb.downloader.DownloaderApp;
import org.scb.downloader.domain.DWTransfert;
import org.scb.downloader.repository.DWTransfertRepository;
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
 * Test class for the DWTransfertResource REST controller.
 *
 * @see DWTransfertResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DownloaderApp.class)
@WebAppConfiguration
@IntegrationTest
public class DWTransfertResourceIntTest {

	private static final DateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	private static final Date DEFAULT_START = new Date(0);
	private static final Date UPDATED_START = new Date();
	private static final String DEFAULT_START_STR = dateTimeFormatter.format(DEFAULT_START);

	private static final Date DEFAULT_END = new Date(0);
	private static final Date UPDATED_END = new Date();
	private static final String DEFAULT_END_STR = dateTimeFormatter.format(DEFAULT_END);

	@Inject
	private DWTransfertRepository dWTransfertRepository;

	@Inject
	private MappingJackson2HttpMessageConverter jacksonMessageConverter;

	@Inject
	private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

	private MockMvc restDWTransfertMockMvc;

	private DWTransfert dWTransfert;

	@PostConstruct
	public void setup() {
		MockitoAnnotations.initMocks(this);
		DWTransfertResource dWTransfertResource = new DWTransfertResource();
		ReflectionTestUtils.setField(dWTransfertResource, "dWTransfertRepository", dWTransfertRepository);
		this.restDWTransfertMockMvc = MockMvcBuilders.standaloneSetup(dWTransfertResource)
				.setCustomArgumentResolvers(pageableArgumentResolver).setMessageConverters(jacksonMessageConverter)
				.build();
	}

	@Before
	public void initTest() {
		dWTransfert = new DWTransfert();
		dWTransfert.setStart(DEFAULT_START);
		dWTransfert.setEnd(DEFAULT_END);
	}

	@Test
	@Transactional
	public void createDWTransfert() throws Exception {
		int databaseSizeBeforeCreate = dWTransfertRepository.findAll().size();

		// Create the DWTransfert

		restDWTransfertMockMvc.perform(post("/api/d-w-transferts").contentType(TestUtil.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(dWTransfert))).andExpect(status().isCreated());

		// Validate the DWTransfert in the database
		List<DWTransfert> dWTransferts = dWTransfertRepository.findAll();
		assertThat(dWTransferts).hasSize(databaseSizeBeforeCreate + 1);
		DWTransfert testDWTransfert = dWTransferts.get(dWTransferts.size() - 1);
		assertThat(testDWTransfert.getStart()).isEqualTo(DEFAULT_START);
		assertThat(testDWTransfert.getEnd()).isEqualTo(DEFAULT_END);
	}

	@Test
	@Transactional
	public void getAllDWTransferts() throws Exception {
		// Initialize the database
		dWTransfertRepository.saveAndFlush(dWTransfert);

		// Get all the dWTransferts
		restDWTransfertMockMvc.perform(get("/api/d-w-transferts?sort=id,desc")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.[*].id").value(hasItem(dWTransfert.getId().intValue())))
				.andExpect(jsonPath("$.[*].start").value(hasItem(DEFAULT_START_STR)))
				.andExpect(jsonPath("$.[*].end").value(hasItem(DEFAULT_END_STR)));
	}

	@Test
	@Transactional
	public void getDWTransfert() throws Exception {
		// Initialize the database
		dWTransfertRepository.saveAndFlush(dWTransfert);

		// Get the dWTransfert
		restDWTransfertMockMvc.perform(get("/api/d-w-transferts/{id}", dWTransfert.getId())).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(dWTransfert.getId().intValue()))
				.andExpect(jsonPath("$.start").value(DEFAULT_START_STR))
				.andExpect(jsonPath("$.end").value(DEFAULT_END_STR));
	}

	@Test
	@Transactional
	public void getNonExistingDWTransfert() throws Exception {
		// Get the dWTransfert
		restDWTransfertMockMvc.perform(get("/api/d-w-transferts/{id}", Long.MAX_VALUE))
				.andExpect(status().isNotFound());
	}

	@Test
	@Transactional
	public void updateDWTransfert() throws Exception {
		// Initialize the database
		dWTransfertRepository.saveAndFlush(dWTransfert);
		int databaseSizeBeforeUpdate = dWTransfertRepository.findAll().size();

		// Update the dWTransfert
		DWTransfert updatedDWTransfert = new DWTransfert();
		updatedDWTransfert.setId(dWTransfert.getId());
		updatedDWTransfert.setStart(UPDATED_START);
		updatedDWTransfert.setEnd(UPDATED_END);

		restDWTransfertMockMvc.perform(put("/api/d-w-transferts").contentType(TestUtil.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(updatedDWTransfert))).andExpect(status().isOk());

		// Validate the DWTransfert in the database
		List<DWTransfert> dWTransferts = dWTransfertRepository.findAll();
		assertThat(dWTransferts).hasSize(databaseSizeBeforeUpdate);
		DWTransfert testDWTransfert = dWTransferts.get(dWTransferts.size() - 1);
		assertThat(testDWTransfert.getStart()).isEqualTo(UPDATED_START);
		assertThat(testDWTransfert.getEnd()).isEqualTo(UPDATED_END);
	}

	@Test
	@Transactional
	public void deleteDWTransfert() throws Exception {
		// Initialize the database
		dWTransfertRepository.saveAndFlush(dWTransfert);
		int databaseSizeBeforeDelete = dWTransfertRepository.findAll().size();

		// Get the dWTransfert
		restDWTransfertMockMvc
				.perform(delete("/api/d-w-transferts/{id}", dWTransfert.getId()).accept(TestUtil.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());

		// Validate the database is empty
		List<DWTransfert> dWTransferts = dWTransfertRepository.findAll();
		assertThat(dWTransferts).hasSize(databaseSizeBeforeDelete - 1);
	}
}
