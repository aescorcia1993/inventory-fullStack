import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        silenceDeprecations: ['legacy-js-api']
      }
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api/v1/products': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/v1/inventory': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/v1/purchases': {
        target: 'http://localhost:8083',
        changeOrigin: true
      }
    }
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts']
  }
})
