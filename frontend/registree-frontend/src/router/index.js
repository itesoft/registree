import { createRouter, createWebHistory } from 'vue-router'
import RegistriesList from '@/views/RegistriesList.vue'
import Console from '@/views/Console.vue'
import { validateToken } from '@/services/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/registries',
      name: 'registries-list',
      component: RegistriesList
    },
    {
      path: '/registries/:registryName',
      name: 'registry-detail',
      component: () => import('../views/RegistryDetail.vue'),
      props: true
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('@/views/Search.vue'),
      props: true
    },
    {
      path: '/search/:format',
      name: 'search-by-format',
      component: () => import('@/views/Search.vue'),
      props: true
    },
    {
      path: '/console',
      name: 'console',
      component: Console
    },
    {
      path: '/',
      redirect: '/registries'
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  await validateToken();
  next();
})

export default router;
