import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// Em desenvolvimento, /api vai para o Spring Boot. Assim o front não precisa saber a URL do backend
// e não precisamos liberar CORS.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': process.env.VITE_API_URL ?? 'http://localhost:8080',
    },
  },
})
