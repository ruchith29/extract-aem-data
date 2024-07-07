package com.quipaem.extract.aem.data.utilities;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.quipaem.extract.aem.data.configuration.MongoConfiguration;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MongoUtilities {

    @Autowired
    public MongoTemplate mongoTemplate;

    @Autowired
    private MongoConfiguration mongoConfiguration;

    public MongoUtilities(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public MongoTemplate mongoTemplate(String databaseName){
        return new MongoTemplate(mongoConfiguration.mongoClient(), databaseName+"_QUIP_SEO");
    }

    public String getDatabase(){
        return mongoTemplate.getDb().getName();
    }

    public List<Document> getAllData(Query query, String collectionName,MongoTemplate userMongoTemplate){
        return userMongoTemplate.find(query, Document.class, collectionName);
    }

    public Boolean insertData(String collectionName, Document data,MongoTemplate userMongoTemplate){
        InsertOneResult result =userMongoTemplate.getCollection(collectionName).insertOne(data);
        return result.wasAcknowledged();
    }

    public Boolean updateDocument(Document document,MongoTemplate userMongoTemplate) {
        Document query=new Document("pagePath|PagePath",document.get("pagePath|PagePath"));
        Document oldDocument=userMongoTemplate.getDb().getCollection(Constants.COLLECTION_NAME).find(query).first();
        if (oldDocument != null) {
            UpdateResult result = userMongoTemplate.getDb().getCollection(Constants.COLLECTION_NAME).updateOne(query,new Document("$set",document));
            return result.wasAcknowledged();
        }
        return false;
    }

    public Boolean deleteData(String data,MongoTemplate userMongoTemplate) {
        Query query=new Query();
        query.addCriteria(Criteria.where("user-id").is(data));
        DeleteResult result = userMongoTemplate.remove(query, Constants.COLLECTION_NAME);
        return result.getDeletedCount() != 0;
    }
}