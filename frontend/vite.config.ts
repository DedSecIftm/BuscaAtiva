import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Em desenvolvimento, as chamadas à API vão para o backend local (no Docker, quem faz isso é o nginx).
    proxy: { '/api': 'http://localhost:8080' },
  },
})
