import { useState } from 'react';
import { demandService, baseRankingService, simulationService } from '../services/api';

const TABS = [
    { key: 'demand', label: ' Demand Prediction' },
    { key: 'baseRanking', label: ' Base Ranking' },
    { key: 'simulation', label: ' Simulation' },
];

export default function PredictionPanel({ selectedProgram }) {
    const [activeTab, setActiveTab] = useState('demand');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [result, setResult] = useState(null);

    // Simulation form state
    const [simParams, setSimParams] = useState({
        minQuota: 10,
        maxQuota: 100,
        baseRankingThreshold: 50000,
    });

    const handlePredict = async () => {
        if (!selectedProgram?.idOSYM) return;

        setLoading(true);
        setError(null);
        setResult(null);

        try {
            let response;

            switch (activeTab) {
                case 'demand':
                    response = await demandService.predict(selectedProgram.idOSYM);
                    setResult({ type: 'demand', value: response });
                    break;

                case 'baseRanking':
                    response = await baseRankingService.predict(selectedProgram.idOSYM);
                    setResult({ type: 'baseRanking', value: response });
                    break;

                case 'simulation':
                    response = await simulationService.runSimulation({
                        idOSYM: selectedProgram.idOSYM,
                        minQuota: parseFloat(simParams.minQuota),
                        maxQuota: parseFloat(simParams.maxQuota),
                        baseRankingThreshold: parseFloat(simParams.baseRankingThreshold),
                    });
                    setResult({ type: 'simulation', value: response });
                    break;
            }
        } catch (err) {
            setError(`Prediction failed: ${err.message}`);
            console.error('Prediction error:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleTabChange = (tab) => {
        setActiveTab(tab);
        setResult(null);
        setError(null);
    };

    if (!selectedProgram) {
        return (
            <section className="prediction-section">
                <div className="card">
                    <div className="empty-state">
                        <h4>Select a Program</h4>
                        <p>Choose a program from the list above to run predictions.</p>
                    </div>
                </div>
            </section>
        );
    }

    return (
        <section className="prediction-section">
            <div className="prediction-tabs">
                {TABS.map((tab) => (
                    <button
                        key={tab.key}
                        className={`prediction-tab ${activeTab === tab.key ? 'active' : ''}`}
                        onClick={() => handleTabChange(tab.key)}
                    >
                        {tab.label}
                    </button>
                ))}
            </div>

            <div className="prediction-content">
                <div style={{ marginBottom: '1rem', padding: '0.75rem', background: 'var(--bg-glass)', borderRadius: 'var(--radius-sm)' }}>
                    <strong style={{ color: 'var(--accent-primary)' }}>Selected Program:</strong>{' '}
                    <span style={{ color: 'var(--text-secondary)' }}>
                        {selectedProgram.departmentName} - {selectedProgram.universityName}
                    </span>
                    <br />
                    <small style={{ color: 'var(--text-muted)' }}>idOSYM: {selectedProgram.idOSYM}</small>
                </div>

                {activeTab === 'simulation' && (
                    <div className="prediction-form">
                        <div className="form-group">
                            <label>Minimum Quota</label>
                            <input
                                type="number"
                                value={simParams.minQuota}
                                onChange={(e) => setSimParams({ ...simParams, minQuota: e.target.value })}
                                min="1"
                            />
                        </div>
                        <div className="form-group">
                            <label>Maximum Quota</label>
                            <input
                                type="number"
                                value={simParams.maxQuota}
                                onChange={(e) => setSimParams({ ...simParams, maxQuota: e.target.value })}
                                min="1"
                            />
                        </div>
                        <div className="form-group">
                            <label>Base Ranking Threshold</label>
                            <input
                                type="number"
                                value={simParams.baseRankingThreshold}
                                onChange={(e) => setSimParams({ ...simParams, baseRankingThreshold: e.target.value })}
                                min="1"
                            />
                        </div>
                    </div>
                )}

                <button
                    className="btn btn-success"
                    onClick={handlePredict}
                    disabled={loading}
                    style={{ width: '100%', marginTop: '1rem' }}
                >
                    {loading ? (
                        <>
                            <div className="loading-spinner"></div>
                            <span>Running...</span>
                        </>
                    ) : (
                        <>Run {TABS.find(t => t.key === activeTab)?.label}</>
                    )}
                </button>

                {error && <div className="error-state" style={{ marginTop: '1rem' }}>{error}</div>}

                {result && (
                    <div className="result-display">
                        {result.type === 'demand' && (
                            <>
                                <h4> Demand Prediction Result</h4>
                                <div className="result-value">{Math.round(result.value).toLocaleString()}</div>
                                <div className="result-label">Predicted Demand (Applications)</div>
                            </>
                        )}

                        {result.type === 'baseRanking' && (
                            <>
                                <h4> Base Ranking Result</h4>
                                <div className="result-value">{Math.round(result.value).toLocaleString()}</div>
                                <div className="result-label">Predicted Base Ranking Position</div>
                            </>
                        )}

                        {result.type === 'simulation' && (
                            <>
                                <h4> Simulation Result</h4>
                                <div className="result-grid">
                                    <div className="result-item">
                                        <div className="value">{result.value.idOSYM}</div>
                                        <div className="label">Program ID</div>
                                    </div>
                                    <div className="result-item">
                                        <div className="value">{Math.round(result.value.optimalQuota || 0)}</div>
                                        <div className="label">Optimal Quota</div>
                                    </div>
                                    <div className="result-item">
                                        <div className="value">{Math.round(result.value.predictedBaseRanking || 0).toLocaleString()}</div>
                                        <div className="label">Predicted Base Ranking</div>
                                    </div>
                                </div>
                            </>
                        )}
                    </div>
                )}
            </div>
        </section>
    );
}
