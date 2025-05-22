<template>
  <div class="terminal-container">
    <div class="output" ref="outputRef">
      <div v-for="(entry, index) in history" :key="index">
        <div><span class="prompt">$</span> {{ entry.command }}</div>
        <div class="response">{{ entry.response }}</div>
      </div>
    </div>

    <form class="input-line" @submit.prevent="handleCommand">
      <span class="prompt">$</span>
      <input
        v-model="currentCommand"
        type="text"
        class="command-input"
        autofocus
        autocomplete="off"
        @keydown="onKeyDown"
      />
    </form>
  </div>
</template>

<script>
import { ref, nextTick, onMounted } from 'vue';
import { apiFetch } from '@/services/api';
import { parseCommand } from '@/utils/command-parser';

export default {
  setup() {
    const currentCommand = ref('');
    const history = ref([]);
    const historyIndex = ref(null);
    const outputRef = ref(null);

    const handleCommand = async () => {
      const cmd = currentCommand.value.trim();
      if (!cmd) {
        return;
      }

      history.value.push({ command: cmd, response: '...' });
      historyIndex.value = null;
      const index = history.value.length - 1;
      currentCommand.value = '';

      try {
        const response = await sendCommand(cmd);
        history.value[index].response = response;
      } catch (err) {
        history.value[index].response = `Error: ${err.message}`;
      }

      await nextTick();
      outputRef.value.scrollTop = outputRef.value.scrollHeight;
    }

    const sendCommand = async (cmd) => {
      const tab = parseCommand(cmd);
      const command = tab[0];
      const args = tab.splice(1);
      const res = await apiFetch('/commands', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ command: command, arguments: args })
      })
      if (!res.ok) {
        throw new Error('Failed to send command');
      }
      const data = await res.json();
      return data.output;
    }

    const onKeyDown = (e) => {
      if (e.key === 'ArrowUp') {
        if (history.value.length === 0) {
          return;
        }
        if (historyIndex.value === null) {
          historyIndex.value = history.value.length - 1;
        } else if (historyIndex.value > 0) {
          historyIndex.value--;
        }
        currentCommand.value = history.value[historyIndex.value].command;
        e.preventDefault();
      } else if (e.key === 'ArrowDown') {
        if (historyIndex.value === null) {
          return;
        }
        if (historyIndex.value < history.value.length - 1) {
          historyIndex.value++;
          currentCommand.value = history.value[historyIndex.value].command;
        } else {
          historyIndex.value = null;
          currentCommand.value = '';
        }
        e.preventDefault();
      }
    }

    onMounted(() => {
      outputRef.value.scrollTop = outputRef.value.scrollHeight;
    });

    return {
      handleCommand,
      onKeyDown,
      currentCommand,
      history,
      outputRef
    }
  }
}
</script>
