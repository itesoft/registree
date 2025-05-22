<template>
  <form class="search-criteria" @submit.prevent="emitSearch">
    <input v-model="scope" type="text" placeholder="Scope" class="search-input" />
    <input v-model="name" type="text" placeholder="Name" class="search-input" />
    <input v-model="version" type="text" placeholder="Version" class="search-input" />
    <button type="submit" class="search-button">Search</button>
  </form>
</template>

<script>
import { ref } from 'vue';
import { toRsql } from '@/utils/search';

export default {
  emits: ['search'],
  setup(_, { emit }) {
    const scope = ref('');
    const name = ref('');
    const version = ref('');

    const emitSearch = () => {
      const query = toRsql({
        group: scope.value,
        name: name.value,
        version: version.value
      });
      emit('search', { filter: query })
    }

    return {
      scope,
      name,
      version,
      emitSearch
    }
  }
}
</script>
