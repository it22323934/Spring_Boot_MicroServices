package com.product.microservices.inventoryService.service;

import com.product.microservices.inventoryService.model.Inventory;
import com.product.microservices.inventoryService.repository.InventoryRepository;
import com.product.microservices.product.event.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public boolean isInStock(String skuCode,Integer quantity){
        return inventoryRepository.existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode,quantity);
    }

    @KafkaListener(topics = "product-producer")
    public void createProduct(ProductCreatedEvent productCreatedEvent){
        logger.info("Got Message from order-placed topic {}", productCreatedEvent);
        logger.info(productCreatedEvent.getSkuCode());
        logger.info(productCreatedEvent.getQuantity().toString());
        Inventory inventory= new Inventory();
        inventory.setSkuCode(productCreatedEvent.getSkuCode());
        inventory.setQuantity(productCreatedEvent.getQuantity());
        inventoryRepository.save(inventory);
        logger.info("Got Message from order-placed topic {} and product added to inventory successfully", productCreatedEvent);

    }
}
