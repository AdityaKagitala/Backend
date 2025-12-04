package com.example.urlshortener.repository;

import com.example.urlshortener.model.User;
import com.example.urlshortener.model.UserUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserUsageRepository  extends JpaRepository<UserUsage, Long> {
    Optional<UserUsage> findByUser(User user);
    List<UserUsage> findAll();
}
