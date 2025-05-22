export async function getBackendUrl() {
  var backendUrl = import.meta.env.VITE_BACKEND_URL;

  if (!backendUrl) {
    const response = await fetch('/env');
    if (response.ok) {
      try {
        const data = await response.json();
        backendUrl = data['BACKEND_URL'] ?? null;
      } catch (err) {
        // ignore
      }
    }
  }

  if (!backendUrl) {
    console.error('Backend URL not defined');
  }

  return backendUrl;
}

export async function getBackendApiUrl() {
  return await getBackendUrl() + "/api/v1";
}
