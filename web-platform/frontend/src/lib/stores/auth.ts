import { writable, derived } from 'svelte/store';
import { browser } from '$app/environment';

export interface User {
  username: string;
  role: string;
  totalScore: number;
  rank: string;
}

export const user = writable<User | null>(null);
export const token = writable<string | null>(browser ? localStorage.getItem('token') : null);
export const isLoggedIn = derived(user, ($user) => $user !== null);

export async function login(username: string, password: string) {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  if (!res.ok) throw new Error('Login failed');
  const data = await res.json();
  token.set(data.token);
  user.set({ username: data.username, role: data.role, totalScore: data.totalScore, rank: data.rank });
  if (browser) localStorage.setItem('token', data.token);
  return data;
}

export function logout() {
  token.set(null);
  user.set(null);
  if (browser) localStorage.removeItem('token');
}

export async function checkAuth() {
  const t = browser ? localStorage.getItem('token') : null;
  if (!t) return;
  try {
    const res = await fetch('/api/user/me', {
      headers: { Authorization: `Bearer ${t}` }
    });
    if (res.ok) {
      const data = await res.json();
      user.set(data);
      token.set(t);
    } else {
      logout();
    }
  } catch {
    logout();
  }
}
