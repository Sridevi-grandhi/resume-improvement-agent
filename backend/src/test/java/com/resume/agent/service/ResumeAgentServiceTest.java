package com.resume.agent.service;

import com.resume.agent.dto.AnalysisResult;
import com.resume.agent.dto.ResumeAnalyzeRequest;
import com.resume.agent.dto.ResumeAnalyzeResponse;
import com.resume.agent.repository.ResumeAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeAgentServiceTest {

    @Mock
    private ResumeAnalysisRepository resumeAnalysisRepository;

    private ResumeAgentService service;

    @BeforeEach
    void setUp() {
        service = new ResumeAgentService(resumeAnalysisRepository);
    }

    private static final String JAVA_FULL_STACK_RESUME = """
            John Smith
            Email: john.smith@email.com
            Phone: 555-123-4567

            PROFESSIONAL SUMMARY
            Java Full Stack Developer with experience in Spring Boot, React, and PostgreSQL.

            TECHNICAL SKILLS
            Java, Spring Boot, REST API, Microservices, React, JavaScript, TypeScript, PostgreSQL, AWS, Docker, Kubernetes, Jenkins, Git, JUnit, Agile, Scrum

            PROFESSIONAL EXPERIENCE
            Senior Java Full Stack Developer | Tech Corp | 2020 - Present
            - Developed REST APIs using Java and Spring Boot
            - Built React frontend applications with TypeScript
            - Deployed microservices on AWS using Docker and Kubernetes
            - Wrote JUnit tests and participated in Agile/Scrum ceremonies

            PROJECTS
            E-Commerce Platform
            - Built full stack application with Spring Boot, React, and PostgreSQL

            EDUCATION
            Bachelor's Degree in Computer Science
            State University, 2018
            """;

    private static final String JAVA_FULL_STACK_JD = """
            Java Full Stack Developer

            Required Skills:
            Java, Spring Boot, REST API, Microservices, React, JavaScript, TypeScript,
            PostgreSQL, AWS, Docker, Kubernetes, Jenkins, Git, JUnit, Agile, Scrum

            Responsibilities:
            - Build and maintain full stack applications
            - Design REST APIs and microservices
            - Deploy to AWS using Docker and Kubernetes
            """;

    private static final String FRONTEND_RESUME = """
            Jane Doe
            Email: jane@email.com

            PROFESSIONAL SUMMARY
            Frontend Developer specializing in React and modern JavaScript.

            TECHNICAL SKILLS
            React, JavaScript, TypeScript, HTML, CSS, Redux, Git, Agile

            PROFESSIONAL EXPERIENCE
            Frontend Developer | WebCo | 2021 - Present
            - Built responsive React applications
            - Implemented Redux state management
            - Collaborated in Agile teams

            EDUCATION
            Bachelor's in Information Technology
            """;

    private static final String PYTHON_JD = """
            Python Developer

            Required Skills:
            Python, Django, Flask, FastAPI, PyTest, REST API, PostgreSQL, Docker, Git, Agile

            Responsibilities:
            - Develop backend services using Python frameworks
            - Write PyTest unit tests
            - Build REST APIs with Django or Flask
            """;

    private static final String REACT_RESUME = """
            Alex Johnson
            Email: alex@email.com

            PROFESSIONAL SUMMARY
            React Developer with strong frontend expertise.

            TECHNICAL SKILLS
            React, JavaScript, TypeScript, HTML, CSS, Redux, Next.js, Git, Jest, Agile

            PROFESSIONAL EXPERIENCE
            React Developer | StartupXYZ | 2020 - Present
            - Developed React components and SPAs
            - Used TypeScript and Redux for state management
            - Wrote Jest tests for frontend code

            PROJECTS
            Portfolio App
            - Built with React, TypeScript, and Next.js
            """;

    private static final String FRONTEND_JD = """
            Frontend Developer

            Required Skills:
            React, JavaScript, TypeScript, HTML, CSS, Redux, Git, Jest, Agile

            Responsibilities:
            - Build responsive web applications
            - Develop reusable UI components
            - Write frontend unit tests
            """;

    private static final String JAVA_DEV_RESUME = """
            Mike Brown
            Email: mike@email.com

            TECHNICAL SKILLS
            Java, Spring Boot, Hibernate, MySQL, Git, JUnit

            PROFESSIONAL EXPERIENCE
            Java Developer | Enterprise Inc | 2019 - Present
            - Developed Java applications with Spring Boot
            - Created REST APIs and database integrations
            """;

    private static final String VERIFIED_PYTHON_SKILLS =
        "Python, PostgreSQL, PL/SQL, automation scripts, REST API, Agile, Scrum, Git, Jira";

    private static final String PYTHON_RICH_RESUME = """
            Alex Developer
            Email: alex@email.com

            PROFESSIONAL SUMMARY
            Backend developer with Python and database experience.

            Environment:
            Python 3.x, FastAPI, Flask, Django, PostgreSQL, REST APIs, SQLAlchemy, MySQL, Cloud SQL, GCP, Docker, Git, Jenkins, Jira, Agile Scrum

            PROFESSIONAL EXPERIENCE
            Software Developer | DataCorp | 2019 - Present
            - Developed Python 3.x automation scripts for database workflows
            - Built REST APIs using FastAPI and Flask
            - Worked with PostgreSQL, MySQL, and SQLAlchemy for backend data logic
            - Deployed services using Docker, GCP, and Cloud SQL
            - Used Git, Jenkins, and Jira in Agile Scrum teams

            EDUCATION
            Bachelor's Degree in Computer Science
            """;

    private static final String PYTHON_DB_JD = """
            Python Developer

            Required Skills:
            Python, FastAPI, Flask, Django, PostgreSQL, REST API, SQLAlchemy, MySQL, Cloud SQL, GCP, Docker, Git, Jenkins, Jira, Agile, Scrum

            Responsibilities:
            - Develop Python backend services and automation scripts
            - Build REST APIs and database-driven applications
            - Work with PostgreSQL, SQL queries, and cloud database platforms
            - Support CI/CD with Jenkins and Agile/Scrum delivery
            """;

    @Test
    void testCaseC_javaFullStack_shouldAnalyzeInputAndGenerateOptimizedResume() {
        ResumeAnalyzeResponse response = analyze(JAVA_FULL_STACK_RESUME, JAVA_FULL_STACK_JD, null);

        assertEquals(0, response.getGeneratedResumeAtsScore(),
            "Main analyze should not return generated resume ATS score");
        assertEquals(response.getOriginalAtsScore(), response.getAtsScore(),
            "Primary atsScore should reflect the resume currently in the input box");
        assertTrue(response.getAtsScore() > 0,
            "Input resume should receive a valid ATS score");

        int generatedScore = scoreGeneratedResume(response.getGeneratedResume(), JAVA_FULL_STACK_JD);
        assertTrue(generatedScore >= 95,
            "Generated resume content should reach 95+ when skills match JD");
        assertFalse(response.getGeneratedResume().toLowerCase().contains("education"),
            "Education section must be removed");
        assertFalse(response.getGeneratedResume().toLowerCase().contains("bachelor"),
            "Degree references must be removed");
        assertTrue(response.getGeneratedResume().contains("Spring Boot"),
            "Generated resume should include JD keywords");
        assertEquals("", response.getOptimizationStatus());
    }

    @Test
    void testCaseA_frontendVsPython_noVerified_shouldNotForce95() {
        ResumeAnalyzeResponse response = analyze(FRONTEND_RESUME, PYTHON_JD, null);

        assertEquals(0, response.getGeneratedResumeAtsScore(),
            "Main analyze should not return generated resume ATS score");
        assertTrue(response.getAtsScore() < 95,
            "Frontend vs Python JD should not reach 95 for input resume");
        assertTrue(response.getAtsScore() <= 65 || response.getMissingKeywords().stream().anyMatch(k -> k.equalsIgnoreCase("Python")),
            "Score should remain modest without Python support");
        assertTrue(response.getMissingKeywords().stream().anyMatch(k -> k.equalsIgnoreCase("Python")),
            "Python should be in missing keywords");
        assertFalse(response.getGeneratedResume().toLowerCase().contains("django"),
            "Should not fake Django experience");
        assertFalse(response.getGeneratedResume().toLowerCase().contains("flask"),
            "Should not fake Flask experience");
        assertTrue(response.getSkillGapAnalysis().toLowerCase().contains("python"),
            "Skill gap should explain Python mismatch");
    }

    @Test
    void testCaseB_frontendVsPython_withVerified_shouldIncreaseGeneratedQuality() {
        ResumeAnalyzeResponse withoutVerified = analyze(FRONTEND_RESUME, PYTHON_JD, null);
        ResumeAnalyzeResponse response = analyze(FRONTEND_RESUME, PYTHON_JD, VERIFIED_PYTHON_SKILLS);

        assertEquals(0, response.getGeneratedResumeAtsScore());

        int withoutVerifiedGeneratedScore = scoreGeneratedResume(withoutVerified.getGeneratedResume(), PYTHON_JD);
        int withVerifiedGeneratedScore = scoreGeneratedResume(response.getGeneratedResume(), PYTHON_JD);

        assertTrue(withVerifiedGeneratedScore > withoutVerifiedGeneratedScore,
            "Verified skills should improve generated resume quality over the no-verified run");
        assertTrue(response.getGeneratedResume().toLowerCase().contains("python"),
            "Generated resume should include Python when verified");
        assertTrue(response.getGeneratedResume().contains("PostgreSQL"),
            "Generated resume should include verified PostgreSQL");
        assertTrue(withVerifiedGeneratedScore >= 55,
            "Verified Python skills should materially improve generated resume alignment");
    }

    @Test
    void test3_reactVsFrontend_shouldScoreHighForInputResume() {
        ResumeAnalyzeResponse response = analyze(REACT_RESUME, FRONTEND_JD, null);

        assertEquals(0, response.getGeneratedResumeAtsScore());
        assertTrue(response.getAtsScore() >= 90,
            "React Developer vs Frontend JD should score 90+ for input resume");
        assertTrue(response.getGeneratedResume().contains("React"),
            "Generated resume should be tailored to frontend JD");
        assertTrue(response.getMatchedKeywords().contains("React"));
    }

    @Test
    void test4_javaVsPython_shouldNotFakePython() {
        ResumeAnalyzeResponse response = analyze(JAVA_DEV_RESUME, PYTHON_JD, null);

        assertEquals(0, response.getGeneratedResumeAtsScore());
        assertTrue(response.getAtsScore() < 95,
            "Java Developer vs Python JD should stay below 95 for input resume");
        assertFalse(response.getGeneratedResume().toLowerCase().contains("django"),
            "Should not add fake Python/Django experience");
        assertTrue(response.getMissingKeywords().stream().anyMatch(k -> k.equalsIgnoreCase("Python")),
            "Python should remain in missing keywords");
    }

    @Test
    void analyzeResumeAgainstJD_returnsExpectedFields() {
        AnalysisResult result = service.analyzeResumeAgainstJD(JAVA_FULL_STACK_RESUME, JAVA_FULL_STACK_JD);

        assertNotNull(result.getScoreLabel());
        assertFalse(result.getMatchedKeywords().isEmpty());
        assertTrue(result.getAtsScore() > 0);
        assertTrue(result.getRoleMatchScore() > 0);
        assertTrue(result.getRequiredSkillScore() > 0);
    }

    @Test
    void pythonRichResume_shouldGenerateHighQualityResumeWithoutAutoScoring() {
        ResumeAnalyzeResponse response = analyze(PYTHON_RICH_RESUME, PYTHON_DB_JD, null);

        assertEquals(0, response.getGeneratedResumeAtsScore(),
            "Main analyze should not auto-score generated resume");

        int generatedScore = scoreGeneratedResume(response.getGeneratedResume(), PYTHON_DB_JD);
        assertTrue(generatedScore >= response.getOriginalAtsScore(),
            "Generated resume quality should not be lower than original when skills are supported");
        assertTrue(generatedScore >= 90,
            "Python-rich generated resume should target 90+ ATS for matching Python JD");
        assertTrue(response.getGeneratedResume().toLowerCase().contains("python"),
            "Generated resume should preserve Python");
        assertTrue(response.getGeneratedResume().contains("FastAPI") || response.getGeneratedResume().contains("Flask"),
            "Generated resume should preserve Python frameworks");
        assertFalse(response.getGeneratedResume().toLowerCase().contains("education"),
            "Education must be removed");
    }

    @Test
    void mainAnalyzeReturnsInputScoreOnly_notGeneratedScore() {
        ResumeAnalyzeResponse response = analyze(JAVA_FULL_STACK_RESUME, JAVA_FULL_STACK_JD, null);

        assertNotEquals(0, response.getOriginalAtsScore());
        assertEquals(0, response.getGeneratedResumeAtsScore(),
            "Generated resume ATS score should not be calculated on first analyze");
        assertEquals(response.getOriginalAtsScore(), response.getAtsScore(),
            "Primary atsScore should be the current input resume score");
        assertEquals("", response.getOptimizationStatus());
    }

    private int scoreGeneratedResume(String generatedResume, String jd) {
        return service.analyzeResumeAgainstJD(generatedResume, jd).getAtsScore();
    }

    private ResumeAnalyzeResponse analyze(String resume, String jd, String verifiedSkills) {
        when(resumeAnalysisRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ResumeAnalyzeRequest request = new ResumeAnalyzeRequest();
        request.setResumeText(resume);
        request.setJobDescription(jd);
        request.setVerifiedAdditionalSkills(verifiedSkills);
        return service.analyzeResume(request);
    }
}
