package com.ssafy.algogo.submission.service.impl;

import com.ssafy.algogo.submission.dto.response.AlgorithmListResponseDto;
import com.ssafy.algogo.submission.dto.response.AlgorithmResponseDto;
import com.ssafy.algogo.submission.entity.Algorithm;
import com.ssafy.algogo.submission.repository.AlgorithmRepository;
import com.ssafy.algogo.submission.service.AlgorithmService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AlgorithmServiceImpl implements AlgorithmService {

    private final AlgorithmRepository algorithmRepository;

    @Override
    @Transactional(readOnly = true)
    public AlgorithmListResponseDto searchAlgorithmWithKeyword(String keyword) {
        // 키워드에 가장 유사한 알고리즘 찾기
        List<Algorithm> searchedAlgorithms = algorithmRepository.findByKeyword(
            keyword.toLowerCase().trim().replaceAll(" ", ""));
        List<AlgorithmResponseDto> searchedAlgorithmResponses = searchedAlgorithms.stream()
            .map(AlgorithmResponseDto::from).toList();
        return AlgorithmListResponseDto.from(searchedAlgorithmResponses);
    }
}
