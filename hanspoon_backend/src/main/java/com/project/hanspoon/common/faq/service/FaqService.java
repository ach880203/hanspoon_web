package com.project.hanspoon.common.faq.service;

import com.project.hanspoon.common.faq.dto.FaqDto;
import com.project.hanspoon.common.faq.entity.Faq;
import com.project.hanspoon.common.faq.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FaqService {

    private final FaqRepository faqRepository;

    @Transactional(readOnly = true)
    public List<FaqDto> getAllFaqList() {
        return faqRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FaqDto> getFaqListByCategory(String category) {
        return faqRepository.findByCategory(category).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<FaqDto> getFaqList(Pageable pageable) {
        return faqRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public FaqDto getFaq(Long faqId) {
        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new IllegalArgumentException("FAQ를 찾을 수 없습니다."));
        return toDto(faq);
    }

    public FaqDto createFaq(FaqDto dto) {
        Faq faq = Faq.builder()
                .category(dto.getCategory())
                .question(dto.getQuestion())
                .answer(dto.getAnswer())
                .build();
        return toDto(faqRepository.save(faq));
    }

    public FaqDto updateFaq(Long faqId, FaqDto dto) {
        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new IllegalArgumentException("FAQ를 찾을 수 없습니다."));
        faq.setCategory(dto.getCategory());
        faq.setQuestion(dto.getQuestion());
        faq.setAnswer(dto.getAnswer());
        return toDto(faqRepository.save(faq));
    }

    public void deleteFaq(Long faqId) {
        faqRepository.deleteById(faqId);
    }

    private FaqDto toDto(Faq faq) {
        return FaqDto.builder()
                .faqId(faq.getFaqId())
                .category(faq.getCategory())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .build();
    }
}
