package in.chester.moneymanager.service;

import in.chester.moneymanager.entity.ProfileEntity;
import in.chester.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;


// Responsible for loading the user details from the database based on the email.
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final ProfileRepository profileRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
         ProfileEntity existingProfile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
         return User.builder()
                 .username(existingProfile.getEmail())
                 .password(existingProfile.getPassword())
                 .authorities(Collections.emptyList())
                 .build();
    }
}
