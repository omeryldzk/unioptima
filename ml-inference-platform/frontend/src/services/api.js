const API_BASE_URL = '/api';

export const searchService = {
    async search(request) {
        const response = await fetch(`${API_BASE_URL}/search`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request),
        });

        if (!response.ok) {
            throw new Error('Search failed');
        }

        return response.json();
    }
};

export const demandService = {
    async predict(idOSYM) {
        const response = await fetch(`${API_BASE_URL}/demand/predict`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ idOSYM }),
        });

        if (!response.ok) {
            throw new Error('Demand prediction failed');
        }

        return response.json();
    },

    async getFeatures(idOSYM) {
        const response = await fetch(`${API_BASE_URL}/demand/${idOSYM}/features`);

        if (!response.ok) {
            throw new Error('Failed to get demand features');
        }

        return response.json();
    }
};

export const baseRankingService = {
    async predict(idOSYM) {
        const response = await fetch(`${API_BASE_URL}/base-ranking/predict`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ idOSYM }),
        });

        if (!response.ok) {
            throw new Error('Base ranking prediction failed');
        }

        return response.json();
    },

    async getFeatures(idOSYM) {
        const response = await fetch(`${API_BASE_URL}/base-ranking/${idOSYM}/features`);

        if (!response.ok) {
            throw new Error('Failed to get base ranking features');
        }

        return response.json();
    }
};

export const simulationService = {
    async runSimulation(request) {
        const response = await fetch(`${API_BASE_URL}/simulation/`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request),
        });

        if (!response.ok) {
            throw new Error('Simulation failed');
        }

        return response.json();
    }
};
