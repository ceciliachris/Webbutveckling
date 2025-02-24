package com.example.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

        @GetMapping("/github/url")
        public String getGithubUrl() {
            return "http://localhost:8080/oauth2/authorization/github";
        }
    }
