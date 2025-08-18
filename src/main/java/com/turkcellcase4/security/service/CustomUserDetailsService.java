package com.turkcellcase4.security.service;

import com.turkcellcase4.user.model.User;
import com.turkcellcase4.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String msisdn) throws UsernameNotFoundException {
        User user = userRepository.findByMsisdn(msisdn)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with msisdn: " + msisdn));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getMsisdn())
                .password("") // No password for this case
                .authorities(new ArrayList<>())
                .build();
    }
}
