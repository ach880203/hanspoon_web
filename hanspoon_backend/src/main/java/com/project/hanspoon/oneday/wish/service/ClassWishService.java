package com.project.hanspoon.oneday.wish.service;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.oneday.clazz.entity.ClassProduct;
import com.project.hanspoon.oneday.clazz.repository.ClassProductRepository;
import com.project.hanspoon.oneday.wish.dto.WishItemResponse;
import com.project.hanspoon.oneday.wish.dto.WishToggleResponse;
import com.project.hanspoon.oneday.wish.entity.ClassWish;
import com.project.hanspoon.oneday.wish.repository.ClassWishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ClassWishService {

    private final ClassWishRepository classWishRepository;
    private final ClassProductRepository classProductRepository;

    @Transactional
    public WishToggleResponse toggle(Long userId, Long classProductId) {
        validateUserId(userId);
        if (classProductId == null || classProductId <= 0) {
            throw new BusinessException("클래스 ID가 올바르지 않습니다.");
        }

        var existing = classWishRepository.findByUserIdAndClassProduct_Id(userId, classProductId);
        if (existing.isPresent()) {
            classWishRepository.delete(existing.get());
            return new WishToggleResponse(userId, classProductId, false);
        }

        ClassProduct classProduct = classProductRepository.findById(classProductId)
                .orElseThrow(() -> new BusinessException("클래스를 찾을 수 없습니다. id=" + classProductId));

        classWishRepository.save(ClassWish.of(userId, classProduct));
        return new WishToggleResponse(userId, classProductId, true);
    }

    @Transactional(readOnly = true)
    public List<WishItemResponse> list(Long userId) {
        validateUserId(userId);
        return classWishRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(wish -> new WishItemResponse(
                        wish.getId(),
                        wish.getClassProduct().getId(),
                        wish.getClassProduct().getTitle(),
                        wish.getCreatedAt()
                ))
                .toList();
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("로그인 정보가 올바르지 않습니다.");
        }
    }
}
