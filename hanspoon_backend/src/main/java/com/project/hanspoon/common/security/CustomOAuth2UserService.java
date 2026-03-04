package com.project.hanspoon.common.security;

import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.common.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // OAuth2 제공자 정보
        String provider = userRequest.getClientRegistration().getRegistrationId();  // google, naver, kakao
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // 제공자별 사용자 정보 추출
        OAuth2UserInfo userInfo = extractUserInfo(provider, attributes);
        
        log.info("OAuth2 로그인: provider={}, email={}, name={}", provider, userInfo.email(), userInfo.name());
        
        // 기존 사용자 조회 또는 신규 가입
        User user = findOrCreateUser(provider, userInfo);
        
        return new CustomUserDetails(user, attributes);
    }

    /**
     * 제공자별 사용자 정보 추출
     */
    private OAuth2UserInfo extractUserInfo(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "google" -> extractGoogleUserInfo(attributes);
            case "naver" -> extractNaverUserInfo(attributes);
            case "kakao" -> extractKakaoUserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("지원하지 않는 로그인 제공자입니다: " + provider);
        };
    }

    private OAuth2UserInfo extractGoogleUserInfo(Map<String, Object> attributes) {
        return new OAuth2UserInfo(
            (String) attributes.get("sub"),
            (String) attributes.get("email"),
            (String) attributes.get("name")
        );
    }

    @SuppressWarnings("unchecked")
    private OAuth2UserInfo extractNaverUserInfo(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return new OAuth2UserInfo(
            (String) response.get("id"),
            (String) response.get("email"),
            (String) response.get("name")
        );
    }

    @SuppressWarnings("unchecked")
    private OAuth2UserInfo extractKakaoUserInfo(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String kakaoId = String.valueOf(attributes.get("id"));
        
        // 이메일 권한이 없을 경우 카카오ID 기반 이메일 생성
        String email = kakaoAccount.get("email") != null 
            ? (String) kakaoAccount.get("email") 
            : "kakao_" + kakaoId + "@kakao.local";
            
        return new OAuth2UserInfo(
            kakaoId,
            email,
            (String) profile.get("nickname")
        );
    }

    /**
     * 기존 사용자 조회 또는 신규 회원 생성
     */
    private User findOrCreateUser(String provider, OAuth2UserInfo userInfo) {
        // 1. provider + providerId로 먼저 조회
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, userInfo.providerId());
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // 2. 이메일로 기존 회원 조회 (일반 가입 후 소셜 로그인 연동 케이스)
        Optional<User> userByEmail = userRepository.findByEmail(userInfo.email());
        if (userByEmail.isPresent()) {
            User user = userByEmail.get();
            // 소셜 로그인 정보 연동
            user.setProvider(provider);
            user.setProviderId(userInfo.providerId());
            return userRepository.save(user);
        }

        // 3. 신규 회원 생성
        User newUser = User.builder()
                .email(userInfo.email())
                .userName(userInfo.name() != null ? userInfo.name() : "사용자")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))  // 임의 비밀번호
                .provider(provider)
                .providerId(userInfo.providerId())
                .build();

        return userRepository.save(newUser);
    }

    /**
     * OAuth2 사용자 정보 레코드
     */
    private record OAuth2UserInfo(String providerId, String email, String name) {}
}
