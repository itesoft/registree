import { useLocalStorage } from '@vueuse/core';
import { API_URL } from '@/utils/config';

const TOKEN_KEY = 'auth_token'
const token = useLocalStorage(TOKEN_KEY, '')

export function setToken(newToken) {
  token.value = newToken
}

export function clearToken() {
  token.value = ''
}

export async function validateToken() {
  if (!token.value) {
    return false;
  }
  let valid = false;
  try {
    const headers = {
      'Content-Type': 'application/json'
    };
    if (token.value) {
      headers.Authorization = `Bearer ${token.value}`;
    }
    const response = await fetch(`${API_URL}/tokens/${token.value}`, {
      headers
    });
    valid = response.ok
  } catch (error) {
    console.log(error.message);
  }
  if (!valid) {
    clearToken();
  }
  return valid;
}

export function useAuth() {
  return { token, setToken, clearToken, validateToken }
}
