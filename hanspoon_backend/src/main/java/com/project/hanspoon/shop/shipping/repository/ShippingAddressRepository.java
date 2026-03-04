package com.project.hanspoon.shop.shipping.repository;

import com.project.hanspoon.shop.shipping.entity.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {

    List<ShippingAddress> findByUser_UserIdOrderByIsDefaultDescShippingAddressIdDesc(Long userId);

    Optional<ShippingAddress> findByUser_UserIdAndIsDefaultTrue(Long userId);

    boolean existsByUser_UserId(Long userId);
}