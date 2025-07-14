package com.fooddelivery.menuservice.service;

import com.fooddelivery.menuservice.dto.CreateRestaurantRequest;
import com.fooddelivery.menuservice.dto.MenuItemDTO;
import com.fooddelivery.menuservice.dto.RestaurantDTO;
import com.fooddelivery.menuservice.entity.MenuItem;
import com.fooddelivery.menuservice.entity.Restaurant;
import com.fooddelivery.menuservice.exception.ResourceNotFoundException;
import com.fooddelivery.menuservice.exception.DuplicateResourceException;
import com.fooddelivery.menuservice.repository.RestaurantRepository;
import com.fooddelivery.menuservice.repository.MenuItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MenuService {

    private static final Logger logger = LoggerFactory.getLogger(MenuService.class);

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CacheService cacheService;

    /**
     * API 1: Add restaurant with menu items
     */
    public RestaurantDTO addRestaurantWithMenu(CreateRestaurantRequest request) {
        logger.info("Adding new restaurant: {}", request.getName());

        // Check if restaurant already exists
        if (restaurantRepository.existsByName(request.getName())) {
            logger.warn("Duplicate restaurant name attempted: {}", request.getName());
            throw new DuplicateResourceException("Restaurant with name '" + request.getName() + "' already exists");
        }

        // Create restaurant
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant = restaurantRepository.save(restaurant);
        final Restaurant restaurant1 = restaurant;
        logger.debug("Restaurant entity saved with ID: {}", restaurant.getRestaurantId());

        // Create menu items
        List<MenuItem> menuItems = request.getMenuItems().stream()
                .map(menuItemDTO -> {
                    MenuItem menuItem = new MenuItem();
                    menuItem.setRestaurant(restaurant1);
                    menuItem.setName(menuItemDTO.getName());
                    menuItem.setPrice(menuItemDTO.getPrice());
                    menuItem.setAvailability(menuItemDTO.getAvailability());
                    menuItem.setVeg(menuItemDTO.getVeg());
                    return menuItem;
                })
                .collect(Collectors.toList());

        menuItems = menuItemRepository.saveAll(menuItems);
        restaurant.setMenuItems(menuItems);

        logger.debug("Saved {} menu items for restaurant: {}", menuItems.size(), restaurant.getRestaurantId());

        // Convert to DTO and cache
        RestaurantDTO restaurantDTO = convertToDTO(restaurant);

        logger.info("Restaurant '{}' created successfully with ID: {} and {} menu items",
                request.getName(), restaurant.getRestaurantId(), menuItems.size());
        return restaurantDTO;
    }

    /**
     * API 2: Get restaurant menu with multi-level caching
     * L1 (Caffeine) -> L2 (Redis) -> Database
     */
    @Transactional(readOnly = true)
    public RestaurantDTO getRestaurantMenu(UUID restaurantId) {
        logger.info("Getting menu for restaurant: {}", restaurantId);

        // Step 1: Check L1 Cache (Caffeine)
        RestaurantDTO restaurant = cacheService.getFromL1Cache(restaurantId);
        if (restaurant != null) {
            logger.info("Menu retrieved from L1 cache for restaurant: {}", restaurantId);
            return restaurant;
        }

        // Step 2: Check L2 Cache (Redis)
        restaurant = cacheService.getFromL2Cache(restaurantId);
        if (restaurant != null) {
            logger.info("Menu retrieved from L2 cache for restaurant: {}", restaurantId);
            cacheService.updateL1Cache(restaurantId,restaurant);
            return restaurant;
        }

        // Step 3: Get from Database
        logger.info("Cache miss - retrieving from database for restaurant: {}", restaurantId);
        Restaurant restaurantEntity = restaurantRepository.findByIdWithMenuItems(restaurantId)
                .orElseThrow(() -> {
                    logger.error("Restaurant not found with ID: {}", restaurantId);
                    return new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId);
                });

        restaurant = convertToDTO(restaurantEntity);

        // Step 4: Update both caches
        cacheService.updateBothCaches(restaurantId, restaurant);

        logger.info("Menu retrieved from database and cached for restaurant: {} with {} menu items",
                restaurantId, restaurant.getMenuItems().size());
        return restaurant;
    }

    /**
     * Convert Restaurant entity to DTO
     */
    private RestaurantDTO convertToDTO(Restaurant restaurant) {
        logger.debug("Converting restaurant entity to DTO for restaurant: {}", restaurant.getRestaurantId());

        List<MenuItemDTO> menuItemDTOs = restaurant.getMenuItems().stream()
                .map(menuItem -> new MenuItemDTO(
                        menuItem.getMenuId(),
                        menuItem.getName(),
                        menuItem.getPrice(),
                        menuItem.getAvailability(),
                        menuItem.getVeg()
                ))
                .collect(Collectors.toList());

        return new RestaurantDTO(
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getAddress(),
                menuItemDTOs
        );
    }
}