package com.resume.agent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "resume_analysis")
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String resumeText;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    private int atsScore;

    @Column(name = "original_ats_score", columnDefinition = "integer default 0")
    private int originalAtsScore = 0;

    @Column(name = "generated_resume_ats_score", columnDefinition = "integer")
    private Integer generatedResumeAtsScore;

    @Column(columnDefinition = "TEXT")
    private String scoreLabel;

    @Column(name = "optimization_status", columnDefinition = "TEXT")
    private String optimizationStatus;

    private int roleMatchScore;

    private int requiredSkillScore;

    @Column(columnDefinition = "TEXT")
    private String matchedKeywords;

    @Column(columnDefinition = "TEXT")
    private String missingKeywords;

    @Column(columnDefinition = "TEXT")
    private String roleMismatchWarning;

    @Column(columnDefinition = "TEXT")
    private String skillGapAnalysis;

    @Column(columnDefinition = "TEXT")
    private String suggestions;

    @Column(name = "improved_summary", columnDefinition = "TEXT")
    private String improvedProfessionalSummary;

    @Column(columnDefinition = "TEXT")
    private String improvedBulletPoints;

    @Column(columnDefinition = "TEXT")
    private String generatedResume;

    @Column(name = "verified_additional_skills", columnDefinition = "TEXT")
    private String verifiedAdditionalSkills;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public ResumeAnalysis() {}

    public static String listToString(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join("||", list);
    }

    public static List<String> stringToList(String str) {
        if (str == null || str.isEmpty()) return List.of();
        return List.of(str.split("\\|\\|"));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getResumeText() { return resumeText; }
    public void setResumeText(String resumeText) { this.resumeText = resumeText; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public int getAtsScore() { return atsScore; }
    public void setAtsScore(int atsScore) { this.atsScore = atsScore; }

    public int getOriginalAtsScore() { return originalAtsScore; }
    public void setOriginalAtsScore(int originalAtsScore) { this.originalAtsScore = originalAtsScore; }

    public int getGeneratedResumeAtsScore() { return generatedResumeAtsScore != null ? generatedResumeAtsScore : 0; }
    public void setGeneratedResumeAtsScore(Integer generatedResumeAtsScore) { this.generatedResumeAtsScore = generatedResumeAtsScore; }

    public String getScoreLabel() { return scoreLabel; }
    public void setScoreLabel(String scoreLabel) { this.scoreLabel = scoreLabel; }

    public int getRoleMatchScore() { return roleMatchScore; }
    public void setRoleMatchScore(int roleMatchScore) { this.roleMatchScore = roleMatchScore; }

    public int getRequiredSkillScore() { return requiredSkillScore; }
    public void setRequiredSkillScore(int requiredSkillScore) { this.requiredSkillScore = requiredSkillScore; }

    public String getMatchedKeywords() { return matchedKeywords; }
    public void setMatchedKeywords(String matchedKeywords) { this.matchedKeywords = matchedKeywords; }

    public String getMissingKeywords() { return missingKeywords; }
    public void setMissingKeywords(String missingKeywords) { this.missingKeywords = missingKeywords; }

    public String getRoleMismatchWarning() { return roleMismatchWarning; }
    public void setRoleMismatchWarning(String roleMismatchWarning) { this.roleMismatchWarning = roleMismatchWarning; }

    public String getSkillGapAnalysis() { return skillGapAnalysis; }
    public void setSkillGapAnalysis(String skillGapAnalysis) { this.skillGapAnalysis = skillGapAnalysis; }

    public String getSuggestions() { return suggestions; }
    public void setSuggestions(String suggestions) { this.suggestions = suggestions; }

    public String getImprovedProfessionalSummary() { return improvedProfessionalSummary; }
    public void setImprovedProfessionalSummary(String improvedProfessionalSummary) { this.improvedProfessionalSummary = improvedProfessionalSummary; }

    public String getImprovedBulletPoints() { return improvedBulletPoints; }
    public void setImprovedBulletPoints(String improvedBulletPoints) { this.improvedBulletPoints = improvedBulletPoints; }

    public String getGeneratedResume() { return generatedResume; }
    public void setGeneratedResume(String generatedResume) { this.generatedResume = generatedResume; }

    public String getOptimizationStatus() { return optimizationStatus; }
    public void setOptimizationStatus(String optimizationStatus) { this.optimizationStatus = optimizationStatus; }

    public String getVerifiedAdditionalSkills() { return verifiedAdditionalSkills; }
    public void setVerifiedAdditionalSkills(String verifiedAdditionalSkills) { this.verifiedAdditionalSkills = verifiedAdditionalSkills; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
