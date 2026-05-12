import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/',
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.api+json'
  }
})

// Deserialize JSON:API single resource response
export function deserializeSingle<T>(response: { data: { id: string; attributes: T } }): T & { id: string } {
  return { id: response.data.id, ...response.data.attributes }
}

// Deserialize JSON:API list response
export function deserializeList<T>(response: { data: Array<{ id: string; attributes: T }> }): Array<T & { id: string }> {
  return response.data.map(item => ({ id: item.id, ...item.attributes }))
}

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      console.error('Unauthorized — invalid API key')
    }
    return Promise.reject(error)
  }
)

export default api
