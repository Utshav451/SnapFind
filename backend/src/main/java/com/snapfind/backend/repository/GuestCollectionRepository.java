package com.snapfind.backend.repository;

import com.snapfind.backend.entity.Collection;
import com.snapfind.backend.entity.GuestCollection;
import com.snapfind.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GuestCollectionRepository extends JpaRepository<GuestCollection, Long> {

    //Get all collections saved in guest's list (Saved Collections page)
    List<GuestCollection> findByUser(User user);

    //Check if guest already accessed this collection (avoid duplicates)
    boolean existsByUserAndCollection(User user, Collection collection);

    //Find specific guest-collection link (for removing from saved list)
    Optional<GuestCollection> findByUserAndCollection(User user, Collection collection);
}