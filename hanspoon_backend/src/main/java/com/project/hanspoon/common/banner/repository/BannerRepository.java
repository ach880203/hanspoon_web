package com.project.hanspoon.common.banner.repository;

import com.project.hanspoon.common.banner.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByIsActiveTrueOrderBySortOrderAscBannerIdAsc();

    List<Banner> findAllByOrderBySortOrderAscBannerIdAsc();
}
