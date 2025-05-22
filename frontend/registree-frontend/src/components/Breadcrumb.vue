<template>
  <nav class="breadcrumb">
    <ul class="breadcrumb-list">
      <li v-for="(item, index) in path" :key="index" class="breadcrumb-item">
        <router-link :to="item.link">{{ item.label }}</router-link>
      </li>
    </ul>
  </nav>
</template>

<script>
export default {
  props: {
    path: {
      type: Array,
      default: () => []
    }
  }
};
</script>
