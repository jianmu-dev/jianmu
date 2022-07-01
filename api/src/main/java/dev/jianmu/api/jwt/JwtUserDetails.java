package dev.jianmu.api.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.jianmu.api.util.JsonUtil;
import dev.jianmu.user.aggregate.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * @author Ethan Liu
 * @class JwtUserDetails
 * @description JwtUserDetails
 * @create 2021-05-17 22:06
 */
public class JwtUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;

    private User user;

    @JsonIgnore
    private String password;

    public JwtUserDetails(User user, String password) {
        this.user = user;
        this.password = password;
    }

    public static JwtUserDetails build(User user, String password) {
        return new JwtUserDetails(
                user,
                password
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return JsonUtil.jsonToString(user);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getId() {
        return user.getId();
    }

}
