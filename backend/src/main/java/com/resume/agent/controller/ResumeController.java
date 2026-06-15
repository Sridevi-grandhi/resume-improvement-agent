package com.resume.agent.controller;

import com.resume.agent.dto.ResumeAnalyzeRequest;
import com.resume.agent.dto.ResumeAnalyzeResponse;
import com.resume.agent.service.ResumeAgentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller that handles resume analysis requests.
 *
 * ENDPOINTS:
 *   POST /api/resume/analyze  → Analyze a resume against a job description
 *   GET  /api/resume/history  → Get all previous analysis records
 *
 * CrossOrigin allows the React frontend (port 5173) to call these APIs.
 */
@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176", "http://localhost:5177"})
public class ResumeController {

    private final ResumeAgentService resumeAgentService;

    // Constructor injection — Spring automatically provides the service
    public ResumeController(ResumeAgentService resumeAgentService) {
        this.resumeAgentService = resumeAgentService;
    }

    /**
     * POST /api/resume/analyze
     *
     * Accepts resume text and job description in the request body,
     * runs the 10-step agent analysis, saves to database, and returns results.
     */
    @PostMapping("/analyze")
    public ResumeAnalyzeResponse analyzeResume(@RequestBody ResumeAnalyzeRequest request) {
        return resumeAgentService.analyzeResume(request);
    }

    /**
     * GET /api/resume/history
     *
     * Returns all previous resume analysis records from the database,
     * ordered by newest first.
     *
     * The frontend calls this when the user clicks "View History".
     */
    @GetMapping("/history")
    public List<Map<String, Object>> getHistory() {
        return resumeAgentService.getHistory();
    }
}
