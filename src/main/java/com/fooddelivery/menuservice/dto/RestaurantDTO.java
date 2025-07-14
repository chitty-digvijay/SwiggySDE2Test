package com.fooddelivery.menuservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class RestaurantDTO implements Serializable {
    private UUID restaurantId;

    @NotBlank(message = "Restaurant name is required")
    @Size(max = 100, message = "Restaurant name must not exceed 100 characters")
    private String name;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Valid
    @NotEmpty(message = "At least one menu item is required")
    private List<MenuItemDTO> menuItems;

    // Constructors
    public RestaurantDTO() {}

    public RestaurantDTO(UUID restaurantId, String name, String address, List<MenuItemDTO> menuItems) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.address = address;
        this.menuItems = menuItems;
    }

    // Getters and Setters
    public UUID getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(UUID restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<MenuItemDTO> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItemDTO> menuItems) {
        this.menuItems = menuItems;
    }
}