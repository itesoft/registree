<template>
  <div>
    <component :is="criteriaComponent"
               :key="format"
               @search="rememberAndSearch"
    />
    <div v-if="hasSearched">
      <SearchResults :results="results" />
      <div class="search-footer">
        <p>{{ totalCount }} components</p>
        <div class="pagination">
          <button :disabled="currentPage === 1"
                  @click="goToPage(currentPage - 1)">
            <
          </button>
          <span>Page {{ currentPage }}</span>
          <button :disabled="currentPage * pageSize >= totalCount"
                  @click="goToPage(currentPage + 1)">
            >
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, defineAsyncComponent, watch } from 'vue';
import SearchResults from '@/components/SearchResults.vue';
import { apiFetch } from '@/services/api';

export default {
  props: ['format'],
  components: {
    SearchResults
  },
  setup(props) {
    const results = ref([]);
    const totalCount = ref(0);
    const currentPage = ref(1);
    const pageSize = 20;
    const hasSearched = ref(false);
    const lastCriteria = ref('');

    const criteriaComponent = computed(() =>
      defineAsyncComponent({
        oci: () => import('@/components/SearchCriteriaOci.vue'),
        npm: () => import('@/components/SearchCriteriaNpm.vue'),
        maven: () => import('@/components/SearchCriteriaMaven.vue'),
        raw: () => import('@/components/SearchCriteriaRaw.vue')
      }[props.format] || (() => import('@/components/SearchCriteria.vue')))
    )

    async function search(criteria) {
      try {
        const subFilter = new URLSearchParams(criteria).toString();
        let filter;
        if (props.format) {
          filter = `${subFilter};registry.format==${props.format}`;
        }
        else {
          filter = subFilter;
        }
        const page = currentPage.value - 1;

        const response = await apiFetch(`/components?${filter}&sort=id&page=${page}&page_size=${pageSize}`);
        if (!response.ok) {
          throw new Error('Search failed');
        }
        const data = await response.json();
        const total = response.headers.get('x-total-count') || '0';
        results.value = data;
        totalCount.value = parseInt(total);
      } catch (err) {
        console.error(err);
        results.value = [];
        totalCount.value = 0;
      }
    }

    async function handleSearch(criteria) {
      if (criteria.filter !== "") {
        currentPage.value = 1
        hasSearched.value = true;
        emitSearch(criteria);
      }
    }

    async function emitSearch(criteria) {
      await search(criteria);
    }

    async function goToPage(page) {
      currentPage.value = page;
      emitSearch(lastCriteria.value);
    }

    async function rememberAndSearch(criteria) {
      lastCriteria.value = criteria;
      handleSearch(criteria);
    }

    watch(() => props.format, () => {
      results.value = [];
      totalCount.value = 0;
      currentPage.value = 1;
      hasSearched.value = false;
      lastCriteria.value = '';
    })

    return {
      criteriaComponent,
      goToPage,
      rememberAndSearch,
      currentPage,
      pageSize,
      results,
      totalCount,
      hasSearched
    }
  }
}
</script>
