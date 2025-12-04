package com.example.urlshortener.repository;

import com.example.urlshortener.model.CustomDomain;
import com.example.urlshortener.model.User;
import com.example.urlshortener.model.DomainStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomDomainRepository extends JpaRepository<CustomDomain, Long> {

    Optional<CustomDomain> findByDomain(String domain);

    boolean existsByDomain(String domain);

    List<CustomDomain> findAllByUser(User user);

    List<CustomDomain> findAllByStatus(DomainStatus status);
}