import { getBackendApiUrl, getBackendUrl } from "@/utils/backend-api.js";

export const BACKEND_URL = await getBackendUrl();
export const API_URL = await getBackendApiUrl();
