package com.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.entity.Space;
import com.repository.SpaceRepository;

@Service
public class SpaceService {

    @Autowired
    private SpaceRepository spaceRepository;

    public List<Space> getSpacesByAdmin(Long adminId) {
        return spaceRepository.findByAdminId(adminId);
    }

    public Space save(Space space) {
        return spaceRepository.save(space);
    }

    public Space getById(Long id) {
        return spaceRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        spaceRepository.deleteById(id);
    }
}
