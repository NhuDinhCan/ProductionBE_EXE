package com.example.production.repositpry;


import com.example.production.entity.InvalidtedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidtedTokenRepository extends JpaRepository<InvalidtedToken, String> {
}
