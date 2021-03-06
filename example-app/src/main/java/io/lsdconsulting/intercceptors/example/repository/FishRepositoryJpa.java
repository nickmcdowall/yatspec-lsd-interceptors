package io.lsdconsulting.intercceptors.example.repository;

import io.lsdconsulting.intercceptors.example.entity.Fish;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Transactional
@Repository
public interface FishRepositoryJpa extends CrudRepository<Fish, String> {
    Fish findFishByName(String name);

    void deleteByName(String name);
}
