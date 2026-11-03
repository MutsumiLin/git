package edu.green.mall;

public record ProductView(long id,long merchantId,String merchantName,String sku,String name,String description,
                          String category,long pointsPrice,int stock,String status) { }
