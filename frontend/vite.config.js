import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/auth': 'http://localhost:8080',
      '/course': 'http://localhost:8080',
      '/toggles': 'http://localhost:8080',
      '/files': 'http://localhost:8080'
    }
  }
})
