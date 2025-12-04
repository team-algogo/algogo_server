package com.ssafy.algogo.submission.repository.query.impl;

import static com.ssafy.algogo.submission.entity.QAlgorithm.algorithm;
import static com.ssafy.algogo.submission.entity.QAlgorithmKeyword.algorithmKeyword;
import static com.ssafy.algogo.submission.entity.QSubmissionAlgorithm.submissionAlgorithm;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.algogo.submission.entity.Algorithm;
import com.ssafy.algogo.submission.repository.query.AlgorithmQueryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AlgorithmQueryRepositoryImpl implements AlgorithmQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Algorithm> findByKeyword(String keyword) {
        return jpaQueryFactory
            .select(algorithm)
            .from(algorithmKeyword)
            .join(algorithmKeyword.algorithm, algorithm)
            .where(algorithmKeyword.keyword.contains(keyword))
            .fetch();
    }

    @Override
    public List<Algorithm> findAllAlgorithmsBySubmissionId(Long submissionId) {
        return jpaQueryFactory
            .select(algorithm)
            .from(submissionAlgorithm)
            .join(submissionAlgorithm.algorithm, algorithm)
            .where(submissionAlgorithm.submission.id.eq(submissionId))
            .fetch();
    }


}
