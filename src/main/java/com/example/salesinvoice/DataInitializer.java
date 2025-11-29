package com.example.salesinvoice;

import com.example.salesinvoice.entity.User;
import com.example.salesinvoice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // بررسی اینکه کاربر از قبل وجود نداشته باشه
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setEmail("admin@test.com");
            userRepository.save(admin);

            System.out.println("✅ کاربر پیش‌فرض ایجاد شد:");
            System.out.println("   نام کاربری: admin");
            System.out.println("   رمز عبور: 123456");
        }
    }
}