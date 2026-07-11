package com.vvs.spendwise_api.auth;

import com.vvs.spendwise_api.auth.dto.AuthResponse;
import com.vvs.spendwise_api.auth.dto.LoginRequest;
import com.vvs.spendwise_api.auth.dto.RegisterRequest;
import com.vvs.spendwise_api.category.Category;
import com.vvs.spendwise_api.category.CategoryRepository;
import com.vvs.spendwise_api.category.DefaultCategories;
import com.vvs.spendwise_api.common.exception.EmailAlreadyExistsException;
import com.vvs.spendwise_api.security.CustomUserDetailsService;
import com.vvs.spendwise_api.security.JwtService;
import com.vvs.spendwise_api.user.Role;
import com.vvs.spendwise_api.user.User;
import com.vvs.spendwise_api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(Role.USER)
                .build();
        userRepository.save(user);
        seedDefaultCategories(user);

        return new AuthResponse(jwtService.generateToken(CustomUserDetailsService.toUserDetails(user)));
    }

    private void seedDefaultCategories(User user) {
        List<Category> categories = DefaultCategories.NAMES.stream()
                .map(name -> Category.builder().user(user).name(name).build())
                .toList();
        categoryRepository.saveAll(categories);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email()).orElseThrow();

        return new AuthResponse(jwtService.generateToken(CustomUserDetailsService.toUserDetails(user)));
    }
}
