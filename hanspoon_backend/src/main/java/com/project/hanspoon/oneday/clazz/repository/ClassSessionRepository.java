package com.project.hanspoon.oneday.clazz.repository;

import com.project.hanspoon.oneday.clazz.domain.SessionSlot;
import com.project.hanspoon.oneday.clazz.entity.ClassSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long>, JpaSpecificationExecutor<ClassSession> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select  s from ClassSession s where  s.id = :id")
    Optional<ClassSession> findByIdForUpdate(@Param("id") Long id );

    @Query("select  s from ClassSession  s " +
            "where s.classProduct.id = :classId " +
            "and (:from is null or s.startAt >= :from) " +
            "and (:to is null or s.startAt < :to) " +
            "and (:slot is null or s.slot = :slot) " +
            "order by s.startAt asc")
    List<ClassSession> findSessions(
            @Param("classId") Long classId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("slot") SessionSlot slot
            );

    boolean existsByClassProductIdAndReservedCountGreaterThan(Long classProductId, int reservedCount);

    void deleteByClassProductId(Long classProductId);
}
