<template>
  <ErrorBanner :errorMessage="error" @clear-error="error = ''" />
  <Breadcrumb :path="breadcrumbPath" />
  <div class="container">
    <div class="tree-panel">
      <ul>
        <TreeNode
          v-for="resource in treeData"
          :key="resource.path"
          :node="resource"
          :ref="element => registerRef(element)"
          @register-ref="registerRef"
          @load-children="loadChildren"
          @select-node="selectNode" />
      </ul>
    </div>
    <div class="details-panel" v-if="selectedNode && details.relativeDownloadPath">
      <h2 class="section-title">Details</h2>
      <p v-if="details.group"><strong>Group :</strong> {{ details.group || 'Non renseigné' }}</p>
      <p><strong>Name:</strong> {{ details.name }}</p>
      <p v-if="details.version"><strong>Version:</strong> {{ details.version }}</p>
      <p><strong>Path:</strong> <a :href="`${BACKEND_URL}/registry/${registryName}/${details.relativeDownloadPath}`" target="_blank">{{ details.relativeDownloadPath }}</a></p>
      <p v-if="details.contentType"><strong>Content type:</strong> {{ details.contentType }}</p>
      <p v-if="details.creationDate"><strong>Création date:</strong> {{ details.creationDate }}</p>
      <p v-if="details.updateDate"><strong>Update date:</strong> {{ details.updateDate }}</p>
      <p v-if="details.uploader"><strong>Uploader:</strong> {{ details.uploader }}</p>
      <p v-if="details.sourceRegistryName"><strong>Source registry:</strong> {{ details.sourceRegistryName }}</p>
    </div>
  </div>
</template>

<script>
import { onMounted, ref, inject, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import router from '@/router';
import Breadcrumb from '@/components/Breadcrumb.vue';
import ErrorBanner from '@/components/ErrorBanner.vue';
import TreeNode from '@/components/TreeNode.vue';
import { apiFetch } from '@/services/api';

export default {
  props: {
    registryName: String
  },
  components: { Breadcrumb, ErrorBanner, TreeNode },
  setup(props) {
    const BACKEND_URL = inject('BACKEND_URL');
    const treeData = ref([]);
    const selectedNode = ref(null);
    const details = ref({});
    const error = ref('');
    const breadcrumbPath = ref([]);
    const route = useRoute();
    const treeRefs = ref({});

    function registerRef(element) {
      if (element) {
        const resource = element.node;
        treeRefs.value[resource.path] = element;
      }
    }

    async function fetchCatchError(uri, elementName) {
      try {
        const response = await apiFetch(uri);
        if (!response.ok) {
          error.value = "Error fetching " + elementName + "!";
          return null;
        }
        return await response.json();
      } catch (error) {
        console.error("Cannot fetch " + elementName, error);
        error.value = "Error fetching " + elementName + "!";
      }
    }

    async function fetchResources(path = '') {
      const encodedPath = encodeURIComponent(path);
      return await fetchCatchError(`/registries/${props.registryName}/resources/${encodedPath}`,
                                   "resources");
    }

    async function fetchComponent(node) {
      const encodedGav = encodeURIComponent(node.componentGav);
      return await fetchCatchError(`/registries/${node.sourceRegistryName}/components/${encodedGav}`,
                                   "component");
    }

    async function fetchFile(node) {
      const encodedPath = encodeURIComponent(node.filePath);
      return await fetchCatchError(`/registries/${node.sourceRegistryName}/files/${encodedPath}`,
                                   "file");
    }

    async function loadChildren(node) {
      if (!node.childrenLoaded) {
        node.children = await fetchResources(node.path);
        node.childrenLoaded = true;
      }
    }

    async function selectNode(node) {
      selectedNode.value = node;
      router.replace({ query: { selectedNode: node.path } });

      if (node.relativeDownloadPath || node.componentGav || node.filePath) {
        var component = {};
        if (node.componentGav) {
          component = await fetchComponent(node);
        }

        var file = {};
        if (node.filePath) {
          file = await fetchFile(node);
        }

        details.value = { ...node, ...component, ...file };
      }
      else {
        details.value = node;
      }
    }

    onMounted(async () => {
      breadcrumbPath.value = [
        {link: "/registries", label: "Registries"},
        {link: "/registries/" + props.registryName, label: props.registryName}
      ];
      treeData.value = await fetchResources();
      await nextTick();

      const nodePath = route.query.selectedNode;
      if (nodePath) {
        const parts = nodePath.split('/');
        let currentPath = '';
        for (const part of parts) {
          currentPath = currentPath ? `${currentPath}/${part}` : part;
          const element = treeRefs.value[currentPath];
          await loadChildren(element.node);
          if (element.node.type === 'directory') {
            await element.toggle();
            await nextTick();
            await new Promise(r => window.requestAnimationFrame(r));
          }
          else {
            selectNode(element.node);
          }
        }
      }
    });

    return {
      BACKEND_URL,
      treeData,
      selectedNode,
      details,
      error,
      breadcrumbPath,
      registerRef,
      loadChildren,
      selectNode
    };
  }
};
</script>
