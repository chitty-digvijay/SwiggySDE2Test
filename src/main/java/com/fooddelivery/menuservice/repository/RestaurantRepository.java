package com.fooddelivery.menuservice.repository;

import com.fooddelivery.menuservice.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    @Query("SELECT r FROM Restaurant r LEFT JOIN FETCH r.menuItems WHERE r.restaurantId = :restaurantId")
    Optional<Restaurant> findByIdWithMenuItems(@Param("restaurantId") UUID restaurantId);

    Optional<Restaurant> findByName(String name);

    boolean existsByName(String name);
}