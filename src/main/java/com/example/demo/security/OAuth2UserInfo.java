package com.example.demo.security;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuth2UserInfo {
    private String id;
    private String name;
    private String email;
    private String imageUrl;
    private String provider;
}
