package com.fooddelivery.menuservice.repository;

import com.fooddelivery.menuservice.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    List<MenuItem> findByRestaurantRestaurantId(UUID restaurantId);

    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.restaurantId = :restaurantId AND m.availability = true")
    List<MenuItem> findAvailableMenuItemsByRestaurantId(@Param("restaurantId") UUID restaurantId);

    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.restaurantId = :restaurantId AND m.veg = :isVeg")
    List<MenuItem> findMenuItemsByRestaurantIdAndVegStatus(@Param("restaurantId") UUID restaurantId,
                                                           @Param("isVeg") Boolean isVeg);

    long countByRestaurantRestaurantId(UUID restaurantId);
}