export default function ProgramList({ programs, totalHits, selectedProgram, onSelect }) {
    if (!programs || programs.length === 0) {
        return (
            <section className="results-section">
                <div className="card">
                    <div className="empty-state">
                        <h4>No Programs Found</h4>
                        <p>Try adjusting your search filters to find programs.</p>
                    </div>
                </div>
            </section>
        );
    }

    return (
        <section className="results-section">
            <div className="results-header">
                <h3>Available Programs</h3>
                <span className="results-count">{totalHits} programs found</span>
            </div>

            <div className="results-grid">
                {programs.slice(0, 12).map((program) => (
                    <div
                        key={program.id || program.idOSYM}
                        className={`program-card ${selectedProgram?.idOSYM === program.idOSYM ? 'selected' : ''}`}
                        onClick={() => onSelect(program)}
                    >
                        <h4>{program.departmentName}</h4>
                        <div className="program-details">
                            <div className="detail">
                                <span className="label">University:</span>
                                <span>{program.universityName}</span>
                            </div>
                            <div className="detail">
                                <span className="label">Faculty:</span>
                                <span>{program.faculty}</span>
                            </div>
                            <div className="detail">
                                <span className="label">Language:</span>
                                <span>{program.language}</span>
                            </div>
                            <div className="detail">
                                <span className="label">Scholarship:</span>
                                <span>{program.scholarshipRate}</span>
                            </div>
                        </div>
                        <div className="idosym">
                            ID: {program.idOSYM}
                        </div>
                    </div>
                ))}
            </div>

            {programs.length > 12 && (
                <p style={{ textAlign: 'center', marginTop: '1rem', color: 'var(--text-muted)' }}>
                    Showing 12 of {totalHits} programs. Refine your filters to see more specific results.
                </p>
            )}
        </section>
    );
}
