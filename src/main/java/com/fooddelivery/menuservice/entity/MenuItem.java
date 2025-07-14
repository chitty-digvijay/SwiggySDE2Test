package com.fooddelivery.menuservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "menu_id", columnDefinition = "BINARY(16)")
    private UUID menuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false, columnDefinition = "BINARY(16)")
    private Restaurant restaurant;

    @NotBlank(message = "Menu item name is required")
    @Size(max = 100, message = "Menu item name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Column(name = "price", nullable = false, precision = 7, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Availability status is required")
    @Column(name = "availability", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean availability = true;

    @NotNull(message = "Vegetarian status is required")
    @Column(name = "veg", nullable = false)
    private Boolean veg;

    // Constructors
    public MenuItem() {}

    public MenuItem(Restaurant restaurant, String name, BigDecimal price, Boolean availability, Boolean veg) {
        this.restaurant = restaurant;
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

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
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

    @Override
    public String toString() {
        return "MenuItem{" +
                "menuId=" + menuId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", availability=" + availability +
                ", veg=" + veg +
                '}';
    }
}