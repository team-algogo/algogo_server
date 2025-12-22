package com.ssafy.algogo.problem.repository;

import com.ssafy.algogo.problem.entity.Problem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query("""
            SELECT p
            FROM Problem p
            WHERE p.title LIKE CONCAT('%', :keyword, '%') ESCAPE '\\'
               OR p.problemNo LIKE CONCAT('%', :keyword, '%') ESCAPE '\\'
        """)
    List<Problem> searchByKeyword(@Param("keyword") String keyword);
}
