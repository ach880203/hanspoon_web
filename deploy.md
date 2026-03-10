# 변경 목표

`https://hanspoon.store` 기준으로 카카오 로그인 시작, 카카오 콜백, 로그인 성공 후 프론트 복귀, nginx 프록시, GitHub Actions 시크릿을 한 경로로 맞춘다.

# 핵심 변경 요약

- 백엔드 기본 프론트 주소를 `https://hanspoon.store`로 맞췄다.
- CORS 허용 도메인을 운영 도메인까지 포함하도록 확장했다.
- nginx가 OAuth2와 API 요청에서 `X-Forwarded-Proto`, `X-Forwarded-Host`를 백엔드로 넘기도록 수정했다.
- 배포 워크플로가 nginx 설정 파일과 보고서를 함께 서버로 올리도록 수정했다.
- GitHub Actions 시크릿을 Terraform으로 관리할 별도 디렉터리를 추가했다.

# 코드/설명

## 1. OAuth2 로그인 성공 후 프론트 복귀 주소

기존 코드

```java
@Value("${FRONTEND_URL:http://localhost:5173}")
private String frontendUrl;
```

변경 코드

```java
@Value("${FRONTEND_URL:https://hanspoon.store}")
private String frontendUrl;
```

설명

- 운영 환경변수가 비어 있어도 기본 복귀 주소가 `https://hanspoon.store`가 되도록 맞췄다.
- 로컬값이 남아 있으면 운영에서 `localhost`로 튀는 문제를 막기 위한 변경이다.

## 2. CORS 허용 도메인

기존 코드

```java
registry.addMapping("/**")
        .allowedOrigins("http://localhost:5173")
        .allowedMethods("GET", "POST", "PUT", "DELETE");
```

변경 코드

```java
List<String> allowedOrigins = new ArrayList<>();
allowedOrigins.add("http://localhost:5173");
allowedOrigins.add("http://127.0.0.1:5173");

String normalizedFrontendUrl = frontendUrl == null ? "" : frontendUrl.trim().replaceAll("/+$", "");
if (!normalizedFrontendUrl.isBlank()) {
    allowedOrigins.add(normalizedFrontendUrl);
    if ("https://hanspoon.store".equalsIgnoreCase(normalizedFrontendUrl)) {
        allowedOrigins.add("https://www.hanspoon.store");
    }
}

registry.addMapping("/**")
        .allowedOrigins(allowedOrigins.toArray(String[]::new))
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
```

설명

- 로컬 개발 주소는 그대로 유지했다.
- 운영 도메인은 `FRONTEND_URL` 값으로 받아서 코드와 시크릿을 같이 맞출 수 있게 했다.
- `PATCH`, `OPTIONS`까지 허용해 브라우저 사전요청에서 막히지 않게 했다.

## 3. nginx 프록시와 전달 헤더

기존 코드

```nginx
server {
  listen 80;
  server_name _;

  location /oauth2/ {
    proxy_pass http://127.0.0.1:8080/oauth2/;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  }
}
```

변경 코드

```nginx
map $http_x_forwarded_proto $forwarded_proto {
  default $http_x_forwarded_proto;
  ""      $scheme;
}

map $http_x_forwarded_host $forwarded_host {
  default $http_x_forwarded_host;
  ""      $host;
}

server {
  listen 80;
  server_name hanspoon.store www.hanspoon.store;

  location /oauth2/ {
    proxy_pass http://127.0.0.1:8080/oauth2/;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $forwarded_proto;
    proxy_set_header X-Forwarded-Host $forwarded_host;
  }
}
```

설명

- ALB가 HTTPS를 종료한 뒤 nginx로 HTTP를 넘기더라도, 원래 요청이 HTTPS였다는 정보를 백엔드가 알 수 있게 했다.
- 이 값이 없으면 스프링이 콜백과 리다이렉트 URL을 `http`로 오인할 수 있다.

## 4. GitHub Actions 시크릿 Terraform 관리

기존 코드

```tf
# GitHub Actions 시크릿을 관리하는 Terraform 코드가 없었다.
```

변경 코드

```tf
resource "github_actions_secret" "managed" {
  for_each = var.actions_secrets

  repository      = var.github_repository
  secret_name     = each.key
  plaintext_value = each.value
}
```

설명

- 도메인 변경처럼 반복해서 바뀔 수 있는 배포 시크릿을 코드로 관리할 수 있게 했다.
- 이번 적용에서는 `FRONTEND_URL`, `VITE_API_BASE_URL`를 `https://hanspoon.store` 기준으로 갱신한다.

# 확인 방법

1. 프론트에서 카카오 로그인 버튼 클릭 시 첫 요청이 `https://hanspoon.store/oauth2/authorization/kakao`로 시작하는지 확인한다.
2. 카카오 인증 후 콜백 주소가 `https://hanspoon.store/login/oauth2/code/kakao`인지 확인한다.
3. 최종 복귀 주소가 `https://hanspoon.store/oauth2/redirect?...`인지 확인한다.
4. 서버에서 `sudo nginx -t`가 성공하고, `curl -I https://hanspoon.store` 응답이 정상인지 확인한다.
5. GitHub Actions 배포 로그에서 nginx 설정 복사 단계가 성공했는지 확인한다.
