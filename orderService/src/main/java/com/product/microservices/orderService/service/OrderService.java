package com.product.microservices.orderService.service;

import com.product.microservices.orderService.client.InventoryClient;
import com.product.microservices.orderService.dto.OrderRequest;
import com.product.microservices.orderService.event.OrderPlacedEvent;
import com.product.microservices.orderService.model.Order;
import com.product.microservices.orderService.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String,OrderPlacedEvent> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, InventoryClient inventoryClient,KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void placeOrder(OrderRequest orderRequest){
        var isProductIsInStock=inventoryClient.isInStock(orderRequest.skuCode(),orderRequest.quantity());
        if(isProductIsInStock){
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price().multiply(BigDecimal.valueOf(orderRequest.quantity())));
            order.setSkuCode(orderRequest.skuCode());
            order.setQuantity(orderRequest.quantity());
            orderRepository.save(order);
            logger.info("Order Placed Successfully");
            // Send the message to the Kafka Topic
            OrderPlacedEvent orderPlacedEvent= new OrderPlacedEvent();
            orderPlacedEvent.setOrderNumber(order.getOrderNumber());
            orderPlacedEvent.setEmail(orderRequest.userDetails().email());
            orderPlacedEvent.setFirstName("James");
            orderPlacedEvent.setLastName("Potter");
            logger.info("Start - sending OrderplacedEvent {} to Kafka topic order-placed",orderPlacedEvent);
            kafkaTemplate.send("order-placed",orderPlacedEvent);
            logger.info("End - sending OrderplacedEvent {} to Kafka topic order-placed",orderPlacedEvent);

        }else{
            throw new RuntimeException("Product with SkuCode "+orderRequest.skuCode()+" is not in stock");
        }
    }
}
