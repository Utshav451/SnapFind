package com.snapfind.backend.repository;

import com.snapfind.backend.entity.Collection;
import com.snapfind.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    //Get all collections created by a specific user (My Collections page)
    List<Collection> findByOwner(User owner);

    //Find collection by invite key (when guest pastes key)
    Optional<Collection> findByUniqueKey(String uniqueKey);
}
