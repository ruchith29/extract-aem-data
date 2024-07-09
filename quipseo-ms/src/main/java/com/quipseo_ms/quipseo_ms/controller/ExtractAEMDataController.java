package com.quipseo_ms.quipseo_ms.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quipseo_ms.quipseo_ms.services.AEMConnection;
import com.quipseo_ms.quipseo_ms.services.ExtractAEMDataService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@RestController
public class ExtractAEMDataController {

    @Autowired
    private AEMConnection aemConnection;

    @Autowired
    private ExtractAEMDataService aemDataService;

    @Autowired
    private ObjectMapper objectMapper;

    // to get data from AEM page
    @GetMapping("/quip/v2/seo")
    public ResponseEntity<JsonNode> getAEMData(@RequestParam String sitePath) {
        if (sitePath.trim().isEmpty()){
            Map<String,String> response=new LinkedHashMap<>();
            response.put("status","failed");
            response.put("response","site-path is empty.");
            return new ResponseEntity<>(objectMapper.convertValue(response, JsonNode.class),HttpStatus.BAD_REQUEST);
        }
        try {
            URL url=new URL(sitePath);
            String AEMPageData=aemConnection.getConnection(url);
            if(Objects.equals(AEMPageData, "") || AEMPageData==null){
                Map<String,String> response=new LinkedHashMap<>();
                response.put("status","failed");
                response.put("response","site-path is invalid.");
                return new ResponseEntity<>(objectMapper.convertValue(response, JsonNode.class),HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(objectMapper.readTree(AEMPageData),HttpStatus.FOUND);
        }
        catch (Exception exception){
            Map<String,String> response=new LinkedHashMap<>();
            response.put("status","failed");
            response.put("response","invalid url: "+exception.getClass().getSimpleName());
            return new ResponseEntity<>(objectMapper.convertValue(response, JsonNode.class),HttpStatus.NOT_FOUND);
        }
    }

    // to store the page data for client
    @PostMapping("/quip/v2/seo")
    public ResponseEntity<ObjectNode> postData(@RequestParam String sitePath,@RequestParam String clientName) throws IOException {
        if (sitePath.trim().isEmpty()){
            Map<String,String> response=new HashMap<>();
            response.put("status","failed");
            response.put("response","site-path is empty.");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class),HttpStatus.BAD_REQUEST);
        }
        try {
            URL url=new URL(sitePath);
            if(Objects.equals(aemConnection.getConnection(url), "") || aemConnection.getConnection(url)==null){
                Map<String,String> response=new LinkedHashMap<>();
                response.put("status","failed");
                response.put("response","site-path is Invalid.");
                return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class),HttpStatus.NOT_ACCEPTABLE);
            }
            return new ResponseEntity<>(objectMapper.convertValue(aemDataService
                    .storeData(aemConnection.getConnection(url),clientName),ObjectNode.class),HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String,String> response=new LinkedHashMap<>();
            response.put("status","failed");
            response.put("response","invalid url: "+exception.getClass().getSimpleName());
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class),HttpStatus.NOT_FOUND);
        }

    }

    // update single doc or page data for client
    @PutMapping("/quip/v2/seo")
    public ResponseEntity<ObjectNode> updateData(@RequestBody ObjectNode siteData,@RequestParam String clientName){

        if (siteData.get("pagePath|PagePath")==null ){
            Map<String,String> response=new LinkedHashMap<>();
            response.put("status","failed");
            response.put("response","given data doesn't contain pagePath.");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class),HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(objectMapper.convertValue(aemDataService
                .updateData(siteData,clientName),ObjectNode.class),HttpStatus.OK);
    }

    // to get data of each specified page of client
    @GetMapping("/quip/v2/seo/getSpecifiedData")
    public ResponseEntity<ObjectNode> getSpecifiedData(@RequestBody ArrayNode sitePath,@RequestParam String clientName){
        Map<String,Map<String,List<Document>>> databaseData=aemDataService.getSpecificDataFromDB(sitePath,clientName);
        if(databaseData==null || databaseData.isEmpty()){
            Map<String,String> response=new LinkedHashMap<>();
            response.put("status","failed");
            response.put("response","Found no data.");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class),HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(objectMapper.convertValue(databaseData,ObjectNode.class),HttpStatus.FOUND);
    }

    // to get all pages data for client
    @GetMapping("/quip/v2/seo/getAllData")
    public ResponseEntity<ObjectNode> getAllData(@RequestParam String clientName){
        Map<String,Map<String,Map<String,List<Document>>>> data=aemDataService.getAllData(clientName);
        if(data==null){
            Map<String,String> response=new LinkedHashMap<>();
            response.put("status","failed");
            response.put("response","no data found for the client: "+clientName);
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class),HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(objectMapper.convertValue(data,ObjectNode.class),HttpStatus.FOUND);
    }

    // delete data for a particular client
    @DeleteMapping("/quip/v2/seo")
    public ResponseEntity<ObjectNode> deleteData(@RequestBody ObjectNode status,@RequestParam String clientName){
        String data=status.get("status").asText();
        if (data.equals("success")){
            Map<String,String> response=aemDataService.clearData(clientName);
            if (response.get("status").equals("failed")){
                return new ResponseEntity<>(objectMapper.convertValue(response,ObjectNode.class),HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(objectMapper.convertValue(response,ObjectNode.class),HttpStatus.OK);
        }
        Map<String,String> response=new LinkedHashMap<>();
        response.put("status","failed");
        response.put("response","data cannot be cleared.");
        return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class),HttpStatus.NOT_FOUND);
    }
}