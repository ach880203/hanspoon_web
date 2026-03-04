package com.project.hanspoon.common.notice.service;

import com.project.hanspoon.common.notice.dto.NoticeDto;
import com.project.hanspoon.common.notice.entity.Notice;
import com.project.hanspoon.common.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional(readOnly = true)
    public Page<NoticeDto> getNoticeList(Pageable pageable) {
        return noticeRepository.findAllByOrderByIsImportantDescCreatedAtDesc(pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public NoticeDto getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
        return toDto(notice);
    }

    public NoticeDto createNotice(NoticeDto dto) {
        Notice notice = Notice.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .isImportant(dto.getIsImportant() != null ? dto.getIsImportant() : false)
                .build();
        return toDto(noticeRepository.save(notice));
    }

    public NoticeDto updateNotice(Long noticeId, NoticeDto dto) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setIsImportant(dto.getIsImportant() != null ? dto.getIsImportant() : false);
        return toDto(noticeRepository.save(notice));
    }

    public void deleteNotice(Long noticeId) {
        noticeRepository.deleteById(noticeId);
    }

    @Transactional(readOnly = true)
    public Page<NoticeDto> searchNotices(String keyword, Pageable pageable) {
        return noticeRepository.findByTitleContaining(keyword, pageable)
                .map(this::toDto);
    }

    private NoticeDto toDto(Notice notice) {
        return NoticeDto.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .isImportant(notice.getIsImportant())
                .viewCount(notice.getViewCount())
                .createdAt(notice.getCreatedAt() != null ? notice.getCreatedAt().format(formatter) : null)
                .build();
    }
}
