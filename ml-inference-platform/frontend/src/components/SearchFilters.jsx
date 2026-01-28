import { useState, useEffect, useCallback } from 'react';
import { searchService } from '../services/api';

const FILTER_FIELDS = [
    { key: 'universityName', label: 'University', placeholder: 'Search universities...' },
    { key: 'faculty', label: 'Faculty', placeholder: 'Search faculties...' },
    { key: 'departmentName', label: 'Department', placeholder: 'Search departments...' },
    { key: 'language', label: 'Language', placeholder: 'Search languages...' },
    { key: 'scholarshipRate', label: 'Scholarship Rate', placeholder: 'Search scholarship rates...' },
];

export default function SearchFilters({ onProgramsUpdate, onProgramSelect }) {
    const [currentStep, setCurrentStep] = useState(0);
    const [locks, setLocks] = useState({});
    const [searchText, setSearchText] = useState('');
    const [facets, setFacets] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Debounce search
    const debounce = (func, wait) => {
        let timeout;
        return (...args) => {
            clearTimeout(timeout);
            timeout = setTimeout(() => func(...args), wait);
        };
    };

    const performSearch = useCallback(async (q, targetField, currentLocks) => {
        setLoading(true);
        setError(null);

        try {
            const response = await searchService.search({
                q: q || '',
                targetField,
                locks: currentLocks,
            });

            setFacets(response.facets || []);
            onProgramsUpdate(response.items || [], response.totalHits || 0);
        } catch (err) {
            setError('Failed to search. Please try again.');
            console.error('Search error:', err);
        } finally {
            setLoading(false);
        }
    }, [onProgramsUpdate]);

    const debouncedSearch = useCallback(
        debounce((q, targetField, currentLocks) => {
            performSearch(q, targetField, currentLocks);
        }, 300),
        [performSearch]
    );

    // Initial search on mount
    useEffect(() => {
        performSearch('', FILTER_FIELDS[0].key, {});
    }, []);

    // Search when text changes
    useEffect(() => {
        if (currentStep < FILTER_FIELDS.length) {
            debouncedSearch(searchText, FILTER_FIELDS[currentStep].key, locks);
        }
    }, [searchText, currentStep, locks]);

    const handleSelectFacet = (value) => {
        const currentField = FILTER_FIELDS[currentStep].key;
        const newLocks = { ...locks, [currentField]: value };

        setLocks(newLocks);
        setSearchText('');

        if (currentStep < FILTER_FIELDS.length - 1) {
            setCurrentStep(currentStep + 1);
            performSearch('', FILTER_FIELDS[currentStep + 1].key, newLocks);
        } else {
            // All filters applied, just update results
            performSearch('', currentField, newLocks);
        }
    };

    const handleClearFilter = (stepIndex) => {
        const newLocks = {};
        // Keep locks only up to stepIndex - 1
        FILTER_FIELDS.slice(0, stepIndex).forEach(field => {
            if (locks[field.key]) {
                newLocks[field.key] = locks[field.key];
            }
        });

        setLocks(newLocks);
        setCurrentStep(stepIndex);
        setSearchText('');
        performSearch('', FILTER_FIELDS[stepIndex].key, newLocks);
    };

    const handleReset = () => {
        setLocks({});
        setCurrentStep(0);
        setSearchText('');
        setFacets([]);
        performSearch('', FILTER_FIELDS[0].key, {});
    };

    return (
        <section className="search-section">
            <div className="filter-steps">
                {FILTER_FIELDS.map((field, index) => {
                    const isActive = index === currentStep;
                    const isCompleted = locks[field.key] !== undefined;
                    const isDisabled = index > currentStep && !locks[FILTER_FIELDS[index - 1]?.key];

                    return (
                        <div
                            key={field.key}
                            className={`filter-step ${isActive ? 'active' : ''} ${isCompleted ? 'completed' : ''} ${isDisabled ? 'disabled' : ''}`}
                        >
                            <div className="filter-header">
                                <div className="filter-label">
                                    <span className="step-number">
                                        {isCompleted ? '✓' : index + 1}
                                    </span>
                                    <h4>{field.label}</h4>
                                </div>
                                {isActive && facets.length > 0 && (
                                    <span className="filter-count">{facets.length} options</span>
                                )}
                            </div>

                            {isCompleted && !isActive ? (
                                <div className="selected-value">
                                    <span>{locks[field.key]}</span>
                                    <button onClick={() => handleClearFilter(index)} title="Clear">
                                        ×
                                    </button>
                                </div>
                            ) : isActive ? (
                                <>
                                    <input
                                        type="text"
                                        className="search-input"
                                        placeholder={field.placeholder}
                                        value={searchText}
                                        onChange={(e) => setSearchText(e.target.value)}
                                        autoFocus
                                    />

                                    {loading && (
                                        <div className="loading">
                                            <div className="loading-spinner"></div>
                                            <span>Searching...</span>
                                        </div>
                                    )}

                                    {error && <div className="error-state">{error}</div>}

                                    {!loading && !error && facets.length > 0 && (
                                        <div className="facet-options">
                                            {facets.map((facet) => (
                                                <button
                                                    key={facet.value}
                                                    className="facet-option"
                                                    onClick={() => handleSelectFacet(facet.value)}
                                                >
                                                    <span>{facet.value}</span>
                                                    <span className="count">{facet.count}</span>
                                                </button>
                                            ))}
                                        </div>
                                    )}

                                    {!loading && !error && facets.length === 0 && searchText && (
                                        <div className="empty-state">
                                            <p>No options found for "{searchText}"</p>
                                        </div>
                                    )}
                                </>
                            ) : null}
                        </div>
                    );
                })}
            </div>

            {Object.keys(locks).length > 0 && (
                <button
                    className="btn"
                    onClick={handleReset}
                    style={{ marginTop: '1rem' }}
                >
                    Reset All Filters
                </button>
            )}
        </section>
    );
}
