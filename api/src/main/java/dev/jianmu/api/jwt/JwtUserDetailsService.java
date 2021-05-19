package dev.jianmu.api.jwt;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @class: JwtUserDetailsService
 * @description: JwtUserDetailsService
 * @author: Ethan Liu
 * @create: 2021-05-17 21:10
 **/
@Service
public class JwtUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public JwtUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("未找到该用户名"));
        return JwtUserDetails.build(user);
    }
}
