package com.quipseo_ms.quipseo_ms.utilities;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;


@Service
public class MongoUtilities {

    @Autowired
    private MongoTemplate mongoTemplate;

    public MongoDatabase getDatabase(){
        return mongoTemplate.getDb();
    }

    public MongoCollection<Document> getCollectionName(String collectionName){
        return getDatabase().getCollection(collectionName);
    }

    public String insertData(Map<String,String> data, String collectionName){
        Document domainData=new Document(data);
        try
        {
            mongoTemplate.insert(domainData,collectionName);
        }
        catch (Exception exception){
            return null;
        }

        return domainData.get("_id").toString();
    }

    public List<Document> getAllData(Query query,String collectionName){
        return mongoTemplate.find(query, Document.class, collectionName);
    }

    public Document getById(Query query,String collectionName) {
        return mongoTemplate.findOne(query, Document.class, collectionName);
    }

    public Boolean updateData(String id,Document query, Document updateDocument,String collectionName){
        Query query1=new Query(Criteria.where("_id").is(id));
        if(mongoTemplate.findOne(query1, Document.class, collectionName)!=null){
            mongoTemplate.getCollection(collectionName).updateOne(query,updateDocument);
            return true;
        }
        return false;
    }

    public Boolean deleteData(Document query,String collectionName){
        DeleteResult result=mongoTemplate.getCollection(collectionName).deleteOne(query);
        return result.getDeletedCount() != 0;
    }

    public Document getByData(Query query,String collectionName) {
        return mongoTemplate.findOne(query, Document.class, collectionName);
    }

    public MongoTemplate mongoTemplate(MongoClient mongoClient,String databaseName){
        return new MongoTemplate(mongoClient, databaseName+"_QUIP_SEO");
    }

    public List<Document> getAllDataFromAEM(Query query, String collectionName,MongoTemplate userMongoTemplate){
        return userMongoTemplate.find(query, Document.class, collectionName);
    }

    public Boolean insertDataFromAEM(String collectionName, Document data,MongoTemplate userMongoTemplate){
        InsertOneResult result =userMongoTemplate.getCollection(collectionName).insertOne(data);
        return result.wasAcknowledged();
    }

    public Boolean updateDocumentAEM(Document document,MongoTemplate userMongoTemplate) {
        Document query=new Document("pagePath|PagePath",document.get("pagePath|PagePath"));
        Document oldDocument=userMongoTemplate.getDb().getCollection(Constants.AEM_COLLECTION_NAME).find(query).first();
        if (oldDocument != null) {
            UpdateResult result = userMongoTemplate.getDb().getCollection(Constants.AEM_COLLECTION_NAME)
                    .updateOne(query,new Document("$set",document));
            return result.wasAcknowledged();
        }
        return false;
    }

    public Boolean deleteDataAEM(String data,MongoTemplate userMongoTemplate) {
        Query query=new Query();
        query.addCriteria(Criteria.where("clientName").is(data));
        DeleteResult result = userMongoTemplate.remove(query, Constants.AEM_COLLECTION_NAME);
        return result.getDeletedCount()!=0;
    }
}