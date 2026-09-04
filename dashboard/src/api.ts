const simulatorUrl = import.meta.env.VITE_SIMULATOR_API_URL || 'http://localhost:8081'
const infraUrl = import.meta.env.VITE_INFRA_API_URL || 'http://localhost:8080'
const username = import.meta.env.VITE_INFRA_USERNAME || ''
const password = import.meta.env.VITE_INFRA_PASSWORD || ''

async function request<T>(base:string, path:string, init:RequestInit = {}, authenticated=false):Promise<T> {
  const headers = new Headers(init.headers)
  if (init.body) headers.set('Content-Type', 'application/json')
  if (authenticated) headers.set('Authorization', `Basic ${btoa(`${username}:${password}`)}`)
  const response = await fetch(`${base}${path}`, {...init, headers})
  if (!response.ok) {
    const body = await response.json().catch(() => ({}))
    throw new Error(body.message || `${response.status} ${response.statusText}`)
  }
  return response.json()
}

export const simulatorApi = {
  get: <T>(path:string) => request<T>(simulatorUrl, path),
  post: <T>(path:string, body?:unknown) => request<T>(simulatorUrl, path, {method:'POST', body:body ? JSON.stringify(body) : undefined}),
  patch: <T>(path:string, body:unknown) => request<T>(simulatorUrl, path, {method:'PATCH', body:JSON.stringify(body)}),
  delete: <T>(path:string) => request<T>(simulatorUrl, path, {method:'DELETE'})
}
export const infraApi = {
  get: <T>(path:string) => request<T>(infraUrl, path, {}, true),
  post: <T>(path:string, body?:unknown) => request<T>(infraUrl, path, {method:'POST', body:body ? JSON.stringify(body) : undefined}, true),
  delete: <T>(path:string) => request<T>(infraUrl, path, {method:'DELETE'}, true)
}
