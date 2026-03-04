package com.project.hanspoon.shop.shipping.controller;

import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.shop.shipping.dto.ShippingAddressDto;
import com.project.hanspoon.shop.shipping.service.ShippingAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my/shipping-addresses")
public class ShippingAddressController {

    private final ShippingAddressService service;

    @GetMapping
    public List<ShippingAddressDto.Response> list(@AuthenticationPrincipal CustomUserDetails user) {
        return service.listMy(user.getUserId());
    }

    @GetMapping("/default")
    public ShippingAddressDto.Response getDefault(@AuthenticationPrincipal CustomUserDetails user) {
        return service.getMyDefault(user.getUserId());
    }

    @PostMapping
    public ShippingAddressDto.Response create(@AuthenticationPrincipal CustomUserDetails user,
                                              @Valid @RequestBody ShippingAddressDto.CreateRequest req) {
        return service.create(user.getUserId(), req);
    }

    @PutMapping("/{id}")
    public ShippingAddressDto.Response update(@AuthenticationPrincipal CustomUserDetails user,
                                              @PathVariable Long id,
                                              @RequestBody ShippingAddressDto.UpdateRequest req) {
        return service.update(user.getUserId(), id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal CustomUserDetails user,
                       @PathVariable Long id) {
        service.delete(user.getUserId(), id);
    }

    @PatchMapping("/{id}/default")
    public ShippingAddressDto.Response setDefault(@AuthenticationPrincipal CustomUserDetails user,
                                                  @PathVariable Long id) {
        return service.setDefault(user.getUserId(), id);
    }
}