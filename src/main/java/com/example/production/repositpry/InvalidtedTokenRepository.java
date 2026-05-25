package com.example.production.repositpry;


import com.example.production.entity.InvalidtedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Repository
public interface InvalidtedTokenRepository extends JpaRepository<InvalidtedToken, String> {

    @Transactional
    long deleteByExpirationTimeBefore(Date expirationTime);
}
