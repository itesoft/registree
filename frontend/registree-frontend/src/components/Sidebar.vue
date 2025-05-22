<template>
  <div class="sidebar">
    <nav class="nav-top">
      <RouterLink to="/" class="nav-link">Registries</RouterLink>
      <RouterLink to="/search" class="nav-link">Search</RouterLink>
      <RouterLink to="/search/oci" class="nav-link">Search oci</RouterLink>
      <RouterLink to="/search/maven" class="nav-link">Search maven</RouterLink>
      <RouterLink to="/search/npm" class="nav-link">Search npm</RouterLink>
      <RouterLink to="/search/raw" class="nav-link">Search raw</RouterLink>
      <RouterLink v-if="token" to="/console" class="nav-link">Console</RouterLink>
    </nav>
    <div class="nav-bottom">
      <button v-if="token" @click="logout" class="login-button">Logout</button>
      <button v-else @click="showLogin = true" class="login-button">Login</button>
    </div>
  </div>
  <LoginModal v-if="showLogin" @close="showLogin = false" />
</template>

<script>
import { ref } from 'vue';
import LoginModal from '@/components/LoginModal.vue';
import { useAuth } from '@/services/auth'

export default {
  components: { LoginModal },
  setup() {
    const showLogin = ref(false);
    const { token, clearToken } = useAuth();

    function logout() {
      clearToken();
    }

    return {
      showLogin,
      token,
      logout
    }
  }
};
</script>
