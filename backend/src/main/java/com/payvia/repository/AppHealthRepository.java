package com.payvia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payvia.entity.AppHealth;

@Repository
public interface AppHealthRepository extends JpaRepository<AppHealth, Long> {
}
