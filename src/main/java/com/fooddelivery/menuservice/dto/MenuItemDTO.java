package com.fooddelivery.menuservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public class MenuItemDTO implements Serializable {
    private UUID menuId;

    @NotBlank(message = "Menu item name is required")
    @Size(max = 100, message = "Menu item name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Availability status is required")
    private Boolean availability = true;

    @NotNull(message = "Vegetarian status is required")
    private Boolean veg;

    // Constructors
    public MenuItemDTO() {}

    public MenuItemDTO(UUID menuId, String name, BigDecimal price, Boolean availability, Boolean veg) {
        this.menuId = menuId;
        this.name = name;
        this.price = price;
        this.availability = availability;
        this.veg = veg;
    }

    // Getters and Setters
    public UUID getMenuId() {
        return menuId;
    }

    public void setMenuId(UUID menuId) {
        this.menuId = menuId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }

    public Boolean getVeg() {
        return veg;
    }

    public void setVeg(Boolean veg) {
        this.veg = veg;
    }
}