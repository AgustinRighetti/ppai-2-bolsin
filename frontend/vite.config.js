import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// El frontend llama a /api y Vite lo redirige al backend Spring Boot.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
