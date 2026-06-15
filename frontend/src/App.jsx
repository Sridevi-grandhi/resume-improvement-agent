import { useState } from 'react';
import { API_BASE_URL } from './config';

function App() {
  const [resumeText, setResumeText] = useState('');
  const [jobDescription, setJobDescription] = useState('');
  const [verifiedAdditionalSkills, setVerifiedAdditionalSkills] = useState('');
  const [atsScore, setAtsScore] = useState(null);
  const [scoreLabel, setScoreLabel] = useState('');
  const [roleMatchScore, setRoleMatchScore] = useState(null);
  const [requiredSkillScore, setRequiredSkillScore] = useState(null);
  const [matchedKeywords, setMatchedKeywords] = useState([]);
  const [missingKeywords, setMissingKeywords] = useState([]);
  const [roleMismatchWarning, setRoleMismatchWarning] = useState('');
  const [skillGapAnalysis, setSkillGapAnalysis] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [improvedProfessionalSummary, setImprovedProfessionalSummary] = useState('');
  const [improvedBulletPoints, setImprovedBulletPoints] = useState([]);
  const [generatedResume, setGeneratedResume] = useState('');
  const [generatedResumeAtsScore, setGeneratedResumeAtsScore] = useState(null);
  const [showGeneratedResumeScore, setShowGeneratedResumeScore] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [history, setHistory] = useState([]);
  const [showHistory, setShowHistory] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [copySuccess, setCopySuccess] = useState('');

  const hasResults = atsScore !== null;

  const handleAnalyze = async () => {
    if (!resumeText.trim() || !jobDescription.trim()) {
      setError('Please paste both resume and job description before analyzing.');
      return;
    }

    setLoading(true);
    setError('');
    setCopySuccess('');

    try {
      const response = await fetch(`${API_BASE_URL}/api/resume/analyze`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ resumeText, jobDescription, verifiedAdditionalSkills }),
      });

      if (!response.ok) {
        throw new Error('Backend returned an error. Status: ' + response.status);
      }

      const data = await response.json();
      setAtsScore(data.atsScore ?? data.originalAtsScore);
      setScoreLabel(data.scoreLabel || '');
      setRoleMatchScore(data.roleMatchScore ?? null);
      setRequiredSkillScore(data.requiredSkillScore ?? null);
      setMatchedKeywords(data.matchedKeywords || []);
      setMissingKeywords(data.missingKeywords || []);
      setRoleMismatchWarning(data.roleMismatchWarning || '');
      setSkillGapAnalysis(data.skillGapAnalysis || '');
      setSuggestions(data.suggestions || []);
      setImprovedProfessionalSummary(data.improvedProfessionalSummary || '');
      setImprovedBulletPoints(data.improvedBulletPoints || []);
      setGeneratedResume(data.generatedResume || '');
      setGeneratedResumeAtsScore(null);
      setShowGeneratedResumeScore(false);
    } catch (err) {
      setError(
        `Could not connect to backend. Make sure the Spring Boot server is reachable at ${API_BASE_URL}`
      );
      console.error('API Error:', err);
    } finally {
      setLoading(false);
    }
  };

  const clearResults = () => {
    setAtsScore(null);
    setScoreLabel('');
    setRoleMatchScore(null);
    setRequiredSkillScore(null);
    setMatchedKeywords([]);
    setMissingKeywords([]);
    setRoleMismatchWarning('');
    setSkillGapAnalysis('');
    setSuggestions([]);
    setImprovedProfessionalSummary('');
    setImprovedBulletPoints([]);
    setGeneratedResume('');
    setGeneratedResumeAtsScore(null);
    setShowGeneratedResumeScore(false);
  };

  const handleClear = () => {
    setResumeText('');
    setJobDescription('');
    setVerifiedAdditionalSkills('');
    clearResults();
    setError('');
    setCopySuccess('');
    setShowHistory(false);
    setHistory([]);
  };

  const handleViewHistory = async () => {
    if (showHistory) {
      setShowHistory(false);
      return;
    }

    setHistoryLoading(true);
    setError('');

    try {
      const response = await fetch(`${API_BASE_URL}/api/resume/history`);
      if (!response.ok) throw new Error('Failed to fetch history.');
      const data = await response.json();
      setHistory(data);
      setShowHistory(true);
    } catch (err) {
      setError('Could not fetch history. Make sure the backend is running.');
      console.error('History Error:', err);
    } finally {
      setHistoryLoading(false);
    }
  };

  async function copyGeneratedResume() {
    if (!generatedResume) {
      alert('No generated resume available to copy.');
      return;
    }

    try {
      await navigator.clipboard.writeText(generatedResume);
      alert('Generated resume copied. Paste it into the Your Resume box and click Analyze Resume again.');
    } catch (err) {
      alert('Failed to copy. Please select and copy manually.');
      console.error('Copy failed:', err);
    }
  }

  function useGeneratedResumeAsInput() {
    if (!generatedResume) {
      alert('No generated resume available to use as input.');
      return;
    }

    setResumeText(generatedResume);
    clearResults();
    setCopySuccess('Generated resume placed in Your Resume box. Click Analyze Resume to check ATS score.');
    setTimeout(() => setCopySuccess(''), 5000);
  }

  const handleDownloadResume = (text) => {
    const blob = new Blob([text], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'improved-resume.txt';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  const getScoreClass = (score) => {
    if (score >= 90) return 'score-great';
    if (score >= 75) return 'score-good';
    if (score >= 60) return 'score-moderate';
    if (score >= 40) return 'score-weak';
    return 'score-poor';
  };

  const getScoreLabel = (score) => {
    if (score >= 90) return 'Great Match';
    if (score >= 75) return 'Good Match';
    if (score >= 60) return 'Moderate Match';
    if (score >= 40) return 'Weak Match';
    return 'Poor Match';
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString();
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>Resume Improvement Agent</h1>
        <p>Paste your resume and a job description to get AI-powered ATS feedback</p>
      </header>

      <div className="input-section">
        <div className="input-card">
          <label htmlFor="resume">Your Resume</label>
          <textarea
            id="resume"
            placeholder="Paste your resume text here..."
            value={resumeText}
            onChange={(e) => setResumeText(e.target.value)}
            rows={12}
          />
        </div>
        <div className="input-card">
          <label htmlFor="jobDesc">Job Description</label>
          <textarea
            id="jobDesc"
            placeholder="Paste the job description here..."
            value={jobDescription}
            onChange={(e) => setJobDescription(e.target.value)}
            rows={12}
          />
        </div>
      </div>

      <div className="verified-skills-section">
        <div className="input-card verified-skills-card">
          <label htmlFor="verifiedSkills">Additional Real Skills / Experience</label>
          <textarea
            id="verifiedSkills"
            placeholder="Enter only skills you truly have but forgot to include in your resume. Do not enter fake skills. Example: Python, PostgreSQL, automation scripts, PyTest, Flask."
            value={verifiedAdditionalSkills}
            onChange={(e) => setVerifiedAdditionalSkills(e.target.value)}
            rows={4}
          />
        </div>
      </div>

      <div className="button-section">
        <button onClick={handleAnalyze} disabled={loading} className="analyze-btn">
          {loading ? 'Analyzing...' : 'Analyze Resume'}
        </button>
        <button onClick={handleClear} className="clear-btn">Clear</button>
        <button onClick={handleViewHistory} disabled={historyLoading} className="history-btn">
          {historyLoading ? 'Loading...' : showHistory ? 'Hide History' : 'View History'}
        </button>
      </div>

      {loading && (
        <div className="loading-message">
          <div className="spinner"></div>
          <p>Analyzing your resume... Please wait.</p>
        </div>
      )}

      {error && <div className="error-message">{error}</div>}
      {copySuccess && <div className="success-message">{copySuccess}</div>}

      {hasResults && (
        <div className="results-section">
          <div className="result-card score-card score-card-primary">
            <h2>ATS Score</h2>
            <div className={`score-circle ${getScoreClass(atsScore)}`}>
              <span className="score-number">{atsScore}</span>
              <span className="score-label">/ 100</span>
            </div>
            <p className={`score-verdict ${getScoreClass(atsScore)}`}>
              {scoreLabel || getScoreLabel(atsScore)}
            </p>
          </div>

          {roleMatchScore !== null && requiredSkillScore !== null && (
            <div className="result-card score-breakdown-card">
              <h2>Score Breakdown</h2>
              <div className="breakdown-grid">
                <div className="breakdown-item">
                  <span className="breakdown-label">Role Match</span>
                  <span className={`breakdown-value ${getScoreClass(roleMatchScore)}`}>{roleMatchScore}/100</span>
                </div>
                <div className="breakdown-item">
                  <span className="breakdown-label">Required Skills Match</span>
                  <span className={`breakdown-value ${getScoreClass(requiredSkillScore)}`}>{requiredSkillScore}/100</span>
                </div>
                <div className="breakdown-item">
                  <span className="breakdown-label">Overall Verdict</span>
                  <span className={`breakdown-value ${getScoreClass(atsScore)}`}>{scoreLabel || getScoreLabel(atsScore)}</span>
                </div>
              </div>
            </div>
          )}

          {roleMismatchWarning && (
            <div className="result-card warning-card">
              <h2>Role Mismatch Warning</h2>
              <p className="warning-text">{roleMismatchWarning}</p>
            </div>
          )}

          {(matchedKeywords.length > 0 || missingKeywords.length > 0) && (
            <div className="result-card keywords-card">
              {matchedKeywords.length > 0 && (
                <div className="keywords-group matched-section">
                  <h3>Matched Keywords ({matchedKeywords.length})</h3>
                  <div className="keyword-list">
                    {matchedKeywords.map((kw, i) => (
                      <span key={i} className="keyword-chip keyword-tag keyword-matched">{kw}</span>
                    ))}
                  </div>
                </div>
              )}
              {missingKeywords.length > 0 && (
                <div className="keywords-group matched-section">
                  <h3>Missing Keywords ({missingKeywords.length})</h3>
                  <div className="keyword-list">
                    {missingKeywords.map((kw, i) => (
                      <span key={i} className="keyword-chip keyword-tag keyword-missing">{kw}</span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {skillGapAnalysis && (
            <div className="result-card skill-gap-card">
              <h2>Skill Gap Analysis</h2>
              <p className="skill-gap-text">{skillGapAnalysis}</p>
            </div>
          )}

          {suggestions.length > 0 && (
            <div className="result-card">
              <h2>Suggestions</h2>
              <ul className="suggestions-list">
                {suggestions.map((s, i) => <li key={i}>{s}</li>)}
              </ul>
            </div>
          )}

          {improvedProfessionalSummary && (
            <div className="result-card">
              <h2>Improved Professional Summary</h2>
              <p className="improved-text">{improvedProfessionalSummary}</p>
            </div>
          )}

          {improvedBulletPoints.length > 0 && (
            <div className="result-card">
              <h2>Improved Bullet Points</h2>
              <ul className="bullet-list">
                {improvedBulletPoints.map((b, i) => <li key={i}>{b}</li>)}
              </ul>
            </div>
          )}

          {generatedResume && (
            <div className="result-card generated-resume-card">
              <h2>Generated Resume</h2>
              <div className="resume-actions">
                <button onClick={copyGeneratedResume} className="copy-btn">
                  Copy Generated Resume
                </button>
                <button onClick={useGeneratedResumeAsInput} className="use-input-btn">
                  Use Generated Resume as Input
                </button>
                <button
                  onClick={() => handleDownloadResume(generatedResume)}
                  className="download-btn"
                >
                  Download Generated Resume as TXT
                </button>
              </div>
              <p className="generated-resume-instruction">
                To check the ATS score of the generated resume, copy the generated resume,
                paste it into the Your Resume box, and click Analyze Resume again.
              </p>
              <pre className="generated-resume-text">{generatedResume}</pre>
            </div>
          )}

          {showGeneratedResumeScore && generatedResumeAtsScore !== null && (
            <div className="result-card score-card score-card-secondary">
              <h2>Generated Resume ATS Score</h2>
              <div className={`score-circle score-circle-small ${getScoreClass(generatedResumeAtsScore)}`}>
                <span className="score-number">{generatedResumeAtsScore}</span>
                <span className="score-label">/ 100</span>
              </div>
            </div>
          )}
        </div>
      )}

      {showHistory && (
        <div className="history-section">
          <h2 className="history-title">Analysis History</h2>

          {history.length === 0 ? (
            <p className="history-empty">No analysis history found. Analyze a resume to create your first record.</p>
          ) : (
            <div className="history-list">
              {history.map((item) => (
                <div key={item.id} className="history-card">
                  <div className="history-card-header">
                    <div className="history-scores">
                      <div className={`history-score ${getScoreClass(item.atsScore)}`}>
                        ATS Score: {item.atsScore}/100 &mdash; {item.scoreLabel || getScoreLabel(item.atsScore)}
                      </div>
                    </div>
                    <div className="history-date">{formatDate(item.createdAt)}</div>
                  </div>

                  {item.roleMismatchWarning && (
                    <div className="history-warning">
                      <strong>Role Mismatch:</strong> {item.roleMismatchWarning}
                    </div>
                  )}

                  {item.matchedKeywords && item.matchedKeywords.length > 0 && (
                    <div className="matched-section history-keywords">
                      <strong>Matched:</strong>
                      <div className="keyword-list">
                        {item.matchedKeywords.map((kw, i) => (
                          <span key={i} className="keyword-chip keyword-tag keyword-matched history-tag">{kw}</span>
                        ))}
                      </div>
                    </div>
                  )}

                  {item.missingKeywords && item.missingKeywords.length > 0 && (
                    <div className="matched-section history-keywords">
                      <strong>Missing:</strong>
                      <div className="keyword-list">
                        {item.missingKeywords.map((kw, i) => (
                          <span key={i} className="keyword-chip keyword-tag keyword-missing history-tag">{kw}</span>
                        ))}
                      </div>
                    </div>
                  )}

                  {item.skillGapAnalysis && (
                    <div className="history-skill-gap">
                      <strong>Skill Gap:</strong> {item.skillGapAnalysis}
                    </div>
                  )}

                  {item.suggestions && item.suggestions.length > 0 && (
                    <div className="history-suggestions">
                      <strong>Suggestions:</strong>
                      <ul>
                        {item.suggestions.map((s, i) => <li key={i}>{s}</li>)}
                      </ul>
                    </div>
                  )}

                  {item.improvedProfessionalSummary && (
                    <div className="history-summary">
                      <strong>Improved Summary:</strong> {item.improvedProfessionalSummary}
                    </div>
                  )}

                  {item.generatedResume && (
                    <div className="history-resume">
                      <strong>Generated Resume:</strong>
                      <div className="history-resume-actions">
                        <button
                          onClick={async () => {
                            await navigator.clipboard.writeText(item.generatedResume);
                            setCopySuccess('Resume copied to clipboard!');
                            setTimeout(() => setCopySuccess(''), 3000);
                          }}
                          className="copy-btn small-btn"
                        >
                          Copy
                        </button>
                      </div>
                      <pre className="history-resume-text">{item.generatedResume}</pre>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      <footer className="app-footer">
        <p>Resume Improvement Agent &mdash; Built with React + Spring Boot + PostgreSQL</p>
      </footer>
    </div>
  );
}

export default App;
