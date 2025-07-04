package com.log430.tp4.security.services;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.log430.tp4.model.User;

/**
 * Adapter entre notre entité User (en base de données)
 * et l’interface UserDetails exigée par Spring Security.
 * Spring utilisera ces informations pour savoir
 * qui est connecté et quels sont ses droits.
 */
public class UserDetailsImpl implements UserDetails {
    
    private static final long serialVersionUID = 1L;

    // Identifiant unique de l’utilisateur
    private Long id;
    // Nom d’utilisateur (login)
    private String username;
    
    // Mot de passe (masqué lors des échanges JSON grâce à @JsonIgnore)
    @JsonIgnore
    private String password;

    // Liste des rôles/permissions (ex. ROLE_ADMIN)
    private Collection<? extends GrantedAuthority> authorities;

    // Constructeur principal (appelé depuis la méthode build)
    public UserDetailsImpl(Long id, String username, String password,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Fabrique un UserDetailsImpl à partir d’un objet User récupéré en base.
     * Pour chaque rôle de l’utilisateur, on crée une GrantedAuthority compréhensible par Spring.
     */
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities);
    }

    // Méthodes imposées par l’interface UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {return id;}

    @Override
    public String getPassword() { return password;}

    @Override
    public String getUsername() {    return username;}

    // Les 4 méthodes suivantes retournent toujours true :
    // on ne gère pas (encore) de compte expiré, verrouillé, etc.
    @Override
    public boolean isAccountNonExpired() {   return true;}

    @Override
    public boolean isAccountNonLocked() {  return true;}

    @Override
    public boolean isCredentialsNonExpired() {  return true;}

    @Override
    public boolean isEnabled() {  return true;}

    // Deux UserDetailsImpl sont égaux s’ils portent le même id
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
} 