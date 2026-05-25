package com.example.production.repositpry;

import com.example.production.entity.UserHashRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserHashRoleRepository extends JpaRepository<UserHashRole, Long> {
    List<UserHashRole> findByUserId(Long userId);

    Optional<UserHashRole> findByUserIdAndRoleId(Long userId, Long roleId);

    void deleteByUserId(Long userId);
}
