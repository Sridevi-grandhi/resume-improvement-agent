package com.resume.agent.repository;

import com.resume.agent.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for the ResumeAnalysis entity.
 *
 * WHAT IS A REPOSITORY?
 * A repository is like a helper that handles all database operations
 * (save, find, delete, update) for a specific entity.
 *
 * By extending JpaRepository, Spring automatically gives us these methods:
 *   - save(entity)       → INSERT or UPDATE a record
 *   - findAll()          → SELECT * FROM resume_analysis
 *   - findById(id)       → SELECT * WHERE id = ?
 *   - deleteById(id)     → DELETE FROM resume_analysis WHERE id = ?
 *   - count()            → SELECT COUNT(*) FROM resume_analysis
 *
 * We can also define custom query methods just by naming them correctly.
 * Spring reads the method name and creates the SQL query automatically!
 *
 * JpaRepository<ResumeAnalysis, Long> means:
 *   - ResumeAnalysis = the entity this repository manages
 *   - Long = the data type of the primary key (id)
 */
@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {

    /**
     * Custom query: Find all analysis records ordered by newest first.
     *
     * Spring reads this method name and generates:
     *   SELECT * FROM resume_analysis ORDER BY created_at DESC
     *
     * This is called a "derived query method" — no SQL needed!
     */
    List<ResumeAnalysis> findAllByOrderByCreatedAtDesc();
}
