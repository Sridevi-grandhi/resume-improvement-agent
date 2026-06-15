package com.resume.agent.dto;

import java.util.List;

public class ResumeAnalyzeResponse {

    private int originalAtsScore;
    private int generatedResumeAtsScore;
    private int atsScore;
    private String scoreLabel;
    private int roleMatchScore;
    private int requiredSkillScore;
    private List<String> matchedKeywords;
    private List<String> originalMatchedKeywords;
    private List<String> generatedMatchedKeywords;
    private List<String> missingKeywords;
    private String roleMismatchWarning;
    private String skillGapAnalysis;
    private List<String> suggestions;
    private String improvedProfessionalSummary;
    private List<String> improvedBulletPoints;
    private String generatedResume;
    private String optimizationStatus;

    public ResumeAnalyzeResponse() {}

    public ResumeAnalyzeResponse(int originalAtsScore, int generatedResumeAtsScore, String scoreLabel,
                                  int roleMatchScore, int requiredSkillScore, List<String> matchedKeywords,
                                  List<String> originalMatchedKeywords, List<String> generatedMatchedKeywords,
                                  List<String> missingKeywords, String roleMismatchWarning,
                                  String skillGapAnalysis, List<String> suggestions,
                                  String improvedProfessionalSummary, List<String> improvedBulletPoints,
                                  String generatedResume, String optimizationStatus) {
        this.originalAtsScore = originalAtsScore;
        this.generatedResumeAtsScore = generatedResumeAtsScore;
        this.atsScore = originalAtsScore;
        this.scoreLabel = scoreLabel;
        this.roleMatchScore = roleMatchScore;
        this.requiredSkillScore = requiredSkillScore;
        this.matchedKeywords = matchedKeywords;
        this.originalMatchedKeywords = originalMatchedKeywords;
        this.generatedMatchedKeywords = generatedMatchedKeywords;
        this.missingKeywords = missingKeywords;
        this.roleMismatchWarning = roleMismatchWarning;
        this.skillGapAnalysis = skillGapAnalysis;
        this.suggestions = suggestions;
        this.improvedProfessionalSummary = improvedProfessionalSummary;
        this.improvedBulletPoints = improvedBulletPoints;
        this.generatedResume = generatedResume;
        this.optimizationStatus = optimizationStatus;
    }

    public int getOriginalAtsScore() { return originalAtsScore; }
    public void setOriginalAtsScore(int originalAtsScore) { this.originalAtsScore = originalAtsScore; }

    public int getGeneratedResumeAtsScore() { return generatedResumeAtsScore; }
    public void setGeneratedResumeAtsScore(int generatedResumeAtsScore) {
        this.generatedResumeAtsScore = generatedResumeAtsScore;
    }

    public int getAtsScore() { return atsScore; }

    public void setAtsScore(int atsScore) {
        this.atsScore = atsScore;
    }

    public String getScoreLabel() { return scoreLabel; }
    public void setScoreLabel(String scoreLabel) { this.scoreLabel = scoreLabel; }

    public int getRoleMatchScore() { return roleMatchScore; }
    public void setRoleMatchScore(int roleMatchScore) { this.roleMatchScore = roleMatchScore; }

    public int getRequiredSkillScore() { return requiredSkillScore; }
    public void setRequiredSkillScore(int requiredSkillScore) { this.requiredSkillScore = requiredSkillScore; }

    public List<String> getMatchedKeywords() { return matchedKeywords; }
    public void setMatchedKeywords(List<String> matchedKeywords) { this.matchedKeywords = matchedKeywords; }

    public List<String> getOriginalMatchedKeywords() { return originalMatchedKeywords; }
    public void setOriginalMatchedKeywords(List<String> originalMatchedKeywords) { this.originalMatchedKeywords = originalMatchedKeywords; }

    public List<String> getGeneratedMatchedKeywords() { return generatedMatchedKeywords; }
    public void setGeneratedMatchedKeywords(List<String> generatedMatchedKeywords) { this.generatedMatchedKeywords = generatedMatchedKeywords; }

    public List<String> getMissingKeywords() { return missingKeywords; }
    public void setMissingKeywords(List<String> missingKeywords) { this.missingKeywords = missingKeywords; }

    public String getRoleMismatchWarning() { return roleMismatchWarning; }
    public void setRoleMismatchWarning(String roleMismatchWarning) { this.roleMismatchWarning = roleMismatchWarning; }

    public String getSkillGapAnalysis() { return skillGapAnalysis; }
    public void setSkillGapAnalysis(String skillGapAnalysis) { this.skillGapAnalysis = skillGapAnalysis; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public String getImprovedProfessionalSummary() { return improvedProfessionalSummary; }
    public void setImprovedProfessionalSummary(String improvedProfessionalSummary) { this.improvedProfessionalSummary = improvedProfessionalSummary; }

    public List<String> getImprovedBulletPoints() { return improvedBulletPoints; }
    public void setImprovedBulletPoints(List<String> improvedBulletPoints) { this.improvedBulletPoints = improvedBulletPoints; }

    public String getGeneratedResume() { return generatedResume; }
    public void setGeneratedResume(String generatedResume) { this.generatedResume = generatedResume; }

    public String getOptimizationStatus() { return optimizationStatus; }
    public void setOptimizationStatus(String optimizationStatus) { this.optimizationStatus = optimizationStatus; }
}
