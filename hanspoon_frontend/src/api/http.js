// src/api/http.js
import { getAccessToken, clearAuth } from "../utils/authStorage";

function isAbsoluteUrl(value) {
  return /^(?:https?:)?\/\//i.test(String(value || "").trim());
}

function normalizeRequestPath(path) {
  const normalizedPath = String(path || "").trim();
  if (!normalizedPath) return "";
  if (isAbsoluteUrl(normalizedPath)) return normalizedPath;
  return normalizedPath.startsWith("/") ? normalizedPath : `/${normalizedPath}`;
}

function getConfiguredBaseUrl() {
  return String(import.meta.env.VITE_API_BASE_URL || "").trim().replace(/\/+$/, "");
}

function shouldStripApiPrefix(baseUrl, requestPath) {
  return !!baseUrl && /\/api$/i.test(baseUrl) && requestPath.startsWith("/api/");
}

function buildRelativePath(baseUrl, requestPath) {
  if (shouldStripApiPrefix(baseUrl, requestPath)) {
    return requestPath.replace(/^\/api\//, "");
  }
  return requestPath.replace(/^\//, "");
}

function buildStringUrl(path) {
  const requestPath = normalizeRequestPath(path);
  if (!requestPath || isAbsoluteUrl(requestPath)) return requestPath;

  const baseUrl = getConfiguredBaseUrl();
  if (!baseUrl) {
    return requestPath;
  }

  // same-origin ?꾨줉?쒖뿉??base媛 /api ?닿퀬 path媛 /api/... ?대㈃ /api/api/... 以묐났??留됱뒿?덈떎.
  if (shouldStripApiPrefix(baseUrl, requestPath)) {
    return `${baseUrl}${requestPath.slice(4)}`;
  }

  return `${baseUrl}${requestPath}`;
}

function buildAbsoluteBaseUrl(baseUrl) {
  if (!baseUrl) {
    return typeof window !== "undefined" ? `${window.location.origin}/` : "";
  }

  if (isAbsoluteUrl(baseUrl)) {
    return `${baseUrl}/`.replace(/([^:]\/)\/+/g, "$1");
  }

  if (typeof window === "undefined") {
    return `${baseUrl}/`.replace(/\/+/g, "/");
  }

  const normalizedBasePath = baseUrl.replace(/^\/+/, "");
  return new URL(`${normalizedBasePath}/`, `${window.location.origin}/`).href;
}

function buildUrl(path, params) {
  const requestPath = normalizeRequestPath(path);
  if (!requestPath || isAbsoluteUrl(requestPath)) {
    return requestPath;
  }

  if (!params) {
    return buildStringUrl(requestPath);
  }

  const baseUrl = getConfiguredBaseUrl();
  const absoluteBaseUrl = buildAbsoluteBaseUrl(baseUrl);
  const relativePath = buildRelativePath(baseUrl, requestPath);
  const url = baseUrl
    ? new URL(relativePath, absoluteBaseUrl)
    : new URL(requestPath, absoluteBaseUrl);

  Object.entries(params).forEach(([k, v]) => {
    if (v === undefined || v === null || v === "") return;

    if (Array.isArray(v)) {
      url.searchParams.delete(k);
      v.forEach((vv) => {
        if (vv === undefined || vv === null || vv === "") return;
        url.searchParams.append(k, String(vv));
      });
      return;
    }

    url.searchParams.set(k, String(v));
  });

  return url.href;
}

async function request(path, { method = "GET", params, body, headers } = {}) {
  const token = getAccessToken();

  const h = new Headers(headers || {});
  if (token) h.set("Authorization", `Bearer ${token}`);

  const isFormData = body instanceof FormData;

  // JSON 蹂몃Ц???뚮쭔 Content-Type???먮룞 ?ㅼ젙?⑸땲??
  if (body && !isFormData && !h.has("Content-Type")) {
    h.set("Content-Type", "application/json");
  }

  // params瑜?URL??遺숈엯?덈떎.
  const url = buildUrl(path, params);

  const res = await fetch(url, {
    method,
    headers: h,
    body: body ? (isFormData ? body : JSON.stringify(body)) : undefined,
  });

  // 401?대㈃ ?좏겙 留뚮즺/濡쒓렇?꾩썐 泥섎━瑜??⑸땲??
  if (res.status === 401) {
    clearAuth();
  }

  const contentType = res.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json");

  const payload = isJson
    ? await res.json().catch(() => null)
    : await res.text().catch(() => null);

  // ApiResponse ?섑띁硫?data留?爰쇰깄?덈떎.
  const data =
    payload && typeof payload === "object" && "data" in payload ? payload.data : payload;
  const message =
    payload && typeof payload === "object" && "message" in payload ? payload.message : null;

  if (!res.ok) {
    const err = new Error(message || "API Error");
    err.status = res.status;
    err.payload = payload;
    throw err;
  }

  return { data, message, raw: payload };
}

export function toErrorMessage(err) {
  if (!err) return "Unknown error";
  if (typeof err === "string") return err;
  if (err.message) return err.message;
  if (err.error) return err.error;
  if (err.errors && Array.isArray(err.errors)) return err.errors.join(", ");
  return JSON.stringify(err);
}

export const http = {
  // opt瑜?洹몃?濡?諛쏅룄濡??좎??⑸땲??
  get: (path, opt) => request(path, opt),
  post: (path, body, opt) => request(path, { method: "POST", body, ...opt }),
  put: (path, body, opt) => request(path, { method: "PUT", body, ...opt }),
  patch: (path, body, opt) => request(path, { method: "PATCH", body, ...opt }),
  del: (path, opt) => request(path, { method: "DELETE", ...opt }),
};
