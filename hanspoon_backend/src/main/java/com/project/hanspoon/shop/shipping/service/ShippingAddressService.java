package com.project.hanspoon.shop.shipping.service;

import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.common.user.repository.UserRepository;
import com.project.hanspoon.shop.shipping.dto.ShippingAddressDto;
import com.project.hanspoon.shop.shipping.entity.ShippingAddress;
import com.project.hanspoon.shop.shipping.repository.ShippingAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShippingAddressService {

    private final ShippingAddressRepository repo;
    private final UserRepository userRepository;

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다."));
    }

    /** 배송지가 없으면 User의 address/userName/phone로 기본배송지 1개 자동 생성 */
    @Transactional
    public void ensureBootstrapDefault(Long userId) {
        if (repo.existsByUser_UserId(userId)) return;

        User user = getUserOrThrow(userId);

        String addr = user.getAddress();
        String name = user.getUserName();
        String phone = user.getPhone();

        if (addr == null || addr.isBlank()) return;
        if (name == null || name.isBlank()) return;
        if (phone == null || phone.isBlank()) return;

        ShippingAddress base = ShippingAddress.builder()
                .user(user)
                .label("기본배송지")
                .receiverName(name)
                .receiverPhone(phone)
                .zipCode(null)
                .address1(addr)
                .address2(null)
                .isDefault(true)
                .build();

        repo.save(base);
    }

    public List<ShippingAddressDto.Response> listMy(Long userId) {
        ensureBootstrapDefault(userId);
        return repo.findByUser_UserIdOrderByIsDefaultDescShippingAddressIdDesc(userId)
                .stream().map(ShippingAddressDto.Response::from).toList();
    }

    public ShippingAddressDto.Response getMyDefault(Long userId) {
        ensureBootstrapDefault(userId);
        ShippingAddress def = repo.findByUser_UserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "기본 배송지가 없습니다. 배송지를 추가해 주세요."));
        return ShippingAddressDto.Response.from(def);
    }

    @Transactional
    public ShippingAddressDto.Response create(Long userId, ShippingAddressDto.CreateRequest req) {
        User user = getUserOrThrow(userId);

        boolean first = !repo.existsByUser_UserId(userId);
        boolean makeDefault = Boolean.TRUE.equals(req.getIsDefault()) || first;

        if (makeDefault) {
            repo.findByUser_UserIdAndIsDefaultTrue(userId).ifPresent(a -> a.setDefault(false));
        }

        ShippingAddress addr = ShippingAddress.builder()
                .user(user)
                .label(req.getLabel())
                .receiverName(req.getReceiverName())
                .receiverPhone(req.getReceiverPhone())
                .zipCode(req.getZipCode())
                .address1(req.getAddress1())
                .address2(req.getAddress2())
                .isDefault(makeDefault)
                .build();

        return ShippingAddressDto.Response.from(repo.save(addr));
    }

    @Transactional
    public ShippingAddressDto.Response update(Long userId, Long id, ShippingAddressDto.UpdateRequest req) {
        ShippingAddress addr = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "배송지를 찾을 수 없습니다."));
        if (!addr.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "권한이 없습니다.");
        }

        addr.update(req.getLabel(), req.getReceiverName(), req.getReceiverPhone(),
                req.getZipCode(), req.getAddress1(), req.getAddress2());

        if (Boolean.TRUE.equals(req.getIsDefault())) {
            repo.findByUser_UserIdAndIsDefaultTrue(userId).ifPresent(a -> a.setDefault(false));
            addr.setDefault(true);
        }

        return ShippingAddressDto.Response.from(addr);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        ShippingAddress addr = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "배송지를 찾을 수 없습니다."));
        if (!addr.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "권한이 없습니다.");
        }

        boolean wasDefault = addr.isDefault();
        repo.delete(addr);

        if (wasDefault) {
            List<ShippingAddress> rest = repo.findByUser_UserIdOrderByIsDefaultDescShippingAddressIdDesc(userId);
            if (!rest.isEmpty()) {
                rest.forEach(a -> a.setDefault(false));
                rest.get(0).setDefault(true);
            }
        }
    }

    @Transactional
    public ShippingAddressDto.Response setDefault(Long userId, Long id) {
        ShippingAddress target = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "배송지를 찾을 수 없습니다."));
        if (!target.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "권한이 없습니다.");
        }

        repo.findByUser_UserIdAndIsDefaultTrue(userId).ifPresent(a -> a.setDefault(false));
        target.setDefault(true);

        return ShippingAddressDto.Response.from(target);
    }
}