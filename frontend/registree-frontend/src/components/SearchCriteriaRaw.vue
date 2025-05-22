<template>
  <form class="search-criteria" @submit.prevent="emitSearch">
    <input v-model="name" type="text" placeholder="Name" class="search-input" />
    <button type="submit" class="search-button">Search</button>
  </form>
</template>

<script>
import { ref } from 'vue';
import { toRsql } from '@/utils/search';

export default {
  emits: ['search'],
  setup(_, { emit }) {
    const name = ref('');

    const emitSearch = () => {
      const query = toRsql({
        name: name.value,
      });
      emit('search', { filter: query })
    }

    return {
      name,
      emitSearch
    }
  }
}
</script>
