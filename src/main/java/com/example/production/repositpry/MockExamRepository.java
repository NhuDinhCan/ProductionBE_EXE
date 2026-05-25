package com.example.production.repositpry;

import com.example.production.entity.MockExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockExamRepository extends JpaRepository<MockExam, Long> {

    @Query("""
            select e from MockExam e
            join fetch e.subject s
            left join fetch e.combination c
            where (:subjectId is null or s.id = :subjectId)
              and (
                    :combinationCode is null
                    or upper(c.code) = upper(:combinationCode)
                    or exists (
                        select 1 from ExamCombinationSubject ecs
                        where ecs.subject.id = s.id
                          and upper(ecs.combination.code) = upper(:combinationCode)
                    )
                  )
              and (:difficulty is null or upper(e.difficulty) = upper(:difficulty))
              and (:year is null or e.year = :year)
              and (:status is null or upper(e.status) = upper(:status))
              and (
                    :keyword is null
                    or lower(e.title) like lower(concat('%', :keyword, '%'))
                    or lower(s.name) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(c.name, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(c.code, '')) like lower(concat('%', :keyword, '%'))
                  )
            order by e.year desc, e.id desc
            """)
    List<MockExam> search(
            @Param("subjectId") Long subjectId,
            @Param("combinationCode") String combinationCode,
            @Param("difficulty") String difficulty,
            @Param("year") Integer year,
            @Param("status") String status,
            @Param("keyword") String keyword);
}
