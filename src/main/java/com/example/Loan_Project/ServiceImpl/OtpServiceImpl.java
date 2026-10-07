package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Repository.CustomerRepository;
import com.example.Loan_Project.Service.EmailService;
import com.example.Loan_Project.Service.OtpService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpServiceImpl implements OtpService {

    private final RedisTemplate<String , String> redisTemplate;
    private final EmailService emailService;
    private final CustomerRepository customerRepository;

    public OtpServiceImpl(RedisTemplate<String, String> redisTemplate, EmailService emailService, CustomerRepository customerRepository) {
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
        this.customerRepository = customerRepository;
    }

    @Override
    public void generateAndStoreOtp(String email) {
        String otp = String.valueOf(
                100000 + new Random().nextInt(900000)
        );

        String key = "email_otp:" + email;

        redisTemplate.opsForValue().set(
                key,
                otp,
                5,
                TimeUnit.MINUTES
        );
        emailService.sendOtp(email,otp);
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        String key = "email_otp:" + email;

        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            return false;
        }

        if (!storedOtp.equals(otp)) {
            return false;
        }

        Optional<Customer> customerOptional =
                customerRepository.findByEmail(email);

        if (customerOptional.isEmpty()) {
            return false;
        }

        Customer customer = customerOptional.get();

        customer.setIsEmailVerified(true);

        customerRepository.save(customer);

        redisTemplate.delete(key);

        return true;
    }
}
