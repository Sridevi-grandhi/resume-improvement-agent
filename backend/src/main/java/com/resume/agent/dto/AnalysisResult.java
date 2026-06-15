package com.resume.agent.dto;

import java.util.ArrayList;
import java.util.List;

public class AnalysisResult {

    private int atsScore;
    private String scoreLabel;
    private int roleMatchScore;
    private int requiredSkillScore;
    private List<String> matchedKeywords = new ArrayList<>();
    private List<String> missingKeywords = new ArrayList<>();
    private String resumeRole;
    private String jdRole;

    public AnalysisResult() {}

    public int getAtsScore() { return atsScore; }
    public void setAtsScore(int atsScore) { this.atsScore = atsScore; }

    public String getScoreLabel() { return scoreLabel; }
    public void setScoreLabel(String scoreLabel) { this.scoreLabel = scoreLabel; }

    public int getRoleMatchScore() { return roleMatchScore; }
    public void setRoleMatchScore(int roleMatchScore) { this.roleMatchScore = roleMatchScore; }

    public int getRequiredSkillScore() { return requiredSkillScore; }
    public void setRequiredSkillScore(int requiredSkillScore) { this.requiredSkillScore = requiredSkillScore; }

    public List<String> getMatchedKeywords() { return matchedKeywords; }
    public void setMatchedKeywords(List<String> matchedKeywords) { this.matchedKeywords = matchedKeywords; }

    public List<String> getMissingKeywords() { return missingKeywords; }
    public void setMissingKeywords(List<String> missingKeywords) { this.missingKeywords = missingKeywords; }

    public String getResumeRole() { return resumeRole; }
    public void setResumeRole(String resumeRole) { this.resumeRole = resumeRole; }

    public String getJdRole() { return jdRole; }
    public void setJdRole(String jdRole) { this.jdRole = jdRole; }
}
