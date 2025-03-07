package com.authorization.server.security;

import com.authorization.server.entity.User;
import com.authorization.server.exception.ApiException;
import com.authorization.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated;
import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;

@Component
@RequiredArgsConstructor
public class UserAuthenticationProvider implements AuthenticationProvider {
    private final UserService userService;
    private final BCryptPasswordEncoder encoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            var user = userService.getUserByEmail((String) authentication.getPrincipal());
            validateUser.accept(user);
            if (encoder.matches((String) authentication.getCredentials(), user.getPassword())) {
                return authenticated(user, "[PROTECTED]", commaSeparatedStringToAuthorityList(user.getRole() + "," + user.getAuthorities()));
            } else throw new BadCredentialsException("Incorrect username/password. Please try again");
        } catch (BadCredentialsException | ApiException | LockedException | CredentialsExpiredException |
                 DisabledException e) {
            throw new ApiException(e.getMessage());
        } catch (Exception e) {
            throw new ApiException("Unable to authenticate. Please try again.");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }

    private final Consumer<User> validateUser = user -> {
        if (!user.isAccountNonLocked() || user.getLoginAttempts() >= 5) {
            throw new LockedException(String.format(user.getLoginAttempts() > 0 ? "Account currently locked %s failed login attempts" : "Account currently locked", user.getLoginAttempts()));
        }
        if (!user.isEnabled()) {
            throw new DisabledException("Your account is currently disabled");
        }
        if (!user.isAccountNonExpired()) {
            throw new DisabledException("Your account has expired. Please contact support team");
        }
    };
}
