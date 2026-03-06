// src/api/http.js
import { getAccessToken, clearAuth } from "../utils/authStorage";

function normalizeApiPath(path) {
  const normalizedPath = String(path || "").trim();
  if (!normalizedPath) return "";
  if (/^(?:https?:)?\/\//i.test(normalizedPath)) return normalizedPath;

  const withLeadingSlash = normalizedPath.startsWith("/") ? normalizedPath : `/${normalizedPath}`;
  const normalizedBase = String(import.meta.env.VITE_API_BASE_URL || "").trim().replace(/\/+$/, "");

  // same-origin 프록시에서 base가 /api 인데 path도 /api/... 로 오면 /api/api/... 중복이 생깁니다.
  if (normalizedBase === "/api" && withLeadingSlash.startsWith("/api/")) {
    return withLeadingSlash.slice(4);
  }

  return withLeadingSlash;
}

function resolveFetchBaseUrl() {
  const rawBaseUrl = String(import.meta.env.VITE_API_BASE_URL || "").trim();
  if (!rawBaseUrl) {
    return typeof window !== "undefined" ? window.location.origin : "";
  }

  // new URL(path, base)는 base가 절대 URL이어야 하므로, 상대 base(/api)는 현재 origin 기준으로 보정합니다.
  if (/^(?:https?:)?\/\//i.test(rawBaseUrl)) {
    return rawBaseUrl.replace(/\/+$/, "");
  }

  if (typeof window !== "undefined") {
    return new URL(rawBaseUrl.replace(/^\//, ""), `${window.location.origin}/`).href.replace(/\/+$/, "");
  }

  return rawBaseUrl.replace(/\/+$/, "");
}

function buildUrl(path, params) {
  const normalizedPath = normalizeApiPath(path);

  if (!params) {
    const baseUrl = String(import.meta.env.VITE_API_BASE_URL || "").trim();

    // 상대 경로 기반 프록시를 쓸 때는 /api/api 중복이 생기지 않도록 정규화한 path를 붙입니다.
    if (baseUrl && normalizedPath.startsWith("/")) {
      const normalizedBaseUrl = baseUrl.replace(/\/+$/, "");
      return `${normalizedBaseUrl}${normalizedPath}`;
    }
    return normalizedPath;
  }

  const baseUrl = resolveFetchBaseUrl();
  const url = new URL(normalizedPath, baseUrl);

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

  // JSON 본문일 때만 Content-Type을 자동 설정합니다.
  if (body && !isFormData && !h.has("Content-Type")) {
    h.set("Content-Type", "application/json");
  }

  // params를 URL에 붙입니다.
  const url = buildUrl(path, params);

  const res = await fetch(url, {
    method,
    headers: h,
    body: body ? (isFormData ? body : JSON.stringify(body)) : undefined,
  });

  // 401이면 토큰 만료/로그아웃 처리를 합니다.
  if (res.status === 401) {
    clearAuth();
  }

  const contentType = res.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json");

  const payload = isJson
    ? await res.json().catch(() => null)
    : await res.text().catch(() => null);

  // ApiResponse 래퍼면 data만 꺼내기
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
  // opt를 그대로 받도록 유지
  get: (path, opt) => request(path, opt),
  post: (path, body, opt) => request(path, { method: "POST", body, ...opt }),
  put: (path, body, opt) => request(path, { method: "PUT", body, ...opt }),
  patch: (path, body, opt) => request(path, { method: "PATCH", body, ...opt }),
  del: (path, opt) => request(path, { method: "DELETE", ...opt }),
};
