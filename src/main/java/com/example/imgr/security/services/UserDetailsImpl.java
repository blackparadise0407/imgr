package com.example.imgr.security.services;

import com.example.imgr.entities.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private Long id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;

    public static UserDetailsImpl build(UserEntity userEntity) {
//        List<GrantedAuthority> authorities  = userEntity.getRoles
    return new UserDetailsImpl(userEntity.getId(), userEntity.getUsername(), userEntity.getPassword(), userEntity.getEmail());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
