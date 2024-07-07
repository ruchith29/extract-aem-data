package com.quipaem.extract.aem.data.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quipaem.extract.aem.data.service.AEMConnection;
import com.quipaem.extract.aem.data.service.AEMDataService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@RestController
public class AEMDataController {

    @Autowired
    private AEMConnection aemConnection;

    @Autowired
    private AEMDataService aemDataService;

    private static final ObjectMapper objectMapper=new ObjectMapper();

    // to get data from AEM page
    @GetMapping("/quip/v2/seo")
    public JsonNode getAEMData(@RequestParam String sitePath) {
        if (sitePath.trim().isEmpty()){
            Map<String,String> response=new LinkedHashMap<>();
            response.put("status","failed");
            response.put("response","site-path is empty.");
            return objectMapper.convertValue(response, JsonNode.class);
        }
        try {
            URL url=new URL(sitePath);
            String AEMPageData=aemConnection.getConnection(url);
            if(Objects.equals(AEMPageData, "") || AEMPageData==null){
                Map<String,String> response=new LinkedHashMap<>();
                response.put("status","failed");
                response.put("response","site-path is invalid.");
                return objectMapper.convertValue(response, JsonNode.class);
            }
            return objectMapper.readTree(AEMPageData);
        }
        catch (Exception exception){
            Map<String,String> response=new LinkedHashMap<>();
            response.put("status","failed");
            response.put("response","invalid url: "+exception.getClass().getSimpleName());
            return objectMapper.convertValue(response, JsonNode.class);
        }
    }

    // to store the page data for client
    @PostMapping("/quip/v2/seo")
    public ObjectNode postData(@RequestParam String sitePath,@RequestParam String clientName) throws IOException {
        if (sitePath.trim().isEmpty()){
            Map<String,String> response=new HashMap<>();
            response.put("status","failed");
            response.put("response","site-path is empty.");
            return objectMapper.convertValue(response, ObjectNode.class);
        }
        try {
            URL url=new URL(sitePath);
            if(Objects.equals(aemConnection.getConnection(url), "") || aemConnection.getConnection(url)==null){
                Map<String,String> response=new LinkedHashMap<>();
                response.put("status","failed");
                response.put("response","site-path is Invalid.");
                return objectMapper.convertValue(response, ObjectNode.class);
            }
            return objectMapper.convertValue(aemDataService.storeData(aemConnection.getConnection(url),clientName),ObjectNode.class);
        }
        catch (Exception exception){
            Map<String,String> response=new LinkedHashMap<>();
            response.put("status","failed");
            response.put("response","invalid url: "+exception.getClass().getSimpleName());
            return objectMapper.convertValue(response, ObjectNode.class);
        }

    }

    // update single doc or page data for client
    @PutMapping("/quip/v2/seo")
    public ObjectNode updateData(@RequestBody ObjectNode siteData,@RequestParam String clientName){

        if (siteData.get("pagePath|PagePath")==null ){
            Map<String,String> response=new LinkedHashMap<>();
            response.put("status","failed");
            response.put("response","given data doesn't contain pagePath.");
            return objectMapper.convertValue(response, ObjectNode.class);
        }
        return objectMapper.convertValue(aemDataService.updateData(siteData,clientName),ObjectNode.class);
    }

    // to get data of each specified page of client
    @GetMapping("/quip/v2/seo/getSpecifiedData")
    public ObjectNode getSpecifiedData(@RequestBody ArrayNode sitePath,@RequestParam String clientName){
        Map<String,Map<String,List<Document>>> databaseData=aemDataService.getData(sitePath,clientName);
        if(databaseData==null || databaseData.isEmpty()){
            Map<String,String> response=new LinkedHashMap<>();
            response.put("status","failed");
            response.put("response","Found no data.");
            return objectMapper.convertValue(response, ObjectNode.class);
        }
        return objectMapper.convertValue(databaseData,ObjectNode.class);
    }

    // to get all pages data for client
    @GetMapping("/quip/v2/seo/getAllData")
    public ObjectNode getAllData(@RequestParam String clientName){
        Map<String,Map<String,Map<String,List<Document>>>> data=aemDataService.getAllData(clientName);
        if(data==null){
            Map<String,String> response=new LinkedHashMap<>();
            response.put("status","failed");
            response.put("response","no data found for the client: "+clientName);
            return objectMapper.convertValue(response, ObjectNode.class);
        }
        return objectMapper.convertValue(data,ObjectNode.class);
    }

    // delete data for a particular client
    @DeleteMapping("/quip/v2/seo")
    public ObjectNode deleteData(@RequestBody ObjectNode status,@RequestParam String clientName){
        String data=status.get("status").asText();
        if (data.equals("success")){
            return aemDataService.clearData(clientName);
        }
        Map<String,String> response=new LinkedHashMap<>();
        response.put("status","failed");
        response.put("response","data cannot be cleared.");
        return objectMapper.convertValue(response, ObjectNode.class);
    }
}