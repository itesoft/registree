<template>
  <li class="tree-node">
    <div @click.stop="$emit('select-node', node)" class="node-item" :class="{ 'directory': node.type === 'directory' }">
      <button v-if="node.type === 'directory'" @click.stop="toggle" class="toggle-btn">
        {{ expanded ? '-' : '+' }}
      </button>
      <span v-else class="toggle-btn-placeholder"></span>
      <span v-if="node.type === 'directory'" class="icon">📂</span>
      <span v-else class="icon">📄</span>
      <span class="node-name">{{ node.name }}</span>
    </div>
    <ul v-if="expanded && node.children" class="children">
      <TreeNode
        v-for="resource in node.children"
        :key="resource.name"
        :node="resource"
        :ref="element => $emit('register-ref', element)"
        @register-ref="$emit('register-ref', $event)"
        @load-children="$emit('load-children', $event)"
        @select-node="$emit('select-node', $event)" />
    </ul>
  </li>
</template>

<script>
import { ref, defineAsyncComponent } from 'vue';

export default {
  components: {
    TreeNode: defineAsyncComponent(() => import('@/components/TreeNode.vue'))
  },
  props: {
    node: Object
  },
  emits: ['register-ref', 'load-children', 'select-node'],
  setup(props, { emit }) {
    const expanded = ref(false);

    const toggle = async () => {
      if (props.node.type === 'directory') {
        expanded.value = !expanded.value;
        if (expanded.value) {
          emit('load-children', props.node);
        }
      }
    };

    return {
      expanded,
      toggle
    };
  }
};
</script>
