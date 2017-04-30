package org.scb.downloader.repository;

import java.util.List;

import org.scb.downloader.domain.DWTransfert;
import org.scb.downloader.domain.enumeration.DWTransfertScheduling;
import org.scb.downloader.domain.enumeration.TransfertStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for the DWTransfert entity.
 */
public interface DWTransfertRepository extends JpaRepository<DWTransfert, Long> {

	@Query("SELECT t FROM DWTransfert t WHERE t.status = :status AND t.scheduling = :scheduling ORDER BY t.rank ASC")
	List<DWTransfert> findNext(//
			@Param("status") TransfertStatus status, //
			@Param("scheduling") DWTransfertScheduling scheduling, //
			Pageable pageable);

	default DWTransfert findNext(//
			@Param("status") TransfertStatus status, //
			@Param("scheduling") DWTransfertScheduling scheduling) {
		List<DWTransfert> ts = findNext(status, scheduling, new PageRequest(0, 1));
		if (ts.isEmpty()) {
			return null;
		} else {
			return ts.get(0);
		}
	}

	@Query("SELECT t FROM DWTransfert t WHERE t.status = :status and t.scheduling = :scheduling and t.dayBegin <= :dayInstant and t.dayEnd >= :dayInstant ORDER BY t.rank ASC")
	List<DWTransfert> findNext(//
			@Param("status") TransfertStatus status, //
			@Param("scheduling") DWTransfertScheduling scheduling, //
			@Param("dayInstant") Long dayInstant, //
			Pageable pageable);

	default DWTransfert findNext(//
			@Param("status") TransfertStatus status, //
			@Param("scheduling") DWTransfertScheduling scheduling, //
			@Param("dayInstant") Long dayInstant) {
		List<DWTransfert> ts = findNext(status, scheduling, dayInstant, new PageRequest(0, 1));
		if (ts.isEmpty()) {
			return null;
		} else {
			return ts.get(0);
		}
	}

	@Query("SELECT t FROM DWTransfert t WHERE t.status = :status ORDER BY t.rank ASC")
	List<DWTransfert> findAllByStatus(@Param("status") TransfertStatus status);

	@Query("SELECT max(t.rank) FROM DWTransfert t")
	Integer findMaxRank();

	@Query("DELETE FROM DWTransfert t where t.status = :status")
	@Modifying
	@Transactional
	void deleteByStatus(@Param("status") TransfertStatus status);
}
