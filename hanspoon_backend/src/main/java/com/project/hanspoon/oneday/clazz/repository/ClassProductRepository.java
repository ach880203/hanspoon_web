package com.project.hanspoon.oneday.clazz.repository;

import com.project.hanspoon.oneday.clazz.domain.RunType;
import com.project.hanspoon.oneday.clazz.entity.ClassProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassProductRepository
        extends JpaRepository<ClassProduct, Long>, JpaSpecificationExecutor<ClassProduct> {

    List<ClassProduct> findAllByRunTypeOrderByCreatedAtDesc(RunType runType, Pageable pageable);

    boolean existsByInstructor_Id(Long instructorId);
}
