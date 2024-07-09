package com.quipseo_ms.quipseo_ms.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoConfiguration {

    @Bean
    public MongoTemplate mongoTemplate(){
        MongoClient mongoClient= MongoClients.create();
        Logger logger= LoggerFactory.getLogger(MongoConfiguration.class);
        logger.info("Database instance is created");
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient,"user-domain-urls"));
    }

    @Bean
    public MongoClient mongoClient(){
        return MongoClients.create();
    }
}
