package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.OfficerLoginRequest;
import com.example.Loan_Project.Entity.User;
import com.example.Loan_Project.Exception.InvalidCredentialsException;
import com.example.Loan_Project.Repository.UserRepository;
import com.example.Loan_Project.Service.OfficerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class OfficerServiceImpl implements OfficerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OfficerServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User login(OfficerLoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password"
                        ));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        if (!user.getRole().getRoleName().equals("Loan Officer")) {

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        return user;
    }
}