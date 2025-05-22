<template>
  <ErrorBanner :errorMessage="error" @clear-error="error = ''" />
  <Breadcrumb :path="breadcrumbPath" />
  <div class="registries-container">
    <div v-if="registries.length">
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Format</th>
            <th>Type</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="registry in registries" :key="registry.name">
            <td @click="navigateToDetail(registry)">{{ registry.name }}</td>
            <td @click="navigateToDetail(registry)">{{ registry.format }}</td>
            <td @click="navigateToDetail(registry)">
              <span :class="['badge', getBadgeClass(registry.type)]">
                {{ registry.type }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">📄</div>
      <p>No registry</p>
    </div>
  </div>
</template>

<script>
import { inject, onMounted, ref } from 'vue';
import router from '@/router';
import Breadcrumb from '@/components/Breadcrumb.vue';
import ErrorBanner from '@/components/ErrorBanner.vue';
import { apiFetch } from '@/services/api';

export default {
  components: { Breadcrumb, ErrorBanner },
  setup() {
    const registries = ref([]);
    const error = ref(null);
    const breadcrumbPath = ref([]);

    async function fetchRegistries() {
      try {
        const response = await apiFetch('/registries?sort=name&page_size=100');

        if (!response.ok) {
          throw new Error('Failed to fetch data');
        }

        registries.value = await response.json();
      } catch (err) {
        error.value = err.message;
      }
    }

    function navigateToDetail(registry) {
      router.push({
        name: 'registry-detail',
        params: { registryName: registry.name }
      });
    }

    function getBadgeClass(type) {
      const typeClasses = {
        'hosted': 'badge-blue',
        'proxy': 'badge-green',
        'group': 'badge-gray',
        'default': 'badge-red'
      };
      return typeClasses[type.toLowerCase()] || typeClasses['default'];
    }

    onMounted(async () => {
      breadcrumbPath.value = [{link: "/registries", label: "Registries"}];
      await fetchRegistries();
    });

    return {
      registries,
      error,
      breadcrumbPath,
      navigateToDetail,
      getBadgeClass
    }
  }
}
</script>
