package edu.green.mall;
/** Synchronous event; listeners participate in the caller transaction. */
public record RedemptionChanged(long userId,long orderId,long points,boolean refund){}
