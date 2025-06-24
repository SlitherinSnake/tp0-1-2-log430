package com.log430.tp3.security.services;

import com.log430.tp3.model.User;
import com.log430.tp3.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service chargé de récupérer un utilisateur (par son login)
 * lorsque Spring Security en a besoin.
 * C’est le « pont » entre la base de données et le système d’authentification.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    /**
     * Cherche l’utilisateur en base grâce au UserRepository.
     * S’il existe, on le transforme en UserDetailsImpl.
     * S’il n’existe pas, on lève une exception comprise par Spring Security.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // findByUsername renvoie Optional<User>; orElseThrow lève l’exception si vide
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        // Conversion en objet accepté par Spring Security
        return UserDetailsImpl.build(user);
    }
} 