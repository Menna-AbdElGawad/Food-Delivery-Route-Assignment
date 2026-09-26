package service;

import models.Restaurant;

import java.math.BigDecimal;

public record RestaurantRevenue(Restaurant restaurant, BigDecimal revenue) {
}
