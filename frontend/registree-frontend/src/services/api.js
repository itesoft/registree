import { useAuth, validateToken } from '@/services/auth';
import { API_URL } from '@/utils/config';

const { token } = useAuth();

export async function apiFetch(path, opts = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(opts.headers || {}),
  };
  if (token.value) {
    headers.Authorization = `Bearer ${token.value}`;
  }
  const response = await fetch(`${API_URL}${path}`, {
    ...opts,
    headers,
  });
  if (response.status == 401) {
    // TODO: redirect to first page, maybe open login box?
  }
  return response;
}
