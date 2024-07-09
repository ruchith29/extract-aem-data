package com.quipaem.extract.aem.data.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoTemplateConfiguration {

    @Autowired
    private MongoConfiguration mongoConfiguration;

    public MongoTemplate mongoTemplate(String databaseName){
        return new MongoTemplate(mongoConfiguration.mongoClient(), "admin");
    }
}
