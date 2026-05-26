import { browser } from '$app/environment';

type RequestOptions = {
  method?: string;
  body?: unknown;
  headers?: Record<string, string>;
};

const API_BASE = (typeof import.meta !== 'undefined' && (import.meta as any).env?.VITE_API_URL) || '';

function apiUrl(path: string): string {
  return `${API_BASE}${path}`;
}

export async function api(path: string, options: RequestOptions = {}) {
  const token = browser ? localStorage.getItem('token') : null;
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...options.headers
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const res = await fetch(apiUrl(path), {
    method: options.method || 'GET',
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined
  });
  if (res.status === 401) {
    if (browser) localStorage.removeItem('token');
    window.location.href = '/login';
    throw new Error('Unauthorized');
  }
  return res;
}

export async function apiJson<T>(path: string, options?: RequestOptions): Promise<T> {
  const res = await api(path, options);
  if (!res.ok) {
    const error = await res.text();
    throw new Error(error || `API error: ${res.status}`);
  }
  return res.json();
}
