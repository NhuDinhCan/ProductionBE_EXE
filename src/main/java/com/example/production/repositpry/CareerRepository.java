package com.example.production.repositpry;


import com.example.production.entity.Career;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CareerRepository extends JpaRepository<Career,Long> {

    Optional<Career> findFirstByNameIgnoreCase(String name);
}
