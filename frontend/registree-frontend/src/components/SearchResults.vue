<template>
  <table>
    <thead>
      <tr>
        <th>Group</th>
        <th>Name</th>
        <th>Version</th>
        <th>Last Update</th>
        <th>Registry</th>
        <th>Format</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="item in results" :key="item.id">
        <td>{{ item.group }}</td>
        <td>{{ item.name }}</td>
        <td>{{ item.version }}</td>
        <td>{{ item.updateDate }}</td>
        <td>{{ item.registryName }}</td>
        <td>{{ item.registryFormat }}</td>
      </tr>
    </tbody>
  </table>
</template>

<script>
export default {
  props: {
    results: {
      type: Array,
      required: true
    }
  },
  setup() {
  }
}
</script>
