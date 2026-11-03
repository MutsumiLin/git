package edu.green.points;

import edu.green.identity.UserProvisioned;
import edu.green.trip.TripAccepted;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class PointsEventListeners {
    private final PointWalletService wallets;
    public PointsEventListeners(PointWalletService wallets) { this.wallets=wallets; }
    @EventListener
    public void initialize(UserProvisioned event) { if("USER".equals(event.role())) wallets.initialize(event.userId()); }
    @EventListener
    @Order(0)
    public void reward(TripAccepted event) { wallets.creditTrip(event.userId(),event.tripId(),event.points()); }
}
