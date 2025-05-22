<template>
  <div v-if="errorMessage" class="error-banner">
    <span class="error-text">{{ errorMessage }}</span>
    <button class="close-btn" @click="clearError">✖</button>
  </div>
</template>

<script>
export default {
  props: {
    errorMessage: {
      type: String,
      default: ''
    }
  },
  emits: ['clear-error'],
  setup(props, { emit }) {
    const clearError = () => {
      emit('clear-error');
    };

    return { clearError };
  }
};
</script>
