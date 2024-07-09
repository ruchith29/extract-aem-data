package com.quipseo_ms.quipseo_ms.services;

import com.quipseo_ms.quipseo_ms.utilities.Constants;
import com.quipseo_ms.quipseo_ms.utilities.MongoUtilities;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Service
public class DomainService {

    @Autowired
    private MongoUtilities mongoUtilities;

    String collectionName= Constants.COLLECTION_NAME;

    public String postDomain(Map<String,String> domainData){
        return mongoUtilities.insertData(domainData,collectionName);
    }


    // excess method which is used to fetch data from db either by id or title or url.
    public Document getDomainByData(Map<String,String> data) {
        Query query=new Query();
        if (data.containsKey("id")){
            query.addCriteria(Criteria.where("_id").is(data.get("id")));
        }
        else if (data.containsKey("title")) {
            query.addCriteria(Criteria.where("title").is(data.get("title")));
        }
        else {
            query.addCriteria(Criteria.where("url").is(data.get("url")));
        }
        query.fields().exclude("_id");
        return mongoUtilities.getByData(query,collectionName);
    }

    public List<Document> getAllDomains(){
        Query query = new Query();
        query.fields().exclude("_id");
        return mongoUtilities.getAllData(query,collectionName);
    }

    public Document getDomainByID(@PathVariable String id){

        Query query=new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        return mongoUtilities.getById(query,collectionName);
    }

    public Boolean updateDomain(@RequestBody Map<String,String> newDocument){
        String id=newDocument.get("_id");

        if(id==null){
            return false;
        }

        if (id.length()!=24 && !(ObjectId.isValid(id))) {
                return false;
            }

        newDocument.remove("_id");
        Document query = new Document("_id",new ObjectId(id));
        Document updateDocument=new Document("$set",newDocument);
        return mongoUtilities.updateData(id,query,updateDocument,collectionName);
    }


    public Boolean deleteDomain(@PathVariable String id){

        Document query=new Document("_id", new ObjectId(id));
        return mongoUtilities.deleteData(query,collectionName);
    }


}