import { useState } from 'react'
import SearchFilters from './components/SearchFilters'
import ProgramList from './components/ProgramList'
import PredictionPanel from './components/PredictionPanel'
import './App.css'

function App() {
  const [programs, setPrograms] = useState([])
  const [totalHits, setTotalHits] = useState(0)
  const [selectedProgram, setSelectedProgram] = useState(null)

  const handleProgramsUpdate = (items, total) => {
    setPrograms(items)
    setTotalHits(total)
    // Clear selection if the selected program is no longer in the list
    if (selectedProgram && !items.find(p => p.idOSYM === selectedProgram.idOSYM)) {
      setSelectedProgram(null)
    }
  }

  const handleProgramSelect = (program) => {
    setSelectedProgram(program)
  }

  return (
    <div className="app">
      <div className="container">
        <header className="header">
          <h1>UniOptima</h1>
          <p>University Program Prediction Platform</p>
        </header>

        <SearchFilters
          onProgramsUpdate={handleProgramsUpdate}
          onProgramSelect={handleProgramSelect}
        />

        <ProgramList
          programs={programs}
          totalHits={totalHits}
          selectedProgram={selectedProgram}
          onSelect={handleProgramSelect}
        />

        <PredictionPanel
          selectedProgram={selectedProgram}
        />
      </div>
    </div>
  )
}

export default App
