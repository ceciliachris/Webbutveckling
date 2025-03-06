package com.example.demo.security;

import com.example.demo.user.UserEntity;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    /**
     * Hämtar användarens detaljer från OAuth2-provider (GitHub) och returnerar en OAuth2User.
     * Metod som kallas när användaren autentiseras via OAuth2.
     *
     * @param userRequest Innehåller begäran om OAuth2-inloggning och information om användaren.
     * @return OAuth2User objekt som innehåller användarens attribut.
     * @throws OAuth2AuthenticationException Om autentisering misslyckas.
     */

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oauth2User = super.loadUser(userRequest);
        System.out.println("OAuth2 Authentication Request Triggered");
        System.out.println("User Attributes: " + oauth2User.getAttributes());

        return processOAuth2User(oauth2User);
    }

    /**
     * Processerar om användarens detaljer som hämtas från OAuth2-provider.
     * Om användaren inte redan finns i systemet skapas en ny användare.
     *
     * @param oauth2User Användarens attribut från OAuth2-provider.
     * @return En OAuth2User med användarens detaljer.
     */

    private OAuth2User processOAuth2User(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        String githubId = attributes.get("id").toString();

        UserEntity user = userRepository.findByProviderAndProviderId("github", githubId)
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setName(attributes.get("login").toString());
                    newUser.setProvider("github");
                    newUser.setProviderId(githubId);
                    newUser.setPassword(UUID.randomUUID().toString());
                    return userRepository.save(newUser);
                });

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        return new DefaultOAuth2User(authorities, attributes, "login");
    }
}
