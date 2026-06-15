package com.resume.agent.dto;

/**
 * DTO (Data Transfer Object) for the incoming request.
 * Contains the resume text, job description, and optional verified additional skills.
 */
public class ResumeAnalyzeRequest {

    private String resumeText;
    private String jobDescription;
    private String verifiedAdditionalSkills;

    public ResumeAnalyzeRequest() {}

    public ResumeAnalyzeRequest(String resumeText, String jobDescription) {
        this.resumeText = resumeText;
        this.jobDescription = jobDescription;
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getVerifiedAdditionalSkills() {
        return verifiedAdditionalSkills;
    }

    public void setVerifiedAdditionalSkills(String verifiedAdditionalSkills) {
        this.verifiedAdditionalSkills = verifiedAdditionalSkills;
    }
}
