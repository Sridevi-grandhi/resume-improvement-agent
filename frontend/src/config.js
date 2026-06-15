// Base URL for the backend API.
// Configurable via the VITE_API_BASE_URL environment variable, with a
// sensible fallback for local development / Docker (browser hits the host).
export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';
