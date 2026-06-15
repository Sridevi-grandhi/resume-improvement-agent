package com.resume.agent.service;

import com.resume.agent.dto.AnalysisResult;
import com.resume.agent.dto.ResumeAnalyzeRequest;
import com.resume.agent.dto.ResumeAnalyzeResponse;
import com.resume.agent.entity.ResumeAnalysis;
import com.resume.agent.repository.ResumeAnalysisRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * ============================================================
 *  ResumeAgentService — An Agentic AI Workflow (Beginner Level)
 * ============================================================
 *
 * WHAT IS AN "AGENTIC AI" WORKFLOW?
 * ---------------------------------
 * An AI Agent is a program that works through a problem STEP BY STEP,
 * just like a human expert would. Instead of doing everything in one
 * big function, the agent breaks the task into small, logical steps.
 *
 * Think of it like a human resume coach:
 *   Step 1:  "Let me read your resume first..."
 *   Step 2:  "Now let me read the job description..."
 *   Step 3:  "I'll extract keywords from the JD by category..."
 *   Step 4:  "I'll check which keywords your resume already has..."
 *   Step 5:  "I'll find what's missing..."
 *   Step 6:  "I'll detect what roles these texts target..."
 *   Step 7:  "I'll calculate weighted ATS score..."
 *   Step 8:  "I'll generate role mismatch warnings..."
 *   Step 9:  "I'll do a skill gap analysis..."
 *   Step 10: "I'll generate suggestions..."
 *   Step 11: "I'll write a truthful improved summary..."
 *   Step 12: "I'll write truthful bullet points..."
 *   Step 13: "I'll generate a complete improved resume..."
 *   Step 14: "Here's your full analysis report!"
 *
 * Each step is a separate method in this class. The main method
 * (analyzeResume) orchestrates all steps in order — like an
 * agent executing a plan.
 *
 * TRUTHFUL MODE: The agent NEVER fabricates skills or metrics.
 * It only highlights skills the candidate truly has.
 */
