<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal">
      <h2>Login</h2>
      <input v-model="username" type="text" placeholder="Username" />
      <input v-model="password" type="password" placeholder="Password" />
      <button @click="login">Login</button>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </div>
</template>

<script>
import { inject, ref } from 'vue';
import { setToken } from '@/services/auth';

export default {
  emits: ['close'],
  setup(props, { emit }) {
    const API_URL = inject('API_URL');
    const username = ref('');
    const password = ref('');
    const error = ref('');

    const login = async () => {
      error.value = '';
      try {
        const response = await fetch(`${API_URL}/tokens`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username: username.value, password: password.value }),
        });
        if (!response.ok) {
          throw new Error('Invalid credentials');
        }
        const result = await response.json();
        setToken(result.token);
        emit('close');
      } catch (err) {
        error.value = err.message;
      }
    };

    const close = () => {
      emit('close');
    };

    return {
      username,
      password,
      error,
      login,
      close
    }
  }
};
</script>
