package com.product.microservices.product.service;


import com.product.microservices.product.dto.ProductRequest;
import com.product.microservices.product.dto.ProductResponse;
import com.product.microservices.product.event.ProductCreatedEvent;
import com.product.microservices.product.model.Product;
import com.product.microservices.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
@Slf4j
public class ProductService {
    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final ProductRepository productRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    public ProductService(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate, ProductRepository productRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(ProductRequest productRequest){
        Product product = new Product(productRequest.id(),productRequest.name(),productRequest.description(),productRequest.skuCode(),productRequest.price());
        productRepository.save(product);
        logger.info("Product Created Successfully");
        ProductCreatedEvent productCreatedEvent= new ProductCreatedEvent(productRequest.skuCode(),productRequest.price(),100);
        logger.info("Start - sending productCreatedEvent {} to Kafka topic product-producer",productCreatedEvent);
        kafkaTemplate.send("product-producer",productCreatedEvent);
        logger.info("End - sending productCreatedEvent {} to Kafka topic product-producer",productCreatedEvent);
        return new ProductResponse(product.getId(),product.getName(),product.getDescription(),product.getSkuCode(),product.getPrice());
    }

    public List<ProductResponse> getAllProducts(){
        return productRepository.findAll()
                .stream()
                .map(product -> new ProductResponse(product.getId(),product.getName(),product.getDescription(), product.getSkuCode(), product.getPrice()))
                .toList();
    }
}
