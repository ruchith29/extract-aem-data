package com.quipseo_ms.quipseo_ms.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quipseo_ms.quipseo_ms.config.MongoConfiguration;
import com.quipseo_ms.quipseo_ms.utilities.Constants;
import com.quipseo_ms.quipseo_ms.utilities.MongoUtilities;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExtractAEMDataService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MongoUtilities mongoUtilities;

    @Autowired
    private MongoConfiguration mongoConfiguration;

    public Map<String,List<Map<String,String>>> storeData(String jsonData ,String clientName) throws JsonProcessingException {
        int failedCount=0;
        int successCount=0;

        MongoTemplate clientMongoTemplate=mongoUtilities.mongoTemplate(mongoConfiguration.mongoClient(),clientName);

        Map<String,List<Map<String,String>>> status=new LinkedHashMap<>();

        JsonNode jsonNodes = objectMapper.readTree(jsonData);
        JsonNode databaseContent = jsonNodes.get(Constants.AEM_DATA_NAME);

        List<Map<String,String>> pageDetails=new ArrayList<>();
        List<Map<String,String>> errorResponse=new ArrayList<>();
        List<Map<String,String>>valuesStored =new ArrayList<>();

        for (Iterator<Map.Entry<String, JsonNode>> databases = databaseContent.fields(); databases.hasNext(); ) {
            Map.Entry<String, JsonNode> databaseEntry = databases.next();
            String collectionName = databaseEntry.getKey();
            ArrayNode pageData = (ArrayNode) databaseEntry.getValue();

            Map<String,String> response =new LinkedHashMap<>();

            for (JsonNode innerData : pageData) {
                Map<String, Object> result = objectMapper.convertValue(innerData, new TypeReference<Map<String, Object>>() {});
                result.put("page-name", collectionName);
                result.put("clientName", clientName);
                Document document=new Document(result);
                if(mongoUtilities.insertDataFromAEM(Constants.AEM_COLLECTION_NAME, document,clientMongoTemplate)){
                    successCount++;
                    Map<String,String> page=new LinkedHashMap<>();
                    page.put(document.get("_id").toString(),document.getString("pagePath|PagePath"));
                    pageDetails.add(page);
                    continue;
                }
                response.put("response","Failed");
                response.put("page-name",collectionName);
                failedCount++;
                response.put("pagePath|PagePath",result.get("pagePath|PagePath").toString());
                errorResponse.add(response);

            }
            response.put("page-name",collectionName);
            response.put("Success Count",""+successCount);
            response.put("Failed Count",""+failedCount);
            valuesStored.add(response);
        }

        status.put("values-stored",valuesStored);
        status.put("error-responses",errorResponse);
        status.put("page -details",pageDetails);
        return status;
    }

    public Map<String,Map<String,List<Document>>> getSpecificDataFromDB(ArrayNode sitePaths,String clientName){

        MongoTemplate clientMongoTemplate=mongoUtilities.mongoTemplate(mongoConfiguration.mongoClient(), clientName);
        Map<String,Map<String,List<Document>>> returnDataToClient =new HashMap<>();
        Map<String,List<Document>> collectionData=new HashMap<>();

        Query query = new Query();
        query.fields().exclude("_id","page-name","clientName");

        for(int i=0;i<sitePaths.size();i++){
            String sitePath=sitePaths.get(i).get("sitePath").asText();
            query.addCriteria(Criteria.where("page-name").is(sitePath));
            List<Document> allData=mongoUtilities.getAllDataFromAEM(query,Constants.AEM_COLLECTION_NAME,clientMongoTemplate);
            if (allData==null || allData.isEmpty()){
                return null;
            }
            collectionData.put(sitePath,allData);
        }
        returnDataToClient.put(Constants.AEM_DATA_NAME,collectionData);
        return returnDataToClient;
    }

    public Map<String,String> updateData(ObjectNode siteData,String clientName) {
        MongoTemplate clientMongoTemplate=mongoUtilities.mongoTemplate(mongoConfiguration.mongoClient(), clientName);

        Document document=objectMapper.convertValue(siteData,Document.class);
        Map<String,String> status=new LinkedHashMap<>();
        if (mongoUtilities.updateDocumentAEM(document,clientMongoTemplate)){
            status.put("update","success");
        }
        else {
            status.put("update", "failed");
            status.put("response","No page found with the specified pagePath");
        }
        return status;
    }


    public Map<String,Map<String,Map<String,List<Document>>>> getAllData(String clientName) {

        MongoTemplate clientMongoTemplate=mongoUtilities.mongoTemplate(mongoConfiguration.mongoClient(), clientName);

        Query query = new Query();
        query.fields().exclude("_id","clientName");

        List<Document> documentList=mongoUtilities.getAllDataFromAEM(query,Constants.AEM_COLLECTION_NAME,clientMongoTemplate);
        if(documentList.isEmpty()){
            return null;
        }

        Map<String,Map<String,Map<String,List<Document>>>> clientData=new HashMap<>();

        Map<String,Map<String,List<Document>>> collectionData=new LinkedHashMap<>();
        Map<String,List<Document>> pageData=new LinkedHashMap<>();

        for (Document document : documentList) {
            String pageName = document.getString("page-name");
            document.remove("page-name");
            List<Document> innerData = new ArrayList<>();

            if (pageData.containsKey(pageName)) {
                innerData = pageData.get(pageName);
                innerData.add(document);
                continue;
            }
            innerData.add(document);
            pageData.put(pageName, innerData);
        }
        collectionData.put(Constants.AEM_DATA_NAME,pageData);

        clientData.put(clientName,collectionData);

        return clientData;
    }

    public Map<String,String> clearData(String clientName) {
        MongoTemplate clientMongoTemplate=mongoUtilities.mongoTemplate(mongoConfiguration.mongoClient(), clientName);

        Map<String,String> response=new LinkedHashMap<>();
        if(clientName.isEmpty()){
            response.put("status","failed");
            response.put("response","clientId is empty");
            return response;
        }
        if (mongoUtilities.deleteDataAEM(clientName,clientMongoTemplate)){
            response.put("status","success");
            response.put("response","data found and cleared");
            return response;
        }
        response.put("status","failed");
        response.put("response","No data found with clientName : "+clientName);
        return response;
    }
}