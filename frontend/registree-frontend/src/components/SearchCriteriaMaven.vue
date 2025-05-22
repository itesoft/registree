<template>
  <form class="search-criteria" @submit.prevent="emitSearch">
    <input v-model="group" type="text" placeholder="Group" class="search-input" />
    <input v-model="artifact" type="text" placeholder="Artifact" class="search-input" />
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
    const group = ref('');
    const artifact = ref('');
    const version = ref('');

    const emitSearch = () => {
      const query = toRsql({
        group: group.value,
        name: artifact.value,
        version: version.value
      });
      emit('search', { filter: query })
    }

    return {
      group,
      artifact,
      version,
      emitSearch
    }
  }
}
</script>
