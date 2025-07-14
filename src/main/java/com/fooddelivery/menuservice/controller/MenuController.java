package com.fooddelivery.menuservice.controller;

import com.fooddelivery.menuservice.dto.ApiResponse;
import com.fooddelivery.menuservice.dto.CreateRestaurantRequest;
import com.fooddelivery.menuservice.dto.RestaurantDTO;
import com.fooddelivery.menuservice.service.MenuService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
@CrossOrigin(origins = "*")
public class MenuController {

    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);

    @Autowired
    private MenuService menuService;

    /**
     * API 1: Add restaurant with menu items
     * POST /api/v1/restaurants
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<RestaurantDTO>> addRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request) {

        logger.info("POST /api/v1/restaurants - Adding restaurant: '{}'", request.getName());

        try {
            RestaurantDTO createdRestaurant = menuService.addRestaurantWithMenu(request);

            ApiResponse<RestaurantDTO> response = ApiResponse.success(
                    "Restaurant created successfully",
                    createdRestaurant
            );

            logger.info("Restaurant '{}' created successfully with ID: {}",
                    createdRestaurant.getName(), createdRestaurant.getRestaurantId());

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating restaurant '{}': {}", request.getName(), e.getMessage());
            throw e;
        }
    }

    /**
     * API 2: Get restaurant menu with multi-level caching
     * GET /api/v1/restaurants/{restaurantId}/menu
     */
    @GetMapping(
            value = "/{restaurantId}/menu",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<RestaurantDTO>> getRestaurantMenu(
            @PathVariable UUID restaurantId) {

        logger.info("GET /api/v1/restaurants/{}/menu - Retrieving menu", restaurantId);

        try {
            RestaurantDTO restaurant = menuService.getRestaurantMenu(restaurantId);

            ApiResponse<RestaurantDTO> response = ApiResponse.success(
                    "Menu retrieved successfully",
                    restaurant
            );

            logger.info("Menu retrieved successfully for restaurant: {} with {} items",
                    restaurantId, restaurant.getMenuItems().size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving menu for restaurant {}: {}", restaurantId, e.getMessage());
            throw e;
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        logger.debug("Health check requested");

        ApiResponse<String> response = ApiResponse.success("Menu service is running", "OK");
        return ResponseEntity.ok(response);
    }
}