import { useState } from 'react';
import { demandService, baseRankingService, simulationService } from '../services/api';
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer,
    ReferenceLine,
    Scatter
} from 'recharts';

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

    const CustomTooltip = ({ active, payload, label }) => {
        if (active && payload && payload.length) {
            return (
                <div style={{
                    backgroundColor: 'rgba(25, 25, 35, 0.9)',
                    padding: '10px',
                    border: '1px solid var(--border-color)',
                    borderRadius: 'var(--radius-sm)',
                    boxShadow: 'var(--shadow-card)'
                }}>
                    <p style={{ margin: 0, fontWeight: 'bold', color: 'var(--text-primary)' }}>{`Quota: ${label}`}</p>
                    <p style={{ margin: 0, color: 'var(--accent-primary)' }}>{`Ranking: ${Math.round(payload[0].value).toLocaleString()}`}</p>
                    <p style={{ margin: 0, color: 'var(--accent-success)' }}>{`Demand: ${Math.round(payload[1]?.value || 0)}`}</p>
                </div>
            );
        }
        return null;
    };

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

                {error && (
                    <div className="error-state" style={{ marginTop: '1rem', textAlign: 'left' }}>
                        <div style={{ fontWeight: 'bold', marginBottom: '0.5rem' }}>Simulation Failed</div>
                        <div style={{ fontSize: '0.9rem', marginBottom: '1rem' }}>{error.replace('Prediction failed: ', '')}</div>

                        <div style={{
                            fontSize: '0.85rem',
                            padding: '0.75rem',
                            background: 'rgba(255, 255, 255, 0.05)',
                            borderRadius: 'var(--radius-sm)',
                            borderLeft: '3px solid var(--accent-error)'
                        }}>
                            <strong style={{ display: 'block', marginBottom: '0.25rem', color: 'var(--text-primary)' }}>Suggested Actions:</strong>
                            <ul style={{ margin: 0, paddingLeft: '1.25rem', color: 'var(--text-secondary)' }}>
                                {error.includes('threshold') ? (
                                    <>
                                        <li>Increase the <strong>Base Ranking Threshold</strong>.</li>
                                        <li>Decrease the <strong>Minimum Quota</strong>.</li>
                                    </>
                                ) : error.includes('university type') ? (
                                    <li>This program type is not currently supported for simulation.</li>
                                ) : (
                                    <>
                                        <li>Double-check your simulation parameters.</li>
                                        <li>Ensure the quota range (Min to Max) is valid.</li>
                                    </>
                                )}
                            </ul>
                        </div>
                    </div>
                )}

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
                                <h4> Simulation Overview</h4>
                                <div className="result-grid" style={{ marginBottom: '2rem' }}>
                                    <div className="result-item">
                                        <div className="value">{Math.round(result.value.result.optimalQuota || 0)}</div>
                                        <div className="label">Optimal Quota</div>
                                    </div>
                                    <div className="result-item">
                                        <div className="value">{Math.round(result.value.result.predictedBaseRanking || 0).toLocaleString()}</div>
                                        <div className="label">Best Base Ranking</div>
                                    </div>
                                    <div className="result-item">
                                        <div className="value">{result.value.result.predictedDemand}</div>
                                        <div className="label">Predicted Demand</div>
                                    </div>
                                </div>

                                <h4> Simulation Steps Visualization</h4>
                                <div style={{ width: '100%', height: 400, marginTop: '1rem', background: 'var(--bg-secondary)', padding: '1rem', borderRadius: 'var(--radius-md)' }}>
                                    <ResponsiveContainer>
                                        <LineChart data={result.value.steps} margin={{ top: 10, right: 30, left: 20, bottom: 0 }}>
                                            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                                            <XAxis
                                                dataKey="quota"
                                                stroke="var(--text-muted)"
                                                fontSize={12}
                                                tickLine={false}
                                                axisLine={false}
                                                label={{ value: 'Quota', position: 'insideBottom', offset: -5, fill: 'var(--text-muted)', fontSize: 12 }}
                                            />
                                            <YAxis
                                                yAxisId="left"
                                                stroke="var(--accent-primary)"
                                                fontSize={12}
                                                tickLine={false}
                                                axisLine={false}
                                                tickFormatter={(value) => value > 1000 ? `${(value / 1000).toFixed(0)}k` : value}
                                                label={{ value: 'Ranking', angle: -90, position: 'insideLeft', fill: 'var(--accent-primary)', fontSize: 12 }}
                                            />
                                            <YAxis
                                                yAxisId="right"
                                                orientation="right"
                                                stroke="var(--accent-success)"
                                                fontSize={12}
                                                tickLine={false}
                                                axisLine={false}
                                                label={{ value: 'Demand', angle: 90, position: 'insideRight', fill: 'var(--accent-success)', fontSize: 12 }}
                                            />
                                            <Tooltip content={<CustomTooltip />} />
                                            <Legend verticalAlign="top" height={36} />
                                            <Line
                                                yAxisId="left"
                                                type="monotone"
                                                dataKey="predictedBaseRanking"
                                                name="Base Ranking"
                                                stroke="var(--accent-primary)"
                                                strokeWidth={3}
                                                dot={{ r: 4, fill: 'var(--accent-primary)', strokeWidth: 2 }}
                                                activeDot={{ r: 6, strokeWidth: 0 }}
                                                animationDuration={1500}
                                            />
                                            <Line
                                                yAxisId="right"
                                                type="monotone"
                                                dataKey="predictedDemand"
                                                name="Predicted Demand"
                                                stroke="var(--accent-success)"
                                                strokeWidth={2}
                                                strokeDasharray="5 5"
                                                dot={false}
                                                animationDuration={1500}
                                            />
                                            {result.value.result && (
                                                <ReferenceLine
                                                    yAxisId="left"
                                                    x={result.value.result.optimalQuota}
                                                    stroke="var(--accent-error)"
                                                    strokeDasharray="3 3"
                                                    label={{ value: 'Optimal', fill: 'var(--accent-error)', fontSize: 12, position: 'top' }}
                                                />
                                            )}
                                        </LineChart>
                                    </ResponsiveContainer>
                                </div>
                                <div style={{ marginTop: '1rem', fontSize: '0.875rem', color: 'var(--text-secondary)', textAlign: 'center' }}>
                                    The graph shows how the predicted base ranking and demand evolve as the quota increases.
                                    The <span style={{ color: 'var(--accent-error)', fontWeight: 'bold' }}>red line</span> indicates the identified optimal configuration.
                                </div>
                            </>
                        )}
                    </div>
                )}
            </div>
        </section>
    );
}