@Service
public class ResumeAgentService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;

    // When true, generated content only includes skills actually found in the resume
    private static final boolean TRUTHFUL_MODE = true;

    public ResumeAgentService(ResumeAnalysisRepository resumeAnalysisRepository) {
        this.resumeAnalysisRepository = resumeAnalysisRepository;
    }

    // ==========================================================================
    //  SKILL CATEGORY LISTS (8 categories)
    // ==========================================================================

    private static final List<String> PROGRAMMING_LANGUAGES = List.of(
        "Java", "Python", "JavaScript", "TypeScript", "SQL", "C#", "Go",
        "C++", "Ruby", "PHP", "Kotlin", "Scala", "Rust", "Swift", "R"
    );

    private static final List<String> BACKEND_FRAMEWORKS = List.of(
        "Spring Boot", "Spring", "Django", "Flask", "FastAPI", "Node.js",
        "Express.js", "REST API", "Microservices", "Hibernate", "JPA", "GraphQL", ".NET", "SQLAlchemy"
    );

    private static final List<String> FRONTEND_FRAMEWORKS = List.of(
        "React", "Angular", "Vue", "HTML", "CSS", "Bootstrap", "Tailwind",
        "Redux", "Next.js", "jQuery", "Sass", "Material UI"
    );

    private static final List<String> DATABASES = List.of(
        "MySQL", "PostgreSQL", "MongoDB", "Oracle", "SQL Server", "Redis",
        "Cassandra", "DynamoDB", "Elasticsearch", "SQLite", "Cloud SQL"
    );

    private static final List<String> CLOUD_TOOLS = List.of(
        "AWS", "Azure", "GCP", "EC2", "S3", "Lambda", "RDS", "CloudWatch",
        "Heroku", "Firebase", "CloudFormation"
    );

    private static final List<String> DEVOPS_TOOLS = List.of(
        "Git", "Jenkins", "Docker", "Kubernetes", "CI/CD", "Maven", "Gradle",
        "Terraform", "Ansible", "Linux", "Nginx", "GitHub Actions", "GitLab CI"
    );

    private static final List<String> TESTING_TOOLS = List.of(
        "JUnit", "PyTest", "Mockito", "Selenium", "Postman", "Jest",
        "Cypress", "TestNG", "Mocha", "Chai"
    );

    private static final List<String> SOFT_SKILLS = List.of(
        "Agile", "Scrum", "Kanban", "TDD", "BDD", "Code Review",
        "Mentoring", "Leadership", "Problem Solving"
    );

    // ==========================================================================
    //  NORMALIZATION ALIASES
    // ==========================================================================

    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        ALIASES.put("reactjs", "React");
        ALIASES.put("react.js", "React");
        ALIASES.put("react js", "React");
        ALIASES.put("springboot", "Spring Boot");
        ALIASES.put("spring-boot", "Spring Boot");
        ALIASES.put("rest apis", "REST API");
        ALIASES.put("rest api", "REST API");
        ALIASES.put("restful", "REST API");
        ALIASES.put("restful api", "REST API");
        ALIASES.put("restful apis", "REST API");
        ALIASES.put("postgres", "PostgreSQL");
        ALIASES.put("ci cd", "CI/CD");
        ALIASES.put("ci-cd", "CI/CD");
        ALIASES.put("cicd", "CI/CD");
        ALIASES.put("continuous integration", "CI/CD");
        ALIASES.put("continuous deployment", "CI/CD");
        ALIASES.put("js", "JavaScript");
        ALIASES.put("ts", "TypeScript");
        ALIASES.put("k8s", "Kubernetes");
        ALIASES.put("vue.js", "Vue");
        ALIASES.put("vuejs", "Vue");
        ALIASES.put("node", "Node.js");
        ALIASES.put("nodejs", "Node.js");
        ALIASES.put("express", "Express.js");
        ALIASES.put("expressjs", "Express.js");
        ALIASES.put("mongo", "MongoDB");
        ALIASES.put("nextjs", "Next.js");
        ALIASES.put("angularjs", "Angular");
        ALIASES.put("angular.js", "Angular");
        ALIASES.put("material-ui", "Material UI");
        ALIASES.put("mui", "Material UI");
        ALIASES.put("pytest", "PyTest");
        ALIASES.put("python 3.x", "Python");
        ALIASES.put("python 3", "Python");
        ALIASES.put("python3", "Python");
        ALIASES.put("python3.x", "Python");
        ALIASES.put("fast api", "FastAPI");
        ALIASES.put("fastapi", "FastAPI");
        ALIASES.put("sql alchemy", "SQLAlchemy");
        ALIASES.put("sqlalchemy", "SQLAlchemy");
        ALIASES.put("cloud sql", "Cloud SQL");
        ALIASES.put("agile scrum", "Agile");
        ALIASES.put("agile/scrum", "Agile");
        ALIASES.put("scrum agile", "Scrum");
        ALIASES.put("jira", "Jira");
        ALIASES.put("amazon web services", "AWS");
        ALIASES.put("google cloud", "GCP");
        ALIASES.put("google cloud platform", "GCP");
        ALIASES.put("microsoft azure", "Azure");
        ALIASES.put("pl/sql", "SQL");
        ALIASES.put("plsql", "SQL");
        ALIASES.put("api development", "REST API");
        ALIASES.put("api integration", "REST API");
        ALIASES.put("python scripting", "Python");
        ALIASES.put("automation scripting", "Python");
        ALIASES.put("automation scripts", "Python");
        ALIASES.put("unix shell scripting", "Linux");
        ALIASES.put("shell scripting", "Linux");
    }

    private static final List<String> MISC_TOOLS = List.of(
        "Jira", "Postman", "IntelliJ IDEA", "VS Code", "SDLC"
    );

    // ==========================================================================
    //  ROLE INDICATORS
    // ==========================================================================

    private static final Map<String, List<String>> ROLE_INDICATORS = new LinkedHashMap<>();

    static {
        ROLE_INDICATORS.put("Frontend Developer", List.of(
            "frontend developer", "front end developer", "front-end developer",
            "frontend engineer", "front end engineer", "react developer",
            "angular developer", "vue developer", "ui developer", "ui engineer",
            "ui/ux developer", "web developer"
        ));
        ROLE_INDICATORS.put("Backend Developer", List.of(
            "backend developer", "back end developer", "back-end developer",
            "backend engineer", "back end engineer", "server side developer"
        ));
        ROLE_INDICATORS.put("Java Developer", List.of(
            "java developer", "java engineer", "java programmer",
            "spring developer", "spring boot developer"
        ));
        ROLE_INDICATORS.put("Python Developer", List.of(
            "python developer", "python engineer", "python programmer",
            "django developer", "flask developer"
        ));
        ROLE_INDICATORS.put("Full Stack Developer", List.of(
            "full stack developer", "fullstack developer", "full-stack developer",
            "full stack engineer", "fullstack engineer", "full-stack engineer"
        ));
        ROLE_INDICATORS.put("DevOps Engineer", List.of(
            "devops engineer", "devops developer", "dev ops engineer",
            "site reliability engineer", "sre engineer", "platform engineer",
            "infrastructure engineer"
        ));
        ROLE_INDICATORS.put("Data Engineer", List.of(
            "data engineer", "data scientist", "data analyst",
            "machine learning engineer", "ml engineer", "ai engineer",
            "big data engineer"
        ));
        ROLE_INDICATORS.put("Mobile Developer", List.of(
            "mobile developer", "android developer", "ios developer",
            "flutter developer", "react native developer", "mobile engineer"
        ));
        ROLE_INDICATORS.put("QA Engineer", List.of(
            "qa engineer", "test engineer", "quality assurance engineer",
            "sdet", "automation tester", "qa analyst"
        ));
        ROLE_INDICATORS.put("Cloud Engineer", List.of(
            "cloud engineer", "aws engineer", "azure engineer",
            "cloud architect", "cloud developer", "solutions architect"
        ));
    }

    // ==========================================================================
    //  AGENT ORCHESTRATOR — The "brain" that runs all steps in order
    // ==========================================================================

    /**
     * This is the MAIN AGENT METHOD — the orchestrator.
     *
     * It acts like a project manager that delegates work to specialists:
     *   "Step 1, normalize the resume text."
     *   "Step 2, normalize the job description."
     *   "Step 3, extract keywords by category from JD."
     *   "Step 4, find matched keywords in resume."
     *   "Step 5, find missing keywords."
     *   "Step 6, detect roles and calculate role match."
     *   "Step 7, calculate component scores and weighted ATS."
     *   ... and so on.
     *
     * Each step calls a private method (like a sub-agent doing one task).
     * The orchestrator collects all results and builds the final response.
     */
    public ResumeAnalyzeResponse analyzeResume(ResumeAnalyzeRequest request) {
        String resumeText = request.getResumeText();
        String jobDescription = request.getJobDescription();
        String verifiedSkills = normalizeVerifiedSkills(request.getVerifiedAdditionalSkills());

        AnalysisResult currentAnalysis = analyzeResumeAgainstJD(resumeText, jobDescription);
        String generatedResume = generateResumeForJobDescription(resumeText, jobDescription, verifiedSkills);

        String roleMismatchWarning = generateRoleMismatchWarning(
            currentAnalysis.getResumeRole(), currentAnalysis.getJdRole(), currentAnalysis.getRoleMatchScore()
        );

        String skillGapAnalysis = generateSkillGapAnalysis(
            currentAnalysis.getResumeRole(), currentAnalysis.getJdRole(),
            currentAnalysis.getMissingKeywords(), currentAnalysis.getMatchedKeywords()
        );

        String supportedNorm = buildSupportedNorm(resumeText, verifiedSkills);
        List<String> suggestions = generateSuggestions(
            supportedNorm,
            currentAnalysis.getMissingKeywords(),
            currentAnalysis.getMatchedKeywords(),
            currentAnalysis.getAtsScore(),
            currentAnalysis.getRoleMatchScore(),
            currentAnalysis.getResumeRole(),
            currentAnalysis.getJdRole()
        );

        String improvedSummary = extractSummaryFromGenerated(generatedResume);
        List<String> improvedBulletPoints = extractBulletsFromGenerated(generatedResume);

        System.out.println("ATS Score (current resume): " + currentAnalysis.getAtsScore());
        System.out.println("Matched Keywords: " + currentAnalysis.getMatchedKeywords());
        System.out.println("Missing Keywords: " + currentAnalysis.getMissingKeywords());

        ResumeAnalyzeResponse response = new ResumeAnalyzeResponse(
            currentAnalysis.getAtsScore(),
            0,
            currentAnalysis.getScoreLabel(),
            currentAnalysis.getRoleMatchScore(),
            currentAnalysis.getRequiredSkillScore(),
            currentAnalysis.getMatchedKeywords(),
            currentAnalysis.getMatchedKeywords(),
            List.of(),
            currentAnalysis.getMissingKeywords(),
            roleMismatchWarning,
            skillGapAnalysis,
            suggestions,
            improvedSummary,
            improvedBulletPoints,
            generatedResume,
            ""
        );

        saveToDatabase(request, response);
        return response;
    }

    /**
     * Generates an optimized resume for the JD without scoring it.
     * Scoring happens only when the user pastes the generated resume into the resume input and analyzes again.
     */
    private String generateResumeForJobDescription(String originalResume, String jobDescription, String verifiedSkills) {
        List<String> supportedJdKeywords = getSupportedJDKeywords(jobDescription, originalResume, verifiedSkills);
        String generatedResume = generateOptimizedResumeForJD(originalResume, jobDescription, verifiedSkills);
        generatedResume = removeEducationSection(generatedResume);
        return ensureAllSupportedKeywordsPlaced(
            generatedResume, supportedJdKeywords, buildSupportedNorm(originalResume, verifiedSkills)
        ).trim();
    }

    private String normalizeVerifiedSkills(String verifiedAdditionalSkills) {
        return verifiedAdditionalSkills == null ? "" : verifiedAdditionalSkills.trim();
    }

    private String buildSupportedNorm(String originalResume, String verifiedSkills) {
        return (originalResume + " " + verifiedSkills).toLowerCase();
    }

    /**
     * Analyzes any resume text against a job description and returns weighted ATS scoring.
     */
    public AnalysisResult analyzeResumeAgainstJD(String resumeText, String jobDescription) {
        String resumeNorm = resumeText.toLowerCase();
        String jdNorm = jobDescription.toLowerCase();

        List<String> jdLangs = extractCategoryKeywords(jdNorm, PROGRAMMING_LANGUAGES);
        List<String> jdBackend = extractCategoryKeywords(jdNorm, BACKEND_FRAMEWORKS);
        List<String> jdFrontend = extractCategoryKeywords(jdNorm, FRONTEND_FRAMEWORKS);
        List<String> jdDatabases = extractCategoryKeywords(jdNorm, DATABASES);
        List<String> jdCloud = extractCategoryKeywords(jdNorm, CLOUD_TOOLS);
        List<String> jdDevops = extractCategoryKeywords(jdNorm, DEVOPS_TOOLS);
        List<String> jdTesting = extractCategoryKeywords(jdNorm, TESTING_TOOLS);
        List<String> jdSoft = extractCategoryKeywords(jdNorm, SOFT_SKILLS);

        List<String> allJdKeywords = new ArrayList<>();
        for (List<String> categoryList : List.of(jdLangs, jdBackend, jdFrontend, jdDatabases, jdCloud, jdDevops, jdTesting, jdSoft)) {
            for (String keyword : categoryList) {
                if (!allJdKeywords.contains(keyword)) {
                    allJdKeywords.add(keyword);
                }
            }
        }

        List<String> matchedKeywords = new ArrayList<>();
        List<String> missingKeywords = new ArrayList<>();
        for (String keyword : allJdKeywords) {
            if (matchesKeyword(resumeNorm, keyword)) {
                matchedKeywords.add(keyword);
            } else {
                missingKeywords.add(keyword);
            }
        }

        String resumeRole = detectRole(resumeNorm);
        String jdRole = detectRole(jdNorm);
        int roleMatchScore = calculateRoleMatchScore(resumeNorm, jdNorm, jdRole);

        List<String> requiredSkills = new ArrayList<>();
        requiredSkills.addAll(jdLangs);
        requiredSkills.addAll(jdBackend);
        requiredSkills.addAll(jdFrontend);

        int requiredSkillScore = calculateMatchPercentage(resumeNorm, requiredSkills);

        List<String> toolsDatabaseCloudSkills = new ArrayList<>();
        toolsDatabaseCloudSkills.addAll(jdDevops);
        toolsDatabaseCloudSkills.addAll(jdDatabases);
        toolsDatabaseCloudSkills.addAll(jdCloud);
        toolsDatabaseCloudSkills.addAll(jdTesting);
        int toolsDatabaseCloudScore = calculateMatchPercentage(resumeNorm, toolsDatabaseCloudSkills);

        int softSkillScore = calculateMatchPercentage(resumeNorm, jdSoft);
        int experienceRelevanceScore = calculateExperienceRelevanceScore(resumeNorm, jdNorm, allJdKeywords);

        int atsScore = (int) Math.round(
            requiredSkillScore * 0.45 +
            roleMatchScore * 0.20 +
            experienceRelevanceScore * 0.20 +
            toolsDatabaseCloudScore * 0.10 +
            softSkillScore * 0.05
        );
        atsScore = Math.max(0, Math.min(100, atsScore));
        atsScore = applyHonestScoreCap(atsScore, roleMatchScore, requiredSkillScore, experienceRelevanceScore, missingKeywords);

        AnalysisResult result = new AnalysisResult();
        result.setAtsScore(atsScore);
        result.setScoreLabel(getScoreLabel(atsScore));
        result.setRoleMatchScore(roleMatchScore);
        result.setRequiredSkillScore(requiredSkillScore);
        result.setMatchedKeywords(matchedKeywords);
        result.setMissingKeywords(missingKeywords);
        result.setResumeRole(resumeRole);
        result.setJdRole(jdRole);
        return result;
    }

    private int applyHonestScoreCap(int atsScore, int roleMatchScore, int requiredSkillScore,
                                    int experienceRelevanceScore, List<String> missingKeywords) {
        if (atsScore < 95) {
            return atsScore;
        }
        boolean canReach95 = requiredSkillScore >= 90
            && roleMatchScore >= 85
            && experienceRelevanceScore >= 85
            && missingKeywords.size() <= 2;
        return canReach95 ? atsScore : Math.min(atsScore, 94);
    }

    private int calculateRoleMatchScore(String resumeNorm, String jdNorm, String jdRole) {
        String summary = extractSummaryText(resumeNorm);
        String jdTitle = extractJdTitle(jdNorm);

        if (matchesKeyword(summary, jdRole) || summary.contains(jdTitle)) {
            return 100;
        }

        int relatedness = getRoleRelatedness(detectRole(resumeNorm), jdRole);
        if (relatedness >= 70) return 75;
        if (relatedness >= 40) return 40;
        return 10;
    }

    private String extractSummaryText(String resumeNorm) {
        StringBuilder summary = new StringBuilder();
        boolean inSummary = false;
        for (String line : resumeNorm.split("\\n")) {
            String trimmed = line.trim();
            String lower = trimmed.toLowerCase();
            if (lower.equals("professional summary") || lower.equals("summary")) {
                inSummary = true;
                continue;
            }
            if (inSummary) {
                if (lower.equals("technical skills") || lower.equals("professional experience")
                    || lower.startsWith("experience") || lower.startsWith("skills")) {
                    break;
                }
                if (!trimmed.isEmpty()) summary.append(trimmed).append(" ");
            }
        }
        return summary.length() > 0 ? summary.toString() : resumeNorm;
    }

    private String extractJdTitle(String jdNorm) {
        for (String line : jdNorm.split("\\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && trimmed.length() < 80) {
                return trimmed;
            }
        }
        return jdNorm.trim();
    }

    private int calculateExperienceRelevanceScore(String resumeNorm, String jdNorm, List<String> jdKeywords) {
        String summarySection = extractSummaryText(resumeNorm);
        String skillsSection = extractSection(resumeNorm, "skill");
        String envSection = extractSection(resumeNorm, "environment");
        String expSection = extractSection(resumeNorm, "experience");
        String projSection = extractSection(resumeNorm, "project");
        String combined = summarySection + " " + skillsSection + " " + envSection + " " + expSection + " " + projSection;

        if (combined.trim().length() < 20) return 25;

        int keywordScore = 0;
        if (!jdKeywords.isEmpty()) {
            int matched = 0;
            for (String keyword : jdKeywords) {
                if (matchesKeyword(combined, keyword)) matched++;
            }
            double ratio = matched / (double) jdKeywords.size();
            keywordScore = (int) Math.round(ratio * 85);
            if (ratio >= 0.85) {
                keywordScore = Math.max(keywordScore, 85);
            }
        }

        List<String> responsibilityTerms = extractResponsibilityTerms(jdNorm);
        int responsibilityScore = 15;
        if (!responsibilityTerms.isEmpty()) {
            int matchedResp = 0;
            for (String term : responsibilityTerms) {
                if (combined.contains(term)) matchedResp++;
            }
            responsibilityScore = (int) Math.round((matchedResp / (double) responsibilityTerms.size()) * 15);
        }

        return Math.min(100, keywordScore + responsibilityScore);
    }

    private List<String> extractResponsibilityTerms(String jdNorm) {
        Set<String> terms = new LinkedHashSet<>();
        for (String line : jdNorm.split("\\n")) {
            String lower = line.toLowerCase().trim();
            if (lower.startsWith("-") || lower.startsWith("*") || lower.contains("responsibilit")) {
                for (String term : List.of("develop", "build", "design", "maintain", "deploy", "test",
                    "automate", "integrate", "support", "troubleshoot", "collaborate", "api", "database", "script")) {
                    if (lower.contains(term)) terms.add(term);
                }
            }
        }
        return new ArrayList<>(terms);
    }

    private static class OptimizationOutcome {
        String generatedResume;
        AnalysisResult generatedAnalysis;
        String optimizationStatus;
    }

    /**
     * Generates an optimized resume, re-analyzes it against the JD, and iteratively improves
     * keyword coverage using only skills supported by the original resume or verified skills.
     */
    private OptimizationOutcome optimizeGeneratedResumeUntilTargetScore(
            String originalResume, String jobDescription, String verifiedSkills, AnalysisResult originalAnalysis) {

        OptimizationOutcome outcome = new OptimizationOutcome();
        List<String> supportedJdKeywords = getSupportedJDKeywords(jobDescription, originalResume, verifiedSkills);

        String generatedResume = generateOptimizedResumeForJD(originalResume, jobDescription, verifiedSkills);
        generatedResume = removeEducationSection(generatedResume);
        generatedResume = ensureAllSupportedKeywordsPlaced(generatedResume, supportedJdKeywords, buildSupportedNorm(originalResume, verifiedSkills));

        AnalysisResult generatedAnalysis = analyzeResumeAgainstJD(generatedResume, jobDescription);

        if (generatedAnalysis.getAtsScore() < originalAnalysis.getAtsScore()) {
            generatedResume = preserveMatchedKeywordsAndRegenerate(
                generatedResume, originalResume, jobDescription, verifiedSkills,
                originalAnalysis.getMatchedKeywords(), supportedJdKeywords
            );
            generatedResume = removeEducationSection(generatedResume);
            generatedAnalysis = analyzeResumeAgainstJD(generatedResume, jobDescription);
        }

        int targetScore = originalAnalysis.getRequiredSkillScore() >= 80 ? 90 : 95;
        int attempts = 0;

        while (generatedAnalysis.getAtsScore() < targetScore && attempts < 3) {
            List<String> missingSupported = findMissingSupportedKeywords(
                generatedResume, jobDescription, originalResume, verifiedSkills
            );
            generatedResume = addSupportedKeywordsNaturally(generatedResume, missingSupported, buildSupportedNorm(originalResume, verifiedSkills));
            generatedResume = removeEducationSection(generatedResume);
            generatedAnalysis = analyzeResumeAgainstJD(generatedResume, jobDescription);
            attempts++;
        }

        if (originalAnalysis.getRequiredSkillScore() >= 80 && generatedAnalysis.getAtsScore() < 90) {
            generatedResume = ensureAllSupportedKeywordsPlaced(generatedResume, supportedJdKeywords, buildSupportedNorm(originalResume, verifiedSkills));
            generatedResume = removeEducationSection(generatedResume);
            generatedAnalysis = analyzeResumeAgainstJD(generatedResume, jobDescription);
        }

        if (generatedAnalysis.getAtsScore() < originalAnalysis.getAtsScore()
            && allKeywordsPreservedInResume(generatedResume, originalAnalysis.getMatchedKeywords())) {
            generatedAnalysis.setAtsScore(Math.max(generatedAnalysis.getAtsScore(), originalAnalysis.getAtsScore()));
            generatedAnalysis.setScoreLabel(getScoreLabel(generatedAnalysis.getAtsScore()));
        }

        outcome.generatedResume = generatedResume.trim();
        outcome.generatedAnalysis = generatedAnalysis;
        outcome.optimizationStatus = buildOptimizationStatus(
            generatedAnalysis, originalAnalysis, originalResume, verifiedSkills, jobDescription
        );

        return outcome;
    }

    private List<String> getSupportedJDKeywords(String jobDescription, String originalResume, String verifiedSkills) {
        String originalNorm = originalResume.toLowerCase();
        String verifiedNorm = verifiedSkills.toLowerCase();
        List<String> rawVerifiedTerms = parseRawVerifiedTerms(verifiedSkills);
        List<String> supported = new ArrayList<>();
        for (String keyword : extractAllJdKeywords(jobDescription.toLowerCase())) {
            if (isSkillSupported(keyword, originalNorm, verifiedNorm, rawVerifiedTerms)) {
                supported.add(keyword);
            }
        }
        return supported;
    }

    private List<String> findMissingSupportedKeywords(String generatedResume, String jobDescription,
                                                      String originalResume, String verifiedSkills) {
        List<String> supported = getSupportedJDKeywords(jobDescription, originalResume, verifiedSkills);
        String generatedNorm = generatedResume.toLowerCase();
        List<String> missing = new ArrayList<>();
        for (String keyword : supported) {
            if (!matchesKeyword(generatedNorm, keyword)) {
                missing.add(keyword);
            }
        }
        return missing;
    }

    private String addSupportedKeywordsNaturally(String generatedResume, List<String> missingSupportedKeywords, String supportedNorm) {
        if (missingSupportedKeywords.isEmpty()) return generatedResume;
        return ensureAllSupportedKeywordsPlaced(generatedResume, missingSupportedKeywords, supportedNorm);
    }

    private boolean allKeywordsPreservedInResume(String generatedResume, List<String> originalMatchedKeywords) {
        if (originalMatchedKeywords == null || originalMatchedKeywords.isEmpty()) return true;
        String generatedNorm = generatedResume.toLowerCase();
        for (String keyword : originalMatchedKeywords) {
            if (!matchesKeyword(generatedNorm, keyword)) {
                return false;
            }
        }
        return true;
    }

    private String preserveMatchedKeywordsAndRegenerate(String generatedResume, String originalResume,
                                                        String jobDescription, String verifiedSkills,
                                                        List<String> originalMatchedKeywords,
                                                        List<String> supportedJdKeywords) {
        Set<String> keywordsToPreserve = new LinkedHashSet<>(originalMatchedKeywords);
        keywordsToPreserve.addAll(supportedJdKeywords);
        return ensureAllSupportedKeywordsPlaced(generatedResume, new ArrayList<>(keywordsToPreserve), buildSupportedNorm(originalResume, verifiedSkills));
    }

    private String ensureAllSupportedKeywordsPlaced(String generatedResume, List<String> supportedKeywords, String supportedNorm) {
        if (supportedKeywords.isEmpty()) return generatedResume;

        StringBuilder resume = new StringBuilder(generatedResume);
        String lower = resume.toString().toLowerCase();
        List<String> missing = new ArrayList<>();
        for (String keyword : supportedKeywords) {
            if (!matchesKeyword(lower, keyword)) missing.add(keyword);
        }
        if (missing.isEmpty()) return generatedResume;

        int summaryIdx = resume.toString().toUpperCase().indexOf("PROFESSIONAL SUMMARY");
        int skillsIdx = resume.toString().toUpperCase().indexOf("TECHNICAL SKILLS");
        int expIdx = resume.toString().toUpperCase().indexOf("PROFESSIONAL EXPERIENCE");

        String summaryAddition = " Skilled in " + String.join(", ", missing.subList(0, Math.min(10, missing.size()))) + ".";
        if (skillsIdx > 0) {
            resume.insert(skillsIdx, summaryAddition);
            skillsIdx = resume.toString().toUpperCase().indexOf("TECHNICAL SKILLS");
            expIdx = resume.toString().toUpperCase().indexOf("PROFESSIONAL EXPERIENCE");
        }

        if (skillsIdx >= 0 && expIdx > skillsIdx) {
            resume.insert(expIdx, "Core Competencies: " + String.join(", ", missing) + "\n");
            expIdx = resume.toString().toUpperCase().indexOf("PROFESSIONAL EXPERIENCE");
        }

        int projIdx = resume.toString().toUpperCase().indexOf("PROJECTS");
        int insertPoint = projIdx > 0 ? projIdx : resume.length();
        for (String keyword : missing) {
            if (!matchesKeyword(resume.toString().toLowerCase(), keyword)) {
                String bullet = buildTruthfulBulletForSkill(keyword);
                if (bullet != null && expIdx >= 0) {
                    resume.insert(insertPoint, "- " + bullet + "\n");
                }
            }
        }

        return resume.toString();
    }

    private String buildOptimizationStatus(AnalysisResult generatedAnalysis, AnalysisResult originalAnalysis,
                                           String originalResume, String verifiedSkills, String jobDescription) {
        if (generatedAnalysis.getAtsScore() >= 95) {
            return "Generated resume successfully optimized to 95+ ATS match using truthful resume content.";
        }
        if (generatedAnalysis.getAtsScore() >= 90 && generatedAnalysis.getAtsScore() >= originalAnalysis.getAtsScore()) {
            return "Generated resume successfully optimized to 90+ ATS match using truthful resume content and preserved JD keywords.";
        }
        if (generatedAnalysis.getAtsScore() >= originalAnalysis.getAtsScore()
            && generatedAnalysis.getMatchedKeywords().size() >= originalAnalysis.getMatchedKeywords().size()) {
            return "Generated resume improved ATS alignment while preserving all supported JD keywords from the original resume.";
        }

        String jdRole = originalAnalysis.getJdRole();
        if (jdRole.equals("Python Developer") && !isPythonSupported(originalResume, verifiedSkills)) {
            return "Cannot truthfully reach 95 ATS because the job description requires Python skills that are not present in the original resume or verified additional skills.";
        }

        return "Cannot truthfully reach 95 ATS because required JD skills are missing from the original resume or verified additional skills.";
    }

    /**
     * Aggressively rewrites the resume for the JD using only supported skills from the
     * original resume and verified additional skills.
     */
    private String generateOptimizedResumeForJD(String originalResume, String jobDescription, String verifiedSkills) {
        String originalNorm = originalResume.toLowerCase();
        String verifiedNorm = verifiedSkills.toLowerCase();
        String supportedNorm = buildSupportedNorm(originalResume, verifiedSkills);
        List<String> rawVerifiedTerms = parseRawVerifiedTerms(verifiedSkills);

        JdContext jdContext = buildJdContext(jobDescription);
        List<String> supportedJdKeywords = new ArrayList<>();
        for (String keyword : jdContext.allKeywords) {
            if (isSkillSupported(keyword, originalNorm, verifiedNorm, rawVerifiedTerms)) {
                supportedJdKeywords.add(keyword);
            }
        }

        List<String> allSupportedSkills = extractAllSupportedSkills(supportedNorm, rawVerifiedTerms);
        ContactInfo contact = parseContactInfo(originalResume);

        if (jdContext.jdRole.equals("Python Developer") && isPythonSupported(originalResume, verifiedSkills)) {
            return buildPythonDeveloperResume(contact, originalResume, supportedJdKeywords, allSupportedSkills, supportedNorm, rawVerifiedTerms);
        }

        if (jdContext.jdRole.equals("Full Stack Developer") || jdContext.jdRole.equals("Java Developer")) {
            return buildFullStackOptimizedResume(contact, originalResume, jdContext, supportedJdKeywords, allSupportedSkills, supportedNorm, verifiedSkills);
        }

        return buildGenericOptimizedResume(contact, originalResume, jdContext, supportedJdKeywords, allSupportedSkills, supportedNorm, verifiedSkills);
    }

    private static class JdContext {
        String jdTitle;
        String jdRole;
        List<String> allKeywords = new ArrayList<>();
        List<String> responsibilities = new ArrayList<>();
    }

    private static class ContactInfo {
        String name = "";
        String email = "";
        String phone = "";
        String location = "";
        String linkedin = "";
        String github = "";
    }

    private JdContext buildJdContext(String jobDescription) {
        String jdNorm = jobDescription.toLowerCase();
        JdContext context = new JdContext();
        context.jdTitle = extractJdTitle(jdNorm);
        context.jdRole = detectRole(jdNorm);
        context.allKeywords = extractAllJdKeywords(jdNorm);
        for (String line : jobDescription.split("\\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("-") || trimmed.startsWith("*")) {
                context.responsibilities.add(trimmed.replaceFirst("^[-*]\\s*", ""));
            }
        }
        return context;
    }

    private List<String> extractAllJdKeywords(String jdNorm) {
        List<String> all = new ArrayList<>();
        for (List<String> category : List.of(
            extractCategoryKeywords(jdNorm, PROGRAMMING_LANGUAGES),
            extractCategoryKeywords(jdNorm, BACKEND_FRAMEWORKS),
            extractCategoryKeywords(jdNorm, FRONTEND_FRAMEWORKS),
            extractCategoryKeywords(jdNorm, DATABASES),
            extractCategoryKeywords(jdNorm, CLOUD_TOOLS),
            extractCategoryKeywords(jdNorm, DEVOPS_TOOLS),
            extractCategoryKeywords(jdNorm, TESTING_TOOLS),
            extractCategoryKeywords(jdNorm, SOFT_SKILLS)
        )) {
            for (String keyword : category) {
                if (!all.contains(keyword)) all.add(keyword);
            }
        }
        return all;
    }

    private List<String> parseRawVerifiedTerms(String verifiedSkills) {
        List<String> terms = new ArrayList<>();
        if (verifiedSkills == null || verifiedSkills.isBlank()) return terms;
        for (String part : verifiedSkills.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) terms.add(trimmed);
        }
        return terms;
    }

    private boolean isPythonSupported(String originalResume, String verifiedSkills) {
        String combined = buildSupportedNorm(originalResume, verifiedSkills);
        return matchesKeyword(combined, "Python");
    }

    private boolean isSkillSupported(String keyword, String originalNorm, String verifiedNorm, List<String> rawVerifiedTerms) {
        if (matchesKeyword(originalNorm, keyword) || matchesKeyword(verifiedNorm, keyword)) {
            return true;
        }
        String lowerKeyword = keyword.toLowerCase();
        for (String term : rawVerifiedTerms) {
            String lowerTerm = term.toLowerCase();
            if (lowerTerm.contains(lowerKeyword) || lowerKeyword.contains(lowerTerm)) {
                return true;
            }
        }
        return false;
    }

    private List<String> extractAllSupportedSkills(String supportedNorm, List<String> rawVerifiedTerms) {
        Set<String> skills = new LinkedHashSet<>(extractAllResumeSkills(supportedNorm));
        for (List<String> category : List.of(PROGRAMMING_LANGUAGES, BACKEND_FRAMEWORKS, FRONTEND_FRAMEWORKS,
            DATABASES, CLOUD_TOOLS, DEVOPS_TOOLS, TESTING_TOOLS, SOFT_SKILLS, MISC_TOOLS)) {
            for (String skill : category) {
                if (matchesKeyword(supportedNorm, skill)) skills.add(skill);
            }
        }
        for (String term : rawVerifiedTerms) {
            skills.add(term);
        }
        return new ArrayList<>(skills);
    }

    private ContactInfo parseContactInfo(String originalResume) {
        ContactInfo contact = new ContactInfo();
        String[] lines = originalResume.split("\\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            String lower = trimmed.toLowerCase();

            if (lower.contains("@") && lower.contains(".") && contact.email.isEmpty()) {
                contact.email = lower.startsWith("email")
                    ? trimmed.replaceFirst("(?i)email\\s*[:|-]?\\s*", "").trim() : trimmed;
            } else if ((lower.contains("phone") || lower.matches(".*\\d{3}.*\\d{3}.*\\d{4}.*")) && contact.phone.isEmpty()) {
                contact.phone = lower.startsWith("phone")
                    ? trimmed.replaceFirst("(?i)phone\\s*[:|-]?\\s*", "").trim() : trimmed;
            } else if ((lower.contains("location") || lower.matches(".*\\b[a-z]+,\\s*[a-z]{2}\\b.*")) && contact.location.isEmpty()) {
                contact.location = lower.startsWith("location")
                    ? trimmed.replaceFirst("(?i)location\\s*[:|-]?\\s*", "").trim() : trimmed;
            } else if ((lower.contains("linkedin.com") || lower.startsWith("linkedin")) && contact.linkedin.isEmpty()) {
                contact.linkedin = lower.startsWith("linkedin")
                    ? trimmed.replaceFirst("(?i)linkedin\\s*[:|-]?\\s*", "").trim() : trimmed;
            } else if ((lower.contains("github.com") || lower.startsWith("github")) && contact.github.isEmpty()) {
                contact.github = lower.startsWith("github")
                    ? trimmed.replaceFirst("(?i)github\\s*[:|-]?\\s*", "").trim() : trimmed;
            } else if (contact.name.isEmpty() && !lower.startsWith("-") && !lower.startsWith("*")
                && !lower.contains("summary") && !lower.contains("experience")
                && !lower.contains("skill") && !lower.contains("project")
                && !lower.contains("education") && !lower.contains("@")
                && !lower.matches(".*\\d{3}.*\\d{3}.*\\d{4}.*")
                && trimmed.length() > 1 && trimmed.length() < 60) {
                contact.name = trimmed;
            }

            if (lower.contains("summary") || lower.contains("experience") || lower.contains("skill") || lower.contains("objective")) {
                break;
            }
        }
        return contact;
    }

    private void appendContactHeader(StringBuilder resume, ContactInfo contact) {
        if (!contact.name.isEmpty()) resume.append(contact.name).append("\n");
        if (!contact.email.isEmpty()) resume.append("Email: ").append(contact.email).append("\n");
        if (!contact.phone.isEmpty()) resume.append("Phone: ").append(contact.phone).append("\n");
        if (!contact.location.isEmpty()) resume.append("Location: ").append(contact.location).append("\n");
        if (!contact.linkedin.isEmpty()) resume.append("LinkedIn: ").append(contact.linkedin).append("\n");
        if (!contact.github.isEmpty()) resume.append("GitHub: ").append(contact.github).append("\n");
    }

    private String buildPythonDeveloperResume(ContactInfo contact, String originalResume,
                                              List<String> supportedJdKeywords, List<String> allSupportedSkills,
                                              String supportedNorm, List<String> rawVerifiedTerms) {
        StringBuilder resume = new StringBuilder();
        appendContactHeader(resume, contact);

        resume.append("\nPROFESSIONAL SUMMARY\n");
        resume.append(buildPythonDeveloperSummary(supportedNorm, supportedJdKeywords, rawVerifiedTerms));

        resume.append("\nTECHNICAL SKILLS\n");
        appendPythonDeveloperSkills(resume, allSupportedSkills, supportedNorm, rawVerifiedTerms);

        resume.append("\nPROFESSIONAL EXPERIENCE\n\n");
        appendPreservedExperienceHeaders(originalResume, resume);
        appendRewrittenExperienceBullets(originalResume, resume, supportedJdKeywords, supportedNorm);
        appendPythonDeveloperBullets(resume, supportedNorm, supportedJdKeywords);

        String projects = extractAndRewriteProjects(originalResume, supportedJdKeywords, supportedNorm);
        if (!projects.isEmpty()) {
            resume.append("\nPROJECTS\n\n").append(projects);
        }

        return ensureAllSupportedKeywordsPlaced(resume.toString(), supportedJdKeywords, supportedNorm);
    }

    private String buildPythonDeveloperSummary(String supportedNorm, List<String> supportedJdKeywords, List<String> rawVerifiedTerms) {
        StringBuilder summary = new StringBuilder();
        summary.append("Python Developer with experience in designing, developing, and maintaining API-driven services, ");
        summary.append("automation scripts, and database-focused applications. ");

        List<String> highlightSkills = new ArrayList<>();
        for (String skill : List.of("Python", "FastAPI", "Flask", "Django", "PostgreSQL", "SQLAlchemy",
            "REST API", "Docker", "Git", "Jenkins", "Jira", "Agile", "Scrum", "GCP", "MySQL", "Cloud SQL")) {
            if (matchesKeyword(supportedNorm, skill) || rawTermsMatch(rawVerifiedTerms, skill)) {
                if (!highlightSkills.contains(skill)) highlightSkills.add(getDisplaySkillName(skill, supportedNorm));
            }
        }
        if (!supportedJdKeywords.isEmpty()) {
            for (String kw : supportedJdKeywords) {
                if (!highlightSkills.contains(kw)) highlightSkills.add(kw);
            }
        }
        if (!highlightSkills.isEmpty()) {
            summary.append("Skilled in ").append(String.join(", ", highlightSkills.subList(0, Math.min(12, highlightSkills.size())))).append(". ");
        }
        summary.append("Experienced in building backend services, optimizing SQL queries, supporting database workflows, ");
        summary.append("and collaborating with cross-functional teams to deliver scalable solutions.\n");
        return summary.toString();
    }

    private String getDisplaySkillName(String skill, String supportedNorm) {
        if (skill.equals("Python") && (supportedNorm.contains("python 3") || supportedNorm.contains("python3"))) {
            return "Python 3.x";
        }
        if (skill.equals("REST API") && supportedNorm.contains("rest apis")) {
            return "REST APIs";
        }
        return skill;
    }

    private void appendPythonDeveloperSkills(StringBuilder resume, List<String> allSupportedSkills,
                                             String supportedNorm, List<String> rawVerifiedTerms) {
        List<String> langs = new ArrayList<>();
        if (matchesKeyword(supportedNorm, "Python") || rawTermsMatch(rawVerifiedTerms, "Python")) {
            langs.add(getDisplaySkillName("Python", supportedNorm));
        }
        for (String lang : List.of("SQL", "JavaScript", "Java")) {
            if (matchesKeyword(supportedNorm, lang) || rawTermsMatch(rawVerifiedTerms, lang)) langs.add(lang);
        }
        List<String> backend = filterSupported(List.of("REST API", "Django", "Flask", "FastAPI", "Microservices", "SQLAlchemy"), supportedNorm, rawVerifiedTerms);
        List<String> databases = filterSupported(List.of("PostgreSQL", "MySQL", "SQL", "Cloud SQL"), supportedNorm, rawVerifiedTerms);
        List<String> cloudDevops = filterSupported(List.of("GCP", "AWS", "Docker", "Jenkins", "Git", "Kubernetes"), supportedNorm, rawVerifiedTerms);
        List<String> tools = filterSupportedMisc(List.of("Jira", "Postman", "IntelliJ IDEA", "VS Code"), supportedNorm, rawVerifiedTerms);
        List<String> process = filterSupported(List.of("Agile", "Scrum", "SDLC"), supportedNorm, rawVerifiedTerms);
        List<String> testing = filterSupported(List.of("PyTest"), supportedNorm, rawVerifiedTerms);

        if (!langs.isEmpty()) resume.append("Programming Languages: ").append(String.join(", ", langs)).append("\n");
        if (!backend.isEmpty()) resume.append("Backend/API: ").append(String.join(", ", backend)).append("\n");
        if (!databases.isEmpty()) resume.append("Database: ").append(String.join(", ", databases)).append("\n");
        if (!cloudDevops.isEmpty()) resume.append("Cloud/DevOps: ").append(String.join(", ", cloudDevops)).append("\n");
        if (!testing.isEmpty()) resume.append("Testing: ").append(String.join(", ", testing)).append("\n");
        if (!tools.isEmpty()) resume.append("Tools: ").append(String.join(", ", tools)).append("\n");
        if (!process.isEmpty()) resume.append("Methodologies: ").append(String.join(", ", process)).append("\n");

        List<String> extras = new ArrayList<>();
        for (String term : rawVerifiedTerms) {
            String lower = term.toLowerCase();
            if (lower.contains("pl/sql") || lower.contains("automation") || lower.contains("validation")) {
                extras.add(term);
            }
        }
        if (!extras.isEmpty()) {
            resume.append("Additional Skills: ").append(String.join(", ", extras)).append("\n");
        }
    }

    private void appendPythonDeveloperBullets(StringBuilder resume, String supportedNorm, List<String> supportedJdKeywords) {
        List<String> bullets = new ArrayList<>();
        if (matchesKeyword(supportedNorm, "Python")) {
            bullets.add("Developed and maintained Python-based backend services and automation scripts to support database management and application workflows.");
        }
        if (containsAny(supportedJdKeywords, "REST API", "FastAPI", "Flask", "Django") || matchesKeyword(supportedNorm, "REST API")) {
            bullets.add("Designed and enhanced REST API-driven services using Python frameworks such as FastAPI, Flask, and Django where applicable.");
        }
        if (containsAny(supportedJdKeywords, "PostgreSQL", "SQL", "MySQL", "SQLAlchemy") || matchesKeyword(supportedNorm, "PostgreSQL")) {
            bullets.add("Worked with PostgreSQL, MySQL, SQL queries, and SQLAlchemy to support data validation, reporting, and backend application logic.");
        }
        if (matchesKeyword(supportedNorm, "Python") || matchesKeyword(supportedNorm, "SQL")) {
            bullets.add("Improved backend responsiveness by tuning SQL queries, connection usage, and Python service logic for high-concurrency scenarios.");
        }
        if (matchesKeyword(supportedNorm, "Git") || matchesKeyword(supportedNorm, "Jenkins") || matchesKeyword(supportedNorm, "Jira")) {
            bullets.add("Used Git branching, pull requests, Jenkins pipelines, Jira tracking, and Agile/Scrum practices to support development and release workflows.");
        }
        if (containsAny(supportedJdKeywords, "Agile", "Scrum") || matchesKeyword(supportedNorm, "Agile")) {
            bullets.add("Collaborated with cross-functional teams to troubleshoot application issues, support database workflows, and deliver sprint commitments.");
        }

        String existing = resume.toString().toLowerCase();
        for (String bullet : bullets) {
            if (!existing.contains(bullet.substring(0, Math.min(40, bullet.length())).toLowerCase())) {
                resume.append("- ").append(bullet).append("\n");
            }
        }
    }

    private List<String> filterSupported(List<String> candidates, String supportedNorm, List<String> rawVerifiedTerms) {
        List<String> found = new ArrayList<>();
        for (String candidate : candidates) {
            if (matchesKeyword(supportedNorm, candidate) || rawTermsMatch(rawVerifiedTerms, candidate)) {
                if (!found.contains(candidate)) found.add(candidate);
            }
        }
        return found;
    }

    private List<String> filterSupportedMisc(List<String> candidates, String supportedNorm, List<String> rawVerifiedTerms) {
        List<String> found = filterSupported(candidates, supportedNorm, rawVerifiedTerms);
        for (String tool : MISC_TOOLS) {
            if (matchesKeyword(supportedNorm, tool) && !found.contains(tool)) {
                found.add(tool);
            }
        }
        return found;
    }

    private boolean rawTermsMatch(List<String> rawVerifiedTerms, String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        for (String term : rawVerifiedTerms) {
            if (term.toLowerCase().contains(lowerKeyword) || lowerKeyword.contains(term.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String resolveSummaryRole(JdContext jdContext, String originalResume, String verifiedSkills,
                                      List<String> supportedJdKeywords) {
        if (jdContext.jdRole.equals("Python Developer") && !isPythonSupported(originalResume, verifiedSkills)) {
            return detectRole(originalResume.toLowerCase());
        }
        if (supportedJdKeywords.size() >= Math.max(3, jdContext.allKeywords.size() / 2)) {
            return jdContext.jdRole;
        }
        return detectRole(originalResume.toLowerCase());
    }

    private String buildFullStackOptimizedResume(ContactInfo contact, String originalResume, JdContext jdContext,
                                                 List<String> supportedJdKeywords, List<String> allSupportedSkills,
                                                 String supportedNorm, String verifiedSkills) {
        StringBuilder resume = new StringBuilder();
        appendContactHeader(resume, contact);

        resume.append("\nPROFESSIONAL SUMMARY\n");
        resume.append(resolveSummaryRole(jdContext, originalResume, verifiedSkills, supportedJdKeywords)).append(" with hands-on experience in ");
        resume.append(String.join(", ", supportedJdKeywords.subList(0, Math.min(8, supportedJdKeywords.size()))));
        resume.append(". Experienced in building RESTful APIs, microservices, responsive user interfaces, ");
        resume.append("cloud deployments, and production-ready full stack solutions.\n");

        resume.append("\nTECHNICAL SKILLS\n");
        appendCategorizedSkills(resume, prioritizeJdMatchedSkills(supportedJdKeywords, allSupportedSkills));

        resume.append("\nPROFESSIONAL EXPERIENCE\n\n");
        appendPreservedExperienceHeaders(originalResume, resume);
        appendRewrittenExperienceBullets(originalResume, resume, supportedJdKeywords, supportedNorm);
        String projects = extractAndRewriteProjects(originalResume, supportedJdKeywords, supportedNorm);
        appendJdTargetedBullets(resume, supportedJdKeywords, supportedNorm, extractExperienceAndProjectText(resume.toString()));
        if (!projects.isEmpty()) {
            resume.append("\nPROJECTS\n\n").append(projects);
            appendJdTargetedBullets(resume, supportedJdKeywords, supportedNorm, extractExperienceAndProjectText(resume.toString()));
        }

        return aggressivelyPlaceKeywords(resume.toString(), supportedJdKeywords, supportedNorm);
    }

    private String buildGenericOptimizedResume(ContactInfo contact, String originalResume, JdContext jdContext,
                                               List<String> supportedJdKeywords, List<String> allSupportedSkills,
                                               String supportedNorm, String verifiedSkills) {
        StringBuilder resume = new StringBuilder();
        appendContactHeader(resume, contact);
        String summaryRole = resolveSummaryRole(jdContext, originalResume, verifiedSkills, supportedJdKeywords);

        resume.append("\nPROFESSIONAL SUMMARY\n");
        if (!supportedJdKeywords.isEmpty()) {
            resume.append(summaryRole).append(" with experience in ");
            resume.append(String.join(", ", supportedJdKeywords.subList(0, Math.min(8, supportedJdKeywords.size()))));
            resume.append(". ");
        } else {
            resume.append(summaryRole).append(" with transferable software development experience. ");
        }
        resume.append("Experienced in delivering production-ready solutions aligned with job requirements using verified skills only.\n");

        resume.append("\nTECHNICAL SKILLS\n");
        appendCategorizedSkills(resume, prioritizeJdMatchedSkills(supportedJdKeywords, allSupportedSkills));

        resume.append("\nPROFESSIONAL EXPERIENCE\n\n");
        appendPreservedExperienceHeaders(originalResume, resume);
        appendRewrittenExperienceBullets(originalResume, resume, supportedJdKeywords, supportedNorm);
        String projects = extractAndRewriteProjects(originalResume, supportedJdKeywords, supportedNorm);
        appendJdTargetedBullets(resume, supportedJdKeywords, supportedNorm, extractExperienceAndProjectText(resume.toString()));
        if (!projects.isEmpty()) {
            resume.append("\nPROJECTS\n\n").append(projects);
            appendJdTargetedBullets(resume, supportedJdKeywords, supportedNorm, extractExperienceAndProjectText(resume.toString()));
        }

        return aggressivelyPlaceKeywords(resume.toString(), supportedJdKeywords, supportedNorm);
    }

    private String extractExperienceAndProjectText(String resumeText) {
        String lower = resumeText.toLowerCase();
        return extractSection(lower, "experience") + " " + extractSection(lower, "project");
    }

    private void appendPreservedExperienceHeaders(String originalResume, StringBuilder resume) {
        boolean inExperience = false;
        for (String line : originalResume.split("\\n")) {
            String trimmed = line.trim();
            String lower = trimmed.toLowerCase();
            if (lower.matches("^(professional\\s+)?experience.*") || lower.equals("work experience")) {
                inExperience = true;
                continue;
            }
            if (inExperience) {
                if (lower.startsWith("project") || lower.contains("education") || lower.contains("skill")) break;
                if (!trimmed.isEmpty() && !trimmed.startsWith("-") && !trimmed.startsWith("*") && !trimmed.startsWith("•")) {
                    resume.append(trimmed).append("\n");
                }
            }
        }
    }

    private void appendRewrittenExperienceBullets(String originalResume, StringBuilder resume,
                                                  List<String> supportedJdKeywords, String supportedNorm) {
        boolean inExperience = false;
        for (String line : originalResume.split("\\n")) {
            String trimmed = line.trim();
            String lower = trimmed.toLowerCase();
            if (lower.matches("^(professional\\s+)?experience.*") || lower.equals("work experience")) {
                inExperience = true;
                continue;
            }
            if (inExperience) {
                if (lower.startsWith("project") || lower.contains("education") || lower.contains("skill")) break;
                if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•")) {
                    String bullet = improveBulletWording(trimmed.replaceFirst("^[-*•]\\s*", ""));
                    bullet = rewriteBulletWithJdLanguage(bullet, supportedJdKeywords, supportedNorm);
                    resume.append("- ").append(bullet).append("\n");
                }
            }
        }
    }

    private String extractAndRewriteProjects(String originalResume, List<String> supportedJdKeywords, String supportedNorm) {
        StringBuilder projects = new StringBuilder();
        boolean inProjects = false;
        for (String line : originalResume.split("\\n")) {
            String trimmed = line.trim();
            String lower = trimmed.toLowerCase();
            if (lower.startsWith("project")) {
                inProjects = true;
                continue;
            }
            if (inProjects) {
                if (lower.contains("education") || lower.contains("skill")) break;
                if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•")) {
                    String bullet = improveBulletWording(trimmed.replaceFirst("^[-*•]\\s*", ""));
                    bullet = rewriteBulletWithJdLanguage(bullet, supportedJdKeywords, supportedNorm);
                    projects.append("- ").append(bullet).append("\n");
                } else if (!trimmed.isEmpty()) {
                    projects.append(trimmed).append("\n");
                }
            }
        }
        return projects.toString();
    }

    private String rewriteBulletWithJdLanguage(String bullet, List<String> supportedJdKeywords, String supportedNorm) {
        List<String> missingInBullet = new ArrayList<>();
        String bulletLower = bullet.toLowerCase();
        for (String keyword : supportedJdKeywords) {
            if (matchesKeyword(supportedNorm, keyword) && !matchesKeyword(bulletLower, keyword)) {
                missingInBullet.add(keyword);
            }
        }
        if (missingInBullet.isEmpty()) {
            return bullet;
        }
        String additions = String.join(", ", missingInBullet.subList(0, Math.min(3, missingInBullet.size())));
        if (!bullet.endsWith(".")) bullet += ".";
        return bullet + " Utilized " + additions + " to deliver scalable, production-ready outcomes";
    }

    private void appendJdTargetedBullets(StringBuilder resume, List<String> supportedJdKeywords,
                                         String supportedNorm, String experienceProjectContent) {
        String contentLower = experienceProjectContent.toLowerCase();
        int added = 0;
        for (String keyword : supportedJdKeywords) {
            if (!matchesKeyword(supportedNorm, keyword) || matchesKeyword(contentLower, keyword)) continue;
            String bullet = buildTruthfulBulletForSkill(keyword);
            if (bullet != null) {
                resume.append("- ").append(bullet).append("\n");
                added++;
            }
            if (added >= 12) break;
        }
    }

    private String aggressivelyPlaceKeywords(String resume, List<String> supportedJdKeywords, String supportedNorm) {
        String result = resume;
        String lower = result.toLowerCase();
        List<String> stillMissing = new ArrayList<>();
        for (String keyword : supportedJdKeywords) {
            if (!matchesKeyword(lower, keyword)) stillMissing.add(keyword);
        }
        if (stillMissing.isEmpty()) return removeEducationSection(result).trim();

        String additions = String.join(", ", stillMissing.subList(0, Math.min(5, stillMissing.size())));
        int skillsIdx = result.toUpperCase().indexOf("TECHNICAL SKILLS");
        int expIdx = result.toUpperCase().indexOf("PROFESSIONAL EXPERIENCE");
        if (skillsIdx >= 0 && expIdx > skillsIdx) {
            result = result.substring(0, expIdx) + "JD-Aligned Skills: " + additions + "\n" + result.substring(expIdx);
        }
        return removeEducationSection(result).trim();
    }

    private List<String> prioritizeJdMatchedSkills(List<String> matchedKeywords, List<String> allResumeSkills) {
        List<String> ordered = new ArrayList<>(matchedKeywords);
        for (String skill : allResumeSkills) {
            if (!ordered.contains(skill)) {
                ordered.add(skill);
            }
        }
        return ordered;
    }

    private String generateJdOptimizedSummary(String resumeNorm, List<String> matchedKeywords,
                                               String resumeRole, String jdRole,
                                               int roleMatchScore, List<String> allResumeSkills) {
        StringBuilder summary = new StringBuilder();
        String summaryRole = roleMatchScore >= 70 ? jdRole : resumeRole;
        summary.append("Results-driven ").append(summaryRole);
        summary.append(" with hands-on experience in ");

        List<String> skillsToShow = matchedKeywords.isEmpty() ? allResumeSkills : matchedKeywords;
        List<String> topSkills = skillsToShow.subList(0, Math.min(8, skillsToShow.size()));
        if (topSkills.isEmpty()) {
            summary.append("software development");
        } else {
            summary.append(String.join(", ", topSkills));
        }
        summary.append(". ");

        String domainRole = roleMatchScore >= 70 ? jdRole : resumeRole;
        if (domainRole.contains("Frontend")) {
            summary.append("Experienced in building responsive user interfaces, accessible web applications, and modern frontend architectures. ");
        } else if (domainRole.contains("Backend") || domainRole.contains("Java") || domainRole.contains("Python")) {
            summary.append("Experienced in building RESTful APIs, server-side architectures, microservices, and data-driven systems. ");
        } else if (domainRole.contains("Full Stack")) {
            summary.append("Experienced in building RESTful APIs, microservices, and responsive user interfaces across the full stack. ");
        } else if (domainRole.contains("DevOps")) {
            summary.append("Experienced in automating infrastructure, CI/CD pipelines, and cloud deployments. ");
        } else {
            summary.append("Experienced in building reliable, production-ready software solutions aligned with business requirements. ");
        }

        if (roleMatchScore < 70 && !resumeRole.equals(jdRole)) {
            summary.append("Brings transferable technical skills while maintaining an honest representation of demonstrated experience. ");
        }

        if (!matchedKeywords.isEmpty()) {
            List<String> highlight = matchedKeywords.subList(0, Math.min(4, matchedKeywords.size()));
            summary.append("Proven expertise with ").append(String.join(", ", highlight))
                   .append(" in professional and project environments. ");
        }

        summary.append("Strong collaborator with a passion for writing clean, well-tested code and delivering impactful solutions.");
        return summary.toString();
    }

    private String assembleOptimizedResume(String originalResume, String improvedSummary,
                                            List<String> jdMatchedSkills, List<String> allResumeSkills,
                                            List<String> matchedKeywords, String resumeNorm) {
        StringBuilder resume = new StringBuilder();
        String[] lines = originalResume.split("\\n");

        String name = "";
        String email = "";
        String phone = "";
        String linkedin = "";
        String github = "";

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String lower = trimmed.toLowerCase();
            if (lower.contains("@") && lower.contains(".") && email.isEmpty()) {
                email = lower.startsWith("email")
                    ? trimmed.replaceFirst("(?i)email\\s*[:|-]?\\s*", "").trim()
                    : trimmed;
            } else if ((lower.contains("phone") || lower.matches(".*\\d{3}.*\\d{3}.*\\d{4}.*")) && phone.isEmpty()) {
                phone = lower.startsWith("phone")
                    ? trimmed.replaceFirst("(?i)phone\\s*[:|-]?\\s*", "").trim()
                    : trimmed;
            } else if ((lower.contains("linkedin.com") || lower.startsWith("linkedin")) && linkedin.isEmpty()) {
                linkedin = lower.startsWith("linkedin")
                    ? trimmed.replaceFirst("(?i)linkedin\\s*[:|-]?\\s*", "").trim()
                    : trimmed;
            } else if ((lower.contains("github.com") || lower.startsWith("github")) && github.isEmpty()) {
                github = lower.startsWith("github")
                    ? trimmed.replaceFirst("(?i)github\\s*[:|-]?\\s*", "").trim()
                    : trimmed;
            } else if (name.isEmpty() && !lower.startsWith("-") && !lower.startsWith("*")
                        && !lower.contains("summary") && !lower.contains("experience")
                        && !lower.contains("skill") && !lower.contains("project")
                        && !lower.contains("education") && !lower.contains("@")
                        && !lower.matches(".*\\d{3}.*\\d{3}.*\\d{4}.*")
                        && trimmed.length() > 1 && trimmed.length() < 60) {
                name = trimmed;
            }

            if (lower.contains("summary") || lower.contains("experience")
                || lower.contains("skill") || lower.contains("objective")) {
                break;
            }
        }

        if (!name.isEmpty()) resume.append(name).append("\n");
        if (!email.isEmpty()) resume.append("Email: ").append(email).append("\n");
        if (!phone.isEmpty()) resume.append("Phone: ").append(phone).append("\n");
        if (!linkedin.isEmpty()) resume.append("LinkedIn: ").append(linkedin).append("\n");
        if (!github.isEmpty()) resume.append("GitHub: ").append(github).append("\n");

        resume.append("\nPROFESSIONAL SUMMARY\n");
        resume.append(improvedSummary).append("\n");

        resume.append("\nTECHNICAL SKILLS\n");
        appendCategorizedSkills(resume, jdMatchedSkills);

        resume.append("\nPROFESSIONAL EXPERIENCE\n\n");

        boolean inExperience = false;
        boolean inProjects = false;
        boolean inEducation = false;
        boolean inSkills = false;
        StringBuilder experienceBlock = new StringBuilder();
        StringBuilder projectsBlock = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            String lower = trimmed.toLowerCase();

            if (lower.matches("^(professional\\s+)?experience.*") || lower.equals("work experience")) {
                inExperience = true; inProjects = false; inEducation = false; inSkills = false;
                continue;
            }
            if (lower.startsWith("project")) {
                inProjects = true; inExperience = false; inEducation = false; inSkills = false;
                continue;
            }
            if (lower.contains("education") || lower.contains("bachelor") || lower.contains("master")
                || lower.contains("university") || lower.contains("graduation") || lower.contains("degree")) {
                inEducation = true; inExperience = false; inProjects = false; inSkills = false;
                continue;
            }
            if (lower.contains("skill") || lower.contains("technical")) {
                inSkills = true; inExperience = false; inProjects = false; inEducation = false;
                continue;
            }
            if (lower.contains("summary") || lower.contains("objective") || lower.contains("certification")) {
                inExperience = false; inProjects = false; inEducation = false; inSkills = false;
                continue;
            }

            if (inEducation || inSkills) continue;

            if (inExperience && !trimmed.isEmpty()) {
                if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•")) {
                    String bullet = improveBulletWording(trimmed.replaceFirst("^[-*•]\\s*", ""));
                    bullet = weaveJdKeywordsIntoBullet(bullet, matchedKeywords, resumeNorm);
                    experienceBlock.append("- ").append(bullet).append("\n");
                } else {
                    experienceBlock.append(trimmed).append("\n");
                }
            }

            if (inProjects && !trimmed.isEmpty()) {
                if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•")) {
                    String bullet = improveBulletWording(trimmed.replaceFirst("^[-*•]\\s*", ""));
                    bullet = weaveJdKeywordsIntoBullet(bullet, matchedKeywords, resumeNorm);
                    projectsBlock.append("- ").append(bullet).append("\n");
                } else {
                    projectsBlock.append(trimmed).append("\n");
                }
            }
        }

        List<String> supplementalBullets = generateSupplementalBullets(matchedKeywords, resumeNorm, experienceBlock.toString() + projectsBlock);
        for (String bullet : supplementalBullets) {
            experienceBlock.append("- ").append(bullet).append("\n");
        }

        if (experienceBlock.length() > 0) {
            resume.append(experienceBlock);
        }

        if (projectsBlock.length() > 0) {
            resume.append("\nPROJECTS\n\n");
            resume.append(projectsBlock);
        }

        return removeEducationSection(resume.toString()).trim();
    }

    private void appendCategorizedSkills(StringBuilder resume, List<String> skills) {
        List<String> langs = new ArrayList<>();
        List<String> backend = new ArrayList<>();
        List<String> frontend = new ArrayList<>();
        List<String> databases = new ArrayList<>();
        List<String> cloud = new ArrayList<>();
        List<String> devops = new ArrayList<>();
        List<String> testing = new ArrayList<>();
        List<String> soft = new ArrayList<>();
        List<String> other = new ArrayList<>();

        for (String skill : skills) {
            switch (getCategory(skill)) {
                case "lang": langs.add(skill); break;
                case "backend": backend.add(skill); break;
                case "frontend": frontend.add(skill); break;
                case "database": databases.add(skill); break;
                case "cloud": cloud.add(skill); break;
                case "devops": devops.add(skill); break;
                case "testing": testing.add(skill); break;
                case "soft": soft.add(skill); break;
                default: other.add(skill); break;
            }
        }

        if (!langs.isEmpty()) resume.append("Programming Languages: ").append(String.join(", ", langs)).append("\n");
        if (!backend.isEmpty()) resume.append("Backend: ").append(String.join(", ", backend)).append("\n");
        if (!frontend.isEmpty()) resume.append("Frontend: ").append(String.join(", ", frontend)).append("\n");
        if (!databases.isEmpty()) resume.append("Database: ").append(String.join(", ", databases)).append("\n");
        if (!cloud.isEmpty()) resume.append("Cloud: ").append(String.join(", ", cloud)).append("\n");
        if (!devops.isEmpty()) resume.append("DevOps Tools: ").append(String.join(", ", devops)).append("\n");
        if (!testing.isEmpty()) resume.append("Testing: ").append(String.join(", ", testing)).append("\n");
        if (!soft.isEmpty()) resume.append("Practices: ").append(String.join(", ", soft)).append("\n");
        if (!other.isEmpty()) resume.append("Other: ").append(String.join(", ", other)).append("\n");
    }

    private String weaveJdKeywordsIntoBullet(String bullet, List<String> matchedKeywords, String resumeNorm) {
        String bulletLower = bullet.toLowerCase();
        List<String> toWeave = new ArrayList<>();
        for (String keyword : matchedKeywords) {
            if (matchesKeyword(resumeNorm, keyword) && !matchesKeyword(bulletLower, keyword)) {
                toWeave.add(keyword);
            }
        }
        if (toWeave.isEmpty()) {
            return bullet;
        }
        List<String> weave = toWeave.subList(0, Math.min(2, toWeave.size()));
        if (!bullet.endsWith(".")) {
            bullet = bullet + ".";
        }
        return bullet + " Applied " + String.join(" and ", weave) + " to deliver production-ready solutions";
    }

    private List<String> generateSupplementalBullets(List<String> matchedKeywords, String resumeNorm, String existingContent) {
        List<String> bullets = new ArrayList<>();
        String contentLower = existingContent.toLowerCase();

        for (String keyword : matchedKeywords) {
            if (!matchesKeyword(resumeNorm, keyword)) continue;
            if (matchesKeyword(contentLower, keyword)) continue;

            String bullet = buildTruthfulBulletForSkill(keyword);
            if (bullet != null && !bullets.contains(bullet)) {
                bullets.add(bullet);
            }
            if (bullets.size() >= 4) break;
        }

        return bullets;
    }

    private String buildTruthfulBulletForSkill(String skill) {
        if (containsAny(List.of(skill), "Java", "Spring Boot", "Spring")) {
            return "Developed and maintained RESTful APIs using Java and Spring Boot, ensuring high performance and reliability";
        }
        if (containsAny(List.of(skill), "Microservices")) {
            return "Designed and implemented microservices architecture, improving system scalability and deployment flexibility";
        }
        if (containsAny(List.of(skill), "React", "Angular", "Vue")) {
            return "Built responsive and accessible UI components using modern frontend frameworks, enhancing user experience across devices";
        }
        if (containsAny(List.of(skill), "Docker", "Kubernetes")) {
            return "Containerized applications using Docker and orchestrated deployments with Kubernetes for consistent environments";
        }
        if (containsAny(List.of(skill), "SQL", "PostgreSQL", "MySQL", "MongoDB")) {
            return "Designed and optimized database schemas and queries, improving data retrieval efficiency and reliability";
        }
        if (containsAny(List.of(skill), "AWS", "Azure", "GCP")) {
            return "Deployed and managed cloud infrastructure to ensure application scalability and cost efficiency";
        }
        if (containsAny(List.of(skill), "CI/CD", "Jenkins", "GitHub Actions")) {
            return "Implemented CI/CD pipelines to automate build, test, and deployment workflows";
        }
        if (containsAny(List.of(skill), "JUnit", "PyTest", "Mockito", "Jest")) {
            return "Wrote comprehensive unit and integration tests to maintain code quality and prevent regressions";
        }
        if (containsAny(List.of(skill), "Agile", "Scrum")) {
            return "Collaborated in Agile/Scrum teams, participating in sprint planning, code reviews, and retrospectives";
        }
        if (containsAny(List.of(skill), "Python", "Django", "Flask", "FastAPI")) {
            return "Developed backend services and APIs using Python frameworks, focusing on clean architecture and maintainability";
        }
        if (containsAny(List.of(skill), "JavaScript", "TypeScript", "HTML", "CSS")) {
            return "Developed interactive web applications using JavaScript/TypeScript with clean, maintainable code";
        }
        if (containsAny(List.of(skill), "Git")) {
            return "Managed source code with Git, following branching strategies and conducting code reviews";
        }
        if (containsAny(List.of(skill), "REST API")) {
            return "Designed and delivered REST API endpoints supporting scalable application integrations";
        }
        if (containsAny(List.of(skill), "SQLAlchemy")) {
            return "Implemented SQLAlchemy ORM models and database integrations for backend application logic";
        }
        if (containsAny(List.of(skill), "Cloud SQL", "GCP")) {
            return "Worked with Cloud SQL and GCP services to support cloud-hosted database and application workflows";
        }
        if (containsAny(List.of(skill), "Jenkins")) {
            return "Configured Jenkins pipelines to automate build, test, and deployment workflows";
        }
        if (containsAny(List.of(skill), "Jira")) {
            return "Tracked development tasks and sprint deliverables using Jira in Agile/Scrum environments";
        }
        return "Leveraged " + skill + " in professional development work to deliver reliable software solutions";
    }

    /**
     * Improves keyword coverage only for skills supported by the original resume or verified skills.
     */
    private String improveKeywordCoverageTruthfully(String generatedResume, String jobDescription,
                                                    String originalResume, String verifiedSkills,
                                                    List<String> missingKeywords) {
        String originalNorm = originalResume.toLowerCase();
        String verifiedNorm = verifiedSkills.toLowerCase();
        List<String> rawVerifiedTerms = parseRawVerifiedTerms(verifiedSkills);
        String generatedNorm = generatedResume.toLowerCase();

        List<String> recoverableKeywords = new ArrayList<>();
        for (String keyword : missingKeywords) {
            if (isSkillSupported(keyword, originalNorm, verifiedNorm, rawVerifiedTerms)
                && !matchesKeyword(generatedNorm, keyword)) {
                recoverableKeywords.add(keyword);
            }
        }

        if (recoverableKeywords.isEmpty()) {
            return generatedResume;
        }

        StringBuilder updated = new StringBuilder(generatedResume);
        String additions = String.join(", ", recoverableKeywords.subList(0, Math.min(5, recoverableKeywords.size())));

        int summaryEnd = updated.toString().toUpperCase().indexOf("TECHNICAL SKILLS");
        if (summaryEnd > 0) {
            updated.insert(summaryEnd, " Core strengths include " + additions + ".");
        }

        int expIdx = updated.toString().toUpperCase().indexOf("PROFESSIONAL EXPERIENCE");
        int projIdx = updated.toString().toUpperCase().indexOf("PROJECTS");
        int insertPoint = projIdx > 0 ? projIdx : updated.length();

        for (String keyword : recoverableKeywords) {
            if (!matchesKeyword(updated.toString().toLowerCase(), keyword)) {
                String bullet = buildTruthfulBulletForSkill(keyword);
                if (bullet != null && expIdx >= 0) {
                    updated.insert(insertPoint, "- " + bullet + "\n");
                }
            }
        }

        return aggressivelyPlaceKeywords(updated.toString(), recoverableKeywords, buildSupportedNorm(originalResume, verifiedSkills));
    }

    private String buildSkillGapAnalysis(AnalysisResult originalAnalysis, AnalysisResult generatedAnalysis,
                                         String originalResume, String verifiedSkills, String jobDescription) {
        String jdRole = originalAnalysis.getJdRole();
        String resumeRole = originalAnalysis.getResumeRole();
        List<String> missing = generatedAnalysis.getMissingKeywords();

        if (jdRole.equals("Python Developer") && !isPythonSupported(originalResume, verifiedSkills)) {
            return "This resume is currently stronger for " + resumeRole + " roles than Python Developer roles. "
                + "Missing core Python Developer requirements include Python, Python scripting, Django/Flask/FastAPI, and PyTest. "
                + "Add these only if the candidate has real experience.";
        }

        return generateSkillGapAnalysis(resumeRole, jdRole, missing, generatedAnalysis.getMatchedKeywords());
    }

    private String extractSummaryFromGenerated(String generatedResume) {
        if (generatedResume == null) return "";
        String[] lines = generatedResume.split("\\n");
        StringBuilder summary = new StringBuilder();
        boolean inSummary = false;
        for (String line : lines) {
            String trimmed = line.trim();
            String upper = trimmed.toUpperCase();
            if (upper.equals("PROFESSIONAL SUMMARY")) {
                inSummary = true;
                continue;
            }
            if (inSummary) {
                if (upper.equals("TECHNICAL SKILLS") || upper.equals("PROFESSIONAL EXPERIENCE")) break;
                if (!trimmed.isEmpty()) summary.append(trimmed).append(" ");
            }
        }
        return summary.toString().trim();
    }

    private List<String> extractBulletsFromGenerated(String generatedResume) {
        List<String> bullets = new ArrayList<>();
        if (generatedResume == null) return bullets;
        for (String line : generatedResume.split("\\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•")) {
                bullets.add(trimmed.replaceFirst("^[-*•]\\s*", ""));
            }
        }
        return bullets;
    }

    // ==========================================================================
    //  EXTRACT ALL RESUME SKILLS — Scan resume for every known skill
    // ==========================================================================

    /**
     * Scans the resume text for ALL known skills across all categories.
     * Returns every skill the candidate actually has, regardless of JD.
     */
    private List<String> extractAllResumeSkills(String resumeNorm) {
        Set<String> found = new LinkedHashSet<>();
        List<List<String>> allCategories = List.of(
            PROGRAMMING_LANGUAGES, BACKEND_FRAMEWORKS, FRONTEND_FRAMEWORKS,
            DATABASES, CLOUD_TOOLS, DEVOPS_TOOLS, TESTING_TOOLS, SOFT_SKILLS
        );
        for (List<String> category : allCategories) {
            for (String skill : category) {
                if (matchesKeyword(resumeNorm, skill)) {
                    found.add(skill);
                }
            }
        }
        return new ArrayList<>(found);
    }

    // ==========================================================================
    //  KEYWORD MATCHING — Word-boundary regex to avoid substring errors
    // ==========================================================================

    /**
     * Checks if a keyword appears in text using word-boundary-like matching.
     * This prevents false positives like "java" matching inside "javascript".
     */
    private boolean matchesKeyword(String text, String keyword) {
        String lowerKey = keyword.toLowerCase();
        if (matchesExact(text, lowerKey)) return true;
        // Check all aliases that map to this keyword
        for (Map.Entry<String, String> alias : ALIASES.entrySet()) {
            if (alias.getValue().equalsIgnoreCase(keyword)) {
                if (matchesExact(text, alias.getKey())) return true;
            }
        }
        return false;
    }

    /**
     * Performs word-boundary matching using lookaround regex assertions.
     * (?<![a-zA-Z0-9]) = not preceded by alphanumeric
     * (?![a-zA-Z0-9]) = not followed by alphanumeric
     */
    private boolean matchesExact(String text, String term) {
        if (term.equals("java")) {
            return Pattern.compile("(?<![a-zA-Z0-9])java(?![a-zA-Z0-9]|script)").matcher(text).find();
        }
        if (term.equals("sql")) {
            return Pattern.compile("(?<![a-zA-Z0-9])sql(?![a-zA-Z0-9])").matcher(text).find();
        }
        String escaped = Pattern.quote(term);
        String pattern = "(?<![a-zA-Z0-9])" + escaped + "(?![a-zA-Z0-9])";
        try {
            return Pattern.compile(pattern).matcher(text).find();
        } catch (Exception e) {
            return text.contains(term);
        }
    }

    // ==========================================================================
    //  KEYWORD EXTRACTION BY CATEGORY
    // ==========================================================================

    private List<String> extractCategoryKeywords(String normalizedText, List<String> categorySkills) {
        List<String> found = new ArrayList<>();
        for (String skill : categorySkills) {
            if (matchesKeyword(normalizedText, skill)) {
                if (!found.contains(skill)) found.add(skill);
            }
        }
        return found;
    }

    // ==========================================================================
    //  ROLE DETECTION
    // ==========================================================================

    private String detectRole(String normalizedText) {
        Map<String, Integer> roleScores = new LinkedHashMap<>();

        // Check role indicator phrases
        for (Map.Entry<String, List<String>> entry : ROLE_INDICATORS.entrySet()) {
            int score = 0;
            for (String indicator : entry.getValue()) {
                if (normalizedText.contains(indicator)) score += 3;
            }
            if (score > 0) roleScores.put(entry.getKey(), score);
        }

        // Boost based on skill distribution
        int frontendCount = countMatchedSkills(normalizedText, FRONTEND_FRAMEWORKS);
        int backendCount = countMatchedSkills(normalizedText, BACKEND_FRAMEWORKS);
        int langBackend = 0;
        for (String lang : List.of("Java", "Python", "C#", "Go", "Kotlin")) {
            if (matchesKeyword(normalizedText, lang)) langBackend++;
        }
        int langFrontend = 0;
        for (String lang : List.of("JavaScript", "TypeScript")) {
            if (matchesKeyword(normalizedText, lang)) langFrontend++;
        }
        int devopsCount = countMatchedSkills(normalizedText, DEVOPS_TOOLS) + countMatchedSkills(normalizedText, CLOUD_TOOLS);

        if (frontendCount >= 3 && (backendCount + langBackend) >= 3) {
            roleScores.merge("Full Stack Developer", 2, Integer::sum);
        } else if (frontendCount >= 3 || langFrontend >= 1 && frontendCount >= 2) {
            roleScores.merge("Frontend Developer", 2, Integer::sum);
        }
        if ((backendCount + langBackend) >= 3) {
            roleScores.merge("Backend Developer", 2, Integer::sum);
        }
        if (devopsCount >= 4) {
            roleScores.merge("DevOps Engineer", 2, Integer::sum);
        }

        // Python-specific detection
        if (matchesKeyword(normalizedText, "Python") &&
            (matchesKeyword(normalizedText, "Django") || matchesKeyword(normalizedText, "Flask") || matchesKeyword(normalizedText, "FastAPI"))) {
            roleScores.merge("Python Developer", 3, Integer::sum);
        }
        // Java-specific detection
        if (matchesKeyword(normalizedText, "Java") &&
            (matchesKeyword(normalizedText, "Spring Boot") || matchesKeyword(normalizedText, "Spring"))) {
            roleScores.merge("Java Developer", 3, Integer::sum);
        }

        return roleScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("General Developer");
    }

    // ==========================================================================
    //  ROLE RELATEDNESS
    // ==========================================================================

    private static int getRoleRelatedness(String role1, String role2) {
        if (role1 == null || role2 == null) return 40;
        if (role1.equals(role2)) return 100;
        if (role1.equals("General Developer") || role2.equals("General Developer")) return 50;

        // Build a pair key (alphabetically sorted)
        String a = role1.compareTo(role2) < 0 ? role1 : role2;
        String b = role1.compareTo(role2) < 0 ? role2 : role1;
        String pair = a + "|" + b;

        Map<String, Integer> relatedness = new HashMap<>();
        // Strong match (70) - same domain family
        relatedness.put("Frontend Developer|Full Stack Developer", 70);
        relatedness.put("Backend Developer|Full Stack Developer", 70);
        relatedness.put("Full Stack Developer|Java Developer", 70);
        relatedness.put("Full Stack Developer|Python Developer", 70);
        relatedness.put("Backend Developer|Java Developer", 70);
        relatedness.put("Backend Developer|Python Developer", 70);
        relatedness.put("Cloud Engineer|DevOps Engineer", 70);

        // Partial match (40) - adjacent domains
        relatedness.put("Backend Developer|Frontend Developer", 40);
        relatedness.put("Frontend Developer|Java Developer", 40);
        relatedness.put("Java Developer|Python Developer", 40);
        relatedness.put("Frontend Developer|Mobile Developer", 40);
        relatedness.put("Backend Developer|DevOps Engineer", 40);

        // Weak match (10) - different domains
        relatedness.put("Frontend Developer|Python Developer", 10);
        relatedness.put("Data Engineer|Frontend Developer", 10);
        relatedness.put("DevOps Engineer|Frontend Developer", 10);
        relatedness.put("Frontend Developer|QA Engineer", 10);

        Integer score = relatedness.get(pair);
        return score != null ? score : 10;
    }

    // ==========================================================================
    //  SCORE CALCULATION HELPERS
    // ==========================================================================

    private int calculateMatchPercentage(String resumeNorm, List<String> categoryKeywords) {
        if (categoryKeywords.isEmpty()) return 100;
        int matched = 0;
        for (String keyword : categoryKeywords) {
            if (matchesKeyword(resumeNorm, keyword)) matched++;
        }
        return (int) Math.round((matched / (double) categoryKeywords.size()) * 100);
    }

    private int countMatchedSkills(String text, List<String> skills) {
        int count = 0;
        for (String skill : skills) {
            if (matchesKeyword(text, skill)) count++;
        }
        return count;
    }

    // ==========================================================================
    //  EXPERIENCE RELEVANCE
    // ==========================================================================

    private int calculateExperienceRelevance(String resumeNorm, List<String> jdKeywords) {
        String expSection = extractSection(resumeNorm, "experience");
        String projSection = extractSection(resumeNorm, "project");
        String combined = expSection + " " + projSection;

        if (combined.trim().length() < 20) return 30;
        if (jdKeywords.isEmpty()) return 50;

        int matchCount = 0;
        for (String keyword : jdKeywords) {
            if (matchesKeyword(combined, keyword)) matchCount++;
        }
        return Math.min(100, (int) Math.round((matchCount / (double) jdKeywords.size()) * 100));
    }

    /**
     * Extracts lines between a section header and the next section header.
     * Looks for "experience" or "project" section patterns.
     */
    private String extractSection(String normalizedText, String sectionType) {
        String[] lines = normalizedText.split("\\n");
        StringBuilder section = new StringBuilder();
        boolean inSection = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) continue;

            // Check if this line is the target section header
            if (!inSection) {
                if (sectionType.equals("experience")) {
                    if (trimmed.matches("^(professional\\s+)?experience$") || trimmed.matches("^work\\s+experience$")) {
                        inSection = true;
                        continue;
                    }
                } else if (sectionType.equals("project")) {
                    if (trimmed.matches("^projects?$") || trimmed.matches("^personal\\s+projects?$")) {
                        inSection = true;
                        continue;
                    }
                } else if (sectionType.equals("skill")) {
                    if (trimmed.matches("^(technical\\s+)?skills?$") || trimmed.contains("technical skills")
                        || trimmed.startsWith("environment") || trimmed.equals("environment:")) {
                        inSection = true;
                        continue;
                    }
                } else if (sectionType.equals("environment")) {
                    if (trimmed.startsWith("environment") || trimmed.equals("environment:")) {
                        inSection = true;
                        continue;
                    }
                }
            } else {
                if (trimmed.matches("^(education|skills|technical\\s+skills|summary|professional\\s+summary|objective|certifications?|projects?|experience|work\\s+experience|personal\\s+projects?|environment)$")) {
                    break;
                }
                section.append(trimmed).append(" ");
            }
        }

        return section.toString();
    }

    // ==========================================================================
    //  SCORE LABELS
    // ==========================================================================

    private String getScoreLabel(int score) {
        if (score >= 90) return "Great Match";
        if (score >= 75) return "Good Match";
        if (score >= 60) return "Moderate Match";
        if (score >= 40) return "Weak Match";
        return "Poor Match";
    }

    // ==========================================================================
    //  ROLE MISMATCH WARNING
    // ==========================================================================

    private String generateRoleMismatchWarning(String resumeRole, String jdRole, int roleMatchScore) {
        if (roleMatchScore >= 70) return "";
        return "This resume appears to target a " + resumeRole + " role, while the job description targets a " + jdRole + " role. " +
               "The ATS score is reduced because the job description requires skills that are not clearly present in the resume.";
    }

    // ==========================================================================
    //  SKILL GAP ANALYSIS
    // ==========================================================================

    private String generateSkillGapAnalysis(String resumeRole, String jdRole, List<String> missingKeywords, List<String> matchedKeywords) {
        StringBuilder analysis = new StringBuilder();

        int roleRelatedness = getRoleRelatedness(resumeRole, jdRole);

        if (roleRelatedness < 70 && !resumeRole.equals(jdRole)) {
            analysis.append("This resume does not strongly match the ").append(jdRole).append(" role because the resume focuses on ").append(resumeRole.toLowerCase()).append(" development. ");
        } else if (matchedKeywords.isEmpty()) {
            analysis.append("The resume does not demonstrate clear alignment with the ").append(jdRole).append(" role. ");
        } else {
            analysis.append("The resume shows partial alignment with the ").append(jdRole).append(" role. ");
        }

        if (!missingKeywords.isEmpty()) {
            List<String> coreMissing = missingKeywords.subList(0, Math.min(5, missingKeywords.size()));
            analysis.append("Missing core requirements include ").append(String.join(", ", coreMissing)).append(". ");
        }

        analysis.append("Recommended truthful improvements: ");
        if (!missingKeywords.isEmpty()) {
            String firstMissing = missingKeywords.get(0);
            analysis.append("Add ").append(firstMissing).append(" only if you have real ").append(firstMissing).append(" experience. ");
            if (missingKeywords.size() > 1) {
                List<String> others = missingKeywords.subList(1, Math.min(3, missingKeywords.size()));
                analysis.append("Add ").append(String.join("/", others)).append(" only if used in a real project. ");
            }
            analysis.append("Complete a relevant project and then add it honestly.");
        } else {
            analysis.append("Continue highlighting your existing skills with concrete examples and metrics from real projects.");
        }

        return analysis.toString();
    }

    // ==========================================================================
    //  SUGGESTIONS
    // ==========================================================================

    private List<String> generateSuggestions(String resumeNorm, List<String> missingKeywords,
                                             List<String> matchedKeywords, int atsScore,
                                             int roleMatchScore, String resumeRole, String jdRole) {
        List<String> suggestions = new ArrayList<>();

        // Missing keywords suggestion
        if (!missingKeywords.isEmpty()) {
            suggestions.add("Add these missing keywords to your resume (only if you truly have these skills): "
                    + String.join(", ", missingKeywords));
        }

        // Score-based advice
        if (atsScore >= 80) {
            suggestions.add("Great job! Your resume is a strong match. Fine-tune it with the tips below");
        } else if (atsScore >= 50) {
            suggestions.add("Your resume is a decent match but needs improvement. Focus on adding the missing keywords");
        } else {
            suggestions.add("Your resume needs significant improvement for this role. Add the missing skills and tailor your experience");
        }

        // Role mismatch suggestion
        if (roleMatchScore < 70 && !resumeRole.equals(jdRole)) {
            suggestions.add("Your resume targets a " + resumeRole + " role but the job is for a " + jdRole + ". Consider tailoring your experience to highlight relevant transferable skills");
        }

        // Check for measurable achievements
        boolean hasMetrics = resumeNorm.contains("achieved") || resumeNorm.contains("increased")
                || resumeNorm.contains("reduced") || resumeNorm.contains("improved")
                || resumeNorm.contains("%") || resumeNorm.contains("percent");
        if (!hasMetrics) {
            suggestions.add("Add measurable achievements (e.g., 'Reduced API response time by 40%', 'Increased test coverage to 85%')");
        }

        // Check for action verbs
        boolean hasActionVerbs = resumeNorm.contains("led") || resumeNorm.contains("architected")
                || resumeNorm.contains("designed") || resumeNorm.contains("implemented")
                || resumeNorm.contains("spearheaded") || resumeNorm.contains("managed");
        if (!hasActionVerbs) {
            suggestions.add("Use stronger action verbs like 'Led', 'Architected', 'Designed', 'Implemented', 'Spearheaded'");
        }

        // Resume length
        if (resumeNorm.length() < 500) {
            suggestions.add("Your resume seems short. Add more details about projects, responsibilities, and impact");
        }

        // Portfolio/GitHub links
        if (!resumeNorm.contains("github") && !resumeNorm.contains("linkedin")
                && !resumeNorm.contains("portfolio") && !resumeNorm.contains("http")) {
            suggestions.add("Include links to your GitHub profile, LinkedIn, or portfolio website");
        }

        // Always-applicable tips
        suggestions.add("Tailor your professional summary to highlight skills matching this specific job description");
        suggestions.add("Use a clean, ATS-friendly format — avoid tables, images, and fancy formatting");

        return suggestions;
    }

    // ==========================================================================
    //  IMPROVED PROFESSIONAL SUMMARY — TRUTHFUL MODE
    // ==========================================================================

    /**
     * Generates an improved professional summary based ONLY on skills
     * the resume actually has. Uses the resume's detected role (not the JD role).
     */
    private String generateImprovedSummary(String resumeNorm, List<String> matchedKeywords,
                                            List<String> missingKeywords, String resumeRole, String jdRole,
                                            List<String> allResumeSkills) {
        StringBuilder summary = new StringBuilder();
        summary.append("Results-driven ").append(resumeRole);
        summary.append(" with hands-on experience in ");

        // Use ALL resume skills (not just JD-matched ones) for a truthful summary
        List<String> skillsToShow = allResumeSkills.isEmpty() ? matchedKeywords : allResumeSkills;
        List<String> topSkills = skillsToShow.subList(0, Math.min(6, skillsToShow.size()));
        if (topSkills.isEmpty()) {
            summary.append("software development");
        } else {
            summary.append(String.join(", ", topSkills));
        }
        summary.append(". ");

        // Domain-specific language based on RESUME role (not JD)
        if (resumeRole.contains("Frontend")) {
            summary.append("Experienced in building responsive user interfaces and accessible web applications. ");
        } else if (resumeRole.contains("Backend") || resumeRole.contains("Java") || resumeRole.contains("Python")) {
            summary.append("Experienced in building RESTful APIs, server-side architectures, and data-driven systems. ");
        } else if (resumeRole.contains("Full Stack")) {
            summary.append("Experienced in building RESTful APIs, microservices, and responsive user interfaces. ");
        } else if (resumeRole.contains("DevOps")) {
            summary.append("Experienced in automating infrastructure, CI/CD pipelines, and cloud deployments. ");
        } else {
            summary.append("Experienced in building reliable, production-ready software solutions. ");
        }

        summary.append("Strong collaborator with a passion for writing clean, well-tested code and delivering impactful solutions.");
        return summary.toString();
    }

    // ==========================================================================
    //  IMPROVED BULLET POINTS — TRUTHFUL MODE
    // ==========================================================================

    /**
     * Generates bullet points ONLY for skills the resume already has.
     * Does NOT generate bullets for missing skills or use fake metrics.
     */
    private List<String> generateImprovedBulletPoints(List<String> matchedKeywords, String resumeNorm) {
        List<String> bullets = new ArrayList<>();

        if (containsAny(matchedKeywords, "Java", "Spring Boot", "Spring")) {
            bullets.add("Developed and maintained RESTful APIs using Java and Spring Boot, ensuring high performance and reliability");
        }
        if (containsAny(matchedKeywords, "Microservices")) {
            bullets.add("Designed and implemented microservices architecture, improving system scalability and deployment flexibility");
        }
        if (containsAny(matchedKeywords, "React", "Angular", "Vue")) {
            bullets.add("Built responsive and accessible UI components using modern frontend frameworks, enhancing user experience across devices");
        }
        if (containsAny(matchedKeywords, "Docker", "Kubernetes")) {
            bullets.add("Containerized applications using Docker and orchestrated deployments with Kubernetes for consistent environments");
        }
        if (containsAny(matchedKeywords, "SQL", "PostgreSQL", "MySQL", "MongoDB")) {
            bullets.add("Designed and optimized database schemas and queries, improving data retrieval efficiency and reliability");
        }
        if (containsAny(matchedKeywords, "AWS", "Azure", "GCP")) {
            bullets.add("Deployed and managed cloud infrastructure to ensure application scalability and cost efficiency");
        }
        if (containsAny(matchedKeywords, "CI/CD", "Jenkins", "GitHub Actions")) {
            bullets.add("Implemented CI/CD pipelines to automate build, test, and deployment workflows");
        }
        if (containsAny(matchedKeywords, "JUnit", "PyTest", "Mockito", "Jest")) {
            bullets.add("Wrote comprehensive unit and integration tests to maintain code quality and prevent regressions");
        }
        if (containsAny(matchedKeywords, "Agile", "Scrum")) {
            bullets.add("Collaborated in Agile/Scrum teams, participating in sprint planning, code reviews, and retrospectives");
        }
        if (containsAny(matchedKeywords, "Kafka", "Redis")) {
            bullets.add("Implemented event-driven architecture for asynchronous processing and real-time data handling");
        }
        if (containsAny(matchedKeywords, "Python", "Django", "Flask", "FastAPI")) {
            bullets.add("Developed backend services and APIs using Python frameworks, focusing on clean architecture and maintainability");
        }
        if (containsAny(matchedKeywords, "JavaScript", "TypeScript", "HTML", "CSS")) {
            bullets.add("Developed interactive web applications using JavaScript/TypeScript with clean, maintainable code");
        }
        if (containsAny(matchedKeywords, "Git")) {
            bullets.add("Managed source code with Git, following branching strategies and conducting code reviews");
        }

        if (bullets.size() < 3) {
            bullets.add("Developed scalable features handling production traffic efficiently");
            bullets.add("Collaborated with cross-functional teams to gather requirements and deliver solutions");
        }
        return bullets;
    }

    // ==========================================================================
    //  GENERATED RESUME — TRUTHFUL MODE
    // ==========================================================================

    /**
     * Generates a complete ATS-friendly resume.
     * TRUTHFUL MODE rules:
     * - Technical Skills section ONLY lists skills from matchedKeywords
     * - Experience bullets are improved with stronger verbs but NO fake metrics
     * - Education section is COMPLETELY REMOVED
     */
    private String generateCompleteResume(String originalResume, String improvedSummary,
                                           List<String> matchedKeywords, List<String> missingKeywords,
                                           List<String> allResumeSkills) {
        StringBuilder resume = new StringBuilder();
        String[] lines = originalResume.split("\\n");

        // --- Extract contact information from the top of the resume ---
        String name = "";
        String email = "";
        String phone = "";
        String linkedin = "";
        String github = "";

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String lower = trimmed.toLowerCase();
            if (lower.contains("@") && lower.contains(".") && email.isEmpty()) {
                if (lower.startsWith("email")) {
                    email = trimmed.replaceFirst("(?i)email\\s*[:|-]?\\s*", "").trim();
                } else {
                    email = trimmed;
                }
            } else if ((lower.contains("phone") || lower.matches(".*\\d{3}.*\\d{3}.*\\d{4}.*")) && phone.isEmpty()) {
                if (lower.startsWith("phone")) {
                    phone = trimmed.replaceFirst("(?i)phone\\s*[:|-]?\\s*", "").trim();
                } else {
                    phone = trimmed;
                }
            } else if ((lower.contains("linkedin.com") || lower.startsWith("linkedin")) && linkedin.isEmpty()) {
                if (lower.startsWith("linkedin")) {
                    linkedin = trimmed.replaceFirst("(?i)linkedin\\s*[:|-]?\\s*", "").trim();
                } else {
                    linkedin = trimmed;
                }
            } else if ((lower.contains("github.com") || lower.startsWith("github")) && github.isEmpty()) {
                if (lower.startsWith("github")) {
                    github = trimmed.replaceFirst("(?i)github\\s*[:|-]?\\s*", "").trim();
                } else {
                    github = trimmed;
                }
            } else if (name.isEmpty() && !lower.startsWith("-") && !lower.startsWith("*")
                        && !lower.contains("summary") && !lower.contains("experience")
                        && !lower.contains("skill") && !lower.contains("project")
                        && !lower.contains("education") && !lower.contains("@")
                        && !lower.matches(".*\\d{3}.*\\d{3}.*\\d{4}.*")
                        && trimmed.length() > 1 && trimmed.length() < 60) {
                name = trimmed;
            }

            // Stop parsing contact info once we hit a section header
            if (lower.contains("summary") || lower.contains("experience")
                || lower.contains("skill") || lower.contains("objective")) {
                break;
            }
        }

        // --- Build contact header ---
        if (!name.isEmpty()) resume.append(name).append("\n");
        if (!email.isEmpty()) resume.append("Email: ").append(email).append("\n");
        if (!phone.isEmpty()) resume.append("Phone: ").append(phone).append("\n");
        if (!linkedin.isEmpty()) resume.append("LinkedIn: ").append(linkedin).append("\n");
        if (!github.isEmpty()) resume.append("GitHub: ").append(github).append("\n");

        // --- Professional Summary (truthful — based on resume skills) ---
        resume.append("\n");
        resume.append("PROFESSIONAL SUMMARY\n");
        resume.append(improvedSummary).append("\n");

        // --- Technical Skills (ONLY matched keywords in TRUTHFUL MODE) ---
        resume.append("\n");
        resume.append("TECHNICAL SKILLS\n");

        if (TRUTHFUL_MODE) {
            // List ALL skills the resume actually has (not just JD-matched ones)
            List<String> langs = new ArrayList<>();
            List<String> backend = new ArrayList<>();
            List<String> frontend = new ArrayList<>();
            List<String> databases = new ArrayList<>();
            List<String> cloud = new ArrayList<>();
            List<String> devops = new ArrayList<>();
            List<String> testing = new ArrayList<>();
            List<String> soft = new ArrayList<>();
            List<String> other = new ArrayList<>();

            for (String skill : allResumeSkills) {
                String category = getCategory(skill);
                switch (category) {
                    case "lang": langs.add(skill); break;
                    case "backend": backend.add(skill); break;
                    case "frontend": frontend.add(skill); break;
                    case "database": databases.add(skill); break;
                    case "cloud": cloud.add(skill); break;
                    case "devops": devops.add(skill); break;
                    case "testing": testing.add(skill); break;
                    case "soft": soft.add(skill); break;
                    default: other.add(skill); break;
                }
            }

            if (!langs.isEmpty()) resume.append("Programming Languages: ").append(String.join(", ", langs)).append("\n");
            if (!backend.isEmpty()) resume.append("Backend: ").append(String.join(", ", backend)).append("\n");
            if (!frontend.isEmpty()) resume.append("Frontend: ").append(String.join(", ", frontend)).append("\n");
            if (!databases.isEmpty()) resume.append("Database: ").append(String.join(", ", databases)).append("\n");
            if (!cloud.isEmpty()) resume.append("Cloud: ").append(String.join(", ", cloud)).append("\n");
            if (!devops.isEmpty()) resume.append("DevOps Tools: ").append(String.join(", ", devops)).append("\n");
            if (!testing.isEmpty()) resume.append("Testing: ").append(String.join(", ", testing)).append("\n");
            if (!soft.isEmpty()) resume.append("Practices: ").append(String.join(", ", soft)).append("\n");
            if (!other.isEmpty()) resume.append("Other: ").append(String.join(", ", other)).append("\n");
        }

        // --- Professional Experience (parsed from original, improved wording) ---
        resume.append("\n");
        resume.append("PROFESSIONAL EXPERIENCE\n\n");

        boolean inExperience = false;
        boolean inProjects = false;
        boolean inEducation = false;
        boolean inSkills = false;
        StringBuilder experienceBlock = new StringBuilder();
        StringBuilder projectsBlock = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            String lower = trimmed.toLowerCase();

            // Detect section headers
            if (lower.matches("^(professional\\s+)?experience.*") || lower.equals("work experience")) {
                inExperience = true; inProjects = false; inEducation = false; inSkills = false;
                continue;
            }
            if (lower.startsWith("project")) {
                inProjects = true; inExperience = false; inEducation = false; inSkills = false;
                continue;
            }
            if (lower.contains("education") || lower.contains("bachelor") || lower.contains("master")
                || lower.contains("university") || lower.contains("graduation") || lower.contains("degree")) {
                inEducation = true; inExperience = false; inProjects = false; inSkills = false;
                continue;
            }
            if (lower.contains("skill") || lower.contains("technical")) {
                inSkills = true; inExperience = false; inProjects = false; inEducation = false;
                continue;
            }
            if (lower.contains("summary") || lower.contains("objective") || lower.contains("certification")) {
                inExperience = false; inProjects = false; inEducation = false; inSkills = false;
                continue;
            }

            // Skip education and skills lines entirely
            if (inEducation) continue;
            if (inSkills) continue;

            // Collect experience lines
            if (inExperience && !trimmed.isEmpty()) {
                if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•")) {
                    String bullet = trimmed.replaceFirst("^[-*•]\\s*", "");
                    bullet = improveBulletWording(bullet);
                    experienceBlock.append("- ").append(bullet).append("\n");
                } else {
                    experienceBlock.append(trimmed).append("\n");
                }
            }

            // Collect project lines
            if (inProjects && !trimmed.isEmpty()) {
                if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•")) {
                    String bullet = trimmed.replaceFirst("^[-*•]\\s*", "");
                    bullet = improveBulletWording(bullet);
                    projectsBlock.append("- ").append(bullet).append("\n");
                } else {
                    projectsBlock.append(trimmed).append("\n");
                }
            }
        }

        if (experienceBlock.length() > 0) {
            resume.append(experienceBlock);
        }

        // --- Projects Section ---
        if (projectsBlock.length() > 0) {
            resume.append("\n");
            resume.append("PROJECTS\n\n");
            resume.append(projectsBlock);
        }

        // --- Final cleanup: strip any Education remnants ---
        String finalResume = resume.toString();
        finalResume = removeEducationSection(finalResume);

        return finalResume.trim();
    }

    // ==========================================================================
    //  HELPER: Get category for a skill
    // ==========================================================================

    private String getCategory(String skill) {
        if (PROGRAMMING_LANGUAGES.contains(skill)) return "lang";
        if (BACKEND_FRAMEWORKS.contains(skill)) return "backend";
        if (FRONTEND_FRAMEWORKS.contains(skill)) return "frontend";
        if (DATABASES.contains(skill)) return "database";
        if (CLOUD_TOOLS.contains(skill)) return "cloud";
        if (DEVOPS_TOOLS.contains(skill)) return "devops";
        if (TESTING_TOOLS.contains(skill)) return "testing";
        if (SOFT_SKILLS.contains(skill)) return "soft";
        return "other";
    }

    // ==========================================================================
    //  HELPER: Improve bullet wording (stronger verbs, NO fake metrics)
    // ==========================================================================

    private String improveBulletWording(String bullet) {
        if (!bullet.isEmpty()) {
            bullet = bullet.substring(0, 1).toUpperCase() + bullet.substring(1);
        }

        if (bullet.toLowerCase().startsWith("worked on ")) {
            bullet = "Developed and delivered " + bullet.substring(10);
        } else if (bullet.toLowerCase().startsWith("helped ")) {
            bullet = "Collaborated to " + bullet.substring(7);
        } else if (bullet.toLowerCase().startsWith("did ")) {
            bullet = "Executed " + bullet.substring(4);
        } else if (bullet.toLowerCase().startsWith("made ")) {
            bullet = "Engineered " + bullet.substring(5);
        } else if (bullet.toLowerCase().startsWith("used ")) {
            bullet = "Leveraged " + bullet.substring(5);
        } else if (bullet.toLowerCase().startsWith("responsible for ")) {
            bullet = "Spearheaded " + bullet.substring(16);
        } else if (bullet.toLowerCase().startsWith("created ")) {
            bullet = "Designed and implemented " + bullet.substring(8);
        } else if (bullet.toLowerCase().startsWith("wrote ")) {
            bullet = "Developed " + bullet.substring(6);
        } else if (bullet.toLowerCase().startsWith("fixed ")) {
            bullet = "Diagnosed and resolved " + bullet.substring(6);
        } else if (bullet.toLowerCase().startsWith("tested ")) {
            bullet = "Validated and tested " + bullet.substring(7);
        } else if (bullet.toLowerCase().startsWith("built ")) {
            bullet = "Architected and built " + bullet.substring(6);
        }

        return bullet;
    }

    // ==========================================================================
    //  HELPER: Remove education section from generated resume
    // ==========================================================================

    private String removeEducationSection(String resumeText) {
        String[] lines = resumeText.split("\\n");
        StringBuilder cleaned = new StringBuilder();
        boolean skipping = false;

        for (String line : lines) {
            String lower = line.trim().toLowerCase();

            if (lower.matches("^education.*") || lower.equals("education")) {
                skipping = true;
                continue;
            }

            // Stop skipping when we hit the next section header
            if (skipping && !lower.isEmpty() && line.trim().matches("^[A-Z][A-Z ]{2,}$")
                && !lower.contains("education")) {
                skipping = false;
            }

            if (!skipping) {
                if (lower.contains("bachelor") || lower.contains("master's degree")
                    || lower.contains("master") || lower.contains("university")
                    || lower.contains("graduation") || lower.contains("bachelor's degree")
                    || lower.contains("college") || lower.contains("degree in")) {
                    continue;
                }
                cleaned.append(line).append("\n");
            }
        }

        return cleaned.toString();
    }

    // ==========================================================================
    //  DATABASE: Save Analysis
    // ==========================================================================

    private void saveToDatabase(ResumeAnalyzeRequest request, ResumeAnalyzeResponse response) {
        ResumeAnalysis entity = new ResumeAnalysis();

        entity.setResumeText(request.getResumeText());
        entity.setJobDescription(request.getJobDescription());

        entity.setAtsScore(response.getAtsScore());
        entity.setOriginalAtsScore(response.getOriginalAtsScore());
        entity.setGeneratedResumeAtsScore(null);
        entity.setScoreLabel(response.getScoreLabel());
        entity.setOptimizationStatus(response.getOptimizationStatus() != null ? response.getOptimizationStatus() : "");
        entity.setRoleMatchScore(response.getRoleMatchScore());
        entity.setRequiredSkillScore(response.getRequiredSkillScore());
        entity.setMatchedKeywords(ResumeAnalysis.listToString(response.getMatchedKeywords()));
        entity.setMissingKeywords(ResumeAnalysis.listToString(response.getMissingKeywords()));
        entity.setRoleMismatchWarning(response.getRoleMismatchWarning());
        entity.setSkillGapAnalysis(response.getSkillGapAnalysis());
        entity.setSuggestions(ResumeAnalysis.listToString(response.getSuggestions()));
        entity.setImprovedProfessionalSummary(response.getImprovedProfessionalSummary());
        entity.setImprovedBulletPoints(ResumeAnalysis.listToString(response.getImprovedBulletPoints()));
        entity.setGeneratedResume(response.getGeneratedResume());
        entity.setVerifiedAdditionalSkills(request.getVerifiedAdditionalSkills() != null ? request.getVerifiedAdditionalSkills() : "");

        resumeAnalysisRepository.save(entity);
    }

    // ==========================================================================
    //  DATABASE: Get History
    // ==========================================================================

    public List<Map<String, Object>> getHistory() {
        List<ResumeAnalysis> records = resumeAnalysisRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> history = new ArrayList<>();

        for (ResumeAnalysis record : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", record.getId());
            item.put("atsScore", record.getAtsScore());
            item.put("originalAtsScore", record.getOriginalAtsScore());
            item.put("generatedResumeAtsScore", record.getGeneratedResumeAtsScore());
            item.put("scoreLabel", record.getScoreLabel() != null ? record.getScoreLabel() : "");
            item.put("optimizationStatus", record.getOptimizationStatus() != null ? record.getOptimizationStatus() : "");
            item.put("roleMatchScore", record.getRoleMatchScore());
            item.put("requiredSkillScore", record.getRequiredSkillScore());
            item.put("matchedKeywords", ResumeAnalysis.stringToList(record.getMatchedKeywords()));
            item.put("missingKeywords", ResumeAnalysis.stringToList(record.getMissingKeywords()));
            item.put("roleMismatchWarning", record.getRoleMismatchWarning() != null ? record.getRoleMismatchWarning() : "");
            item.put("skillGapAnalysis", record.getSkillGapAnalysis() != null ? record.getSkillGapAnalysis() : "");
            item.put("suggestions", ResumeAnalysis.stringToList(record.getSuggestions()));
            item.put("improvedProfessionalSummary", record.getImprovedProfessionalSummary() != null ? record.getImprovedProfessionalSummary() : "");
            item.put("improvedBulletPoints", ResumeAnalysis.stringToList(record.getImprovedBulletPoints()));
            item.put("generatedResume", record.getGeneratedResume() != null ? record.getGeneratedResume() : "");
            item.put("verifiedAdditionalSkills", record.getVerifiedAdditionalSkills() != null ? record.getVerifiedAdditionalSkills() : "");
            item.put("createdAt", record.getCreatedAt() != null ? record.getCreatedAt().toString() : "");
            history.add(item);
        }

        return history;
    }

    // ==========================================================================
    //  HELPER: containsAny
    // ==========================================================================

    private boolean containsAny(List<String> keywords, String... values) {
        for (String value : values) {
            if (keywords.contains(value)) return true;
        }
        return false;
    }
}
