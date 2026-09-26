package service;

import models.Rider;

import java.time.Duration;

public record RiderStatus(Rider rider, int completedDeliveries, Duration averageDeliveryDuration) {
}
