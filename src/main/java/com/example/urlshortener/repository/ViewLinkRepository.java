package com.example.urlshortener.repository;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.model.ViewLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ViewLinkRepository extends JpaRepository<ViewLink, Long> {

    List<ViewLink> findByUrlMapping(UrlMapping mapping);
}
