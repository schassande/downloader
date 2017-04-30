package org.scb.downloader.repository;

import org.scb.downloader.domain.DWHostAccount;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the DWHostAccount entity.
 */
@SuppressWarnings("unused")
public interface DWHostAccountRepository extends JpaRepository<DWHostAccount,Long> {

}
