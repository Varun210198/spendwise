package com.vvs.spendwise_api.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    boolean existsByCategoryId(Long categoryId);

    @Query("""
            select t from Transaction t
            where t.user.id = :userId
              and (:from is null or t.date >= :from)
              and (:to is null or t.date <= :to)
              and (:categoryId is null or t.category.id = :categoryId)
            """)
    Page<Transaction> search(@Param("userId") Long userId,
                              @Param("from") LocalDate from,
                              @Param("to") LocalDate to,
                              @Param("categoryId") Long categoryId,
                              Pageable pageable);

    @Query("""
            select t from Transaction t join fetch t.category
            where t.user.id = :userId and t.date between :from and :to
            """)
    List<Transaction> findByUserIdAndDateBetween(@Param("userId") Long userId,
                                                  @Param("from") LocalDate from,
                                                  @Param("to") LocalDate to);
}
