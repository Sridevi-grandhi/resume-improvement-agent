import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    // host: true makes Vite listen on 0.0.0.0 so the dev server is
    // reachable from outside the container (and on the LAN locally).
    host: true,
    port: 5173,
  },
});
