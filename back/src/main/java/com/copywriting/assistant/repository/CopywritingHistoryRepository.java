package com.copywriting.assistant.repository;

import com.copywriting.assistant.model.entity.CopywritingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CopywritingHistoryRepository extends JpaRepository<CopywritingHistory, Long> {

    List<CopywritingHistory> findByTaskId(String taskId);

    Page<CopywritingHistory> findByProductNameContaining(String productName, Pageable pageable);

    List<CopywritingHistory> findTop10ByOrderByCreatedAtDesc();
}
