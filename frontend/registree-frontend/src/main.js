import "@/assets/styles.css";

import { createApp } from 'vue'
import App from '@/App.vue'
import router from '@/router'
import { API_URL, BACKEND_URL } from '@/utils/config';

const app = createApp(App);
app.provide('BACKEND_URL', BACKEND_URL);
app.provide('API_URL', API_URL);
app.use(router);
app.mount('#app');
