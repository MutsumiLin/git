package edu.green.points;
import edu.green.mall.RedemptionChanged;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
@Component
public class RedemptionPointsListener {
 private final PointWalletService wallets;public RedemptionPointsListener(PointWalletService w){wallets=w;}
 @EventListener @Order(0) public void on(RedemptionChanged event){wallets.changeForOrder(event.userId(),event.orderId(),event.points(),event.refund());}
}
