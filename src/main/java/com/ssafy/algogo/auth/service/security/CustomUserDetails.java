package com.ssafy.algogo.auth.service.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long userId; // pk
    private final String username; // 구현해야함 -> 실제로 username을 들여보내는게 맞나? nickname or email
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

}
