package com.example.production.repositpry;

import com.example.production.entity.LearningResource;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningResourceRepository extends JpaRepository<LearningResource, Long> {

    boolean existsByTitleIgnoreCase(String title);

    @EntityGraph(attributePaths = {"career"})
    Optional<LearningResource> findByIdAndStatus(Long id, String status);

    @Query("""
            select r from LearningResource r
            join fetch r.career c
            where r.status = :status
              and (:careerId is null or c.id = :careerId)
              and (:type is null or r.resourceType = :type)
              and (
                    :keyword is null
                    or lower(r.title) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(r.description, '')) like lower(concat('%', :keyword, '%'))
                    or lower(c.name) like lower(concat('%', :keyword, '%'))
              )
            order by r.createdAt desc, r.id desc
            """)
    List<LearningResource> search(
            @Param("status") String status,
            @Param("careerId") Long careerId,
            @Param("type") String type,
            @Param("keyword") String keyword);
}
