import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Pendant le développement, les appels /api/... sont envoyés au backend Spring Boot.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
