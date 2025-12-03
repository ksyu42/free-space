package com.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.entity.Space;

public interface SpaceRepository extends JpaRepository<Space, Long> {
    List<Space> findByAdminId(Long adminId);
}
