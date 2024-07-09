package com.quipseo_ms.quipseo_ms.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quipseo_ms.quipseo_ms.services.DomainService;
import org.apache.commons.validator.routines.UrlValidator;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
public class DomainController {

    @Autowired
    private DomainService urlService;

    Logger logger = LoggerFactory.getLogger(DomainController.class);
    ObjectMapper objectMapper = new ObjectMapper();

    // create a domain
    @PostMapping("/quip/v2/createDomain")
    public ResponseEntity<ObjectNode> createDomain(@RequestBody ObjectNode data) throws IOException {

        Map<String, String> domainData = objectMapper.convertValue(data, new TypeReference<Map<String, String>>(){});

        logger.info("createDomain(): is initiated.");
        Map<String, String> response = new LinkedHashMap<>();

        //Validation
        logger.info("createDomain(): Validation of given data is started.");
        UrlValidator urlValidator = new UrlValidator();

        if (domainData.get("title").trim().isEmpty()) {
            logger.error("createDomain(): Title is empty.");

            response.put("status", "Failed");
            response.put("errorCode", "" + HttpStatus.BAD_REQUEST.value());
            response.put("errorMessage", "Data Invalid. Title cannot be empty.");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.BAD_REQUEST);
        }
        if (domainData.get("url").trim().isEmpty()) {
            logger.error("createDomain(): URL is empty.");

            response.put("status", "Failed");
            response.put("errorCode", "" + HttpStatus.BAD_REQUEST.value());
            response.put("errorMessage", "Data Invalid. URL cannot be empty.");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.BAD_REQUEST);
        }

        if (!(urlValidator.isValid(domainData.get("url")))) {
            logger.error("createDomain(): Given URL is not valid.");

            response.put("status", "Failed");
            response.put("errorCode", "" + HttpStatus.BAD_REQUEST.value());
            response.put("errorMessage", "Data Invalid. Please enter valid URL.");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.BAD_REQUEST);
        }

        logger.info("createDomain(): Validation of the given data is done.");

        String id = urlService.postDomain(domainData);

        if (id == null || id.isEmpty()) {
            logger.info("createDomain(): Given Title or URL already exits. Domain creation is unsuccessful.");

            response.put("status", "Failed");
            response.put("errorCode", "" + HttpStatus.BAD_REQUEST.value());
            response.put("errorMessage", "Domain is creation is failed as Title or URL already exists. Try using another values.");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.BAD_REQUEST);
        }

        logger.info("createDomain(): Domain is posted successfully, and user is returned with Id.");

        response.put("status", "Success");
        response.put("response", "Domain created successfully. Domain can be accessed by Id: " + id);
        return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.CREATED);
    }

    // fetch domain based on ID
    @GetMapping("/quip/v2/getDomainById")
    public ResponseEntity<ObjectNode> getDomainByID(@RequestBody ObjectNode data) {
        logger.info("getDomainById(): is initiated.");

        Map<String,String> domainData= objectMapper.convertValue(data, new TypeReference<Map<String, String>>(){});
        String id = domainData.get("id");
        Map<String, String> response = new LinkedHashMap<>();

        // validation
        logger.info("getDomainById(): Validation is initiated for the given Id.");
        if (id == null || id.isEmpty()) {
            logger.warn("getDomainById(): Id is NULL.");
            response.put("status", "Failed");
            response.put("errorCode", "" + HttpStatus.BAD_REQUEST.value());
            response.put("errorMessage", "Id is NULL.");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.BAD_REQUEST);
        }

        if (id.length() != 24 && !(ObjectId.isValid(id))) {
            logger.warn("getDomainById(): Given Id is Invalid.");
            response.put("status", "Failed");
            response.put("errorCode", "" + HttpStatus.BAD_REQUEST.value());
            response.put("errorMessage", "Invalid Id. Enter a valid Id.");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.BAD_REQUEST);
        }

        logger.info("getDomainById(): Validation is done successfully.");

        Document domain = urlService.getDomainByID(id);

        if (domain == null) {
            logger.error("getDomainById(): No Domain found with the specified Id.");
            response.put("status", "Failed");
            response.put("errorCode", "" + HttpStatus.NOT_FOUND.value());
            response.put("errorMessage", "No Domain found with the specified Id.");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.NOT_FOUND);
        }

        domain.remove("_id");
        logger.info("getDomainById(): Domain found and returned to user.");
        return new ResponseEntity<>(objectMapper.convertValue(domain, ObjectNode.class), HttpStatus.OK);
    }

    // fetch all the domains from the database
    @GetMapping("/quip/v2/getAllDomains")
    public ResponseEntity<ObjectNode> getAllDomains() {
        logger.info("getAllDomains(): is initiated.");
        Map<String, String> response = new LinkedHashMap<>();

        logger.info("getAllDomains(): Fetching all the domains from database.");
        List<Document> data = urlService.getAllDomains();

        if ((data.isEmpty())) {
            logger.warn("getAllDomains(): No domains in the database.");
            response.put("status", "Failed");
            response.put("errorCode", "" + HttpStatus.NOT_FOUND.value());
            response.put("errorMessage", "No domains available in the collection.");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.NOT_FOUND);
        }

        logger.info("getAllDomains(): All the domains from the database are fetched and return to the user.");
        return new ResponseEntity<>(objectMapper.convertValue(new Document("domains", data), ObjectNode.class), HttpStatus.OK);
    }

    // update domain based on id
    @PutMapping("/quip/v2/updateDomain")
    public ResponseEntity<ObjectNode> updateDomain(@RequestBody ObjectNode data) {
        Map<String, String> newDocument=objectMapper.convertValue(data, new TypeReference<Map<String, String>>(){});
        Map<String, String> response = new LinkedHashMap<>();
        logger.info("updateDomain(): is initiated");

        UrlValidator urlValidator = new UrlValidator();
        String id = newDocument.get("_id");

        // validation
        logger.info("updateDomain(): Validation of given data is started.");
        if (id == null|| id.isEmpty()) {
            logger.warn("updateDomain(): Id is NULL.");
            response.put("status", "Failed");
            response.put("errorCode", "" + HttpStatus.BAD_REQUEST.value());
            response.put("errorMessage", "Id is NULL.");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.BAD_REQUEST);
        }

        if (id.length() != 24 && !(ObjectId.isValid(id))) {
            logger.warn("updateDomain(): Id is Invalid.");
            response.put("status", "Failed");
            response.put("errorCode", "" + HttpStatus.BAD_REQUEST.value());
            response.put("errorMessage", "Invalid Id. Enter a valid Id.");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.BAD_REQUEST);
        }

        if (newDocument.containsKey("url")) {
            String url = newDocument.get("url");
            if (url.isEmpty() || (!(urlValidator.isValid(newDocument.get("url"))))) {
                logger.warn("updateDomain(): Given URL is Invalid.");
                response.put("status", "Failed");
                response.put("errorCode", "" + HttpStatus.BAD_REQUEST.value());
                response.put("errorMessage", "Invalid URL.");
                return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.BAD_REQUEST);
            }
        }

        if (newDocument.containsKey("title")) {
            String title = newDocument.get("title");
            if (title.isEmpty()) {
                logger.warn("updateDomain(): Given Title is Invalid.");
                response.put("status", "Failed");
                response.put("errorCode", "" + HttpStatus.BAD_REQUEST.value());
                response.put("errorMessage", "Invalid Title.");
                return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.BAD_REQUEST);
            }
        }
        logger.info("updateDomain(): Validation is done successfully.");

        if (urlService.updateDomain(newDocument)) {
            logger.info("updateDomain(): Domain is updated.");
            response.put("status", "Success");
            response.put("response", "Domain updated successfully!");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.ACCEPTED);
        }
        logger.warn("updateDomain(): Domain couldn't get update as no domain found by given Id.");
        response.put("status", "Failed");
        response.put("errorCode", "" + HttpStatus.NOT_FOUND.value());
        response.put("errorMessage", "No domain found by given Id.");
        return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.NOT_FOUND);
    }

    // delete domain based on id
    @DeleteMapping("/quip/v2/deleteDomains")
    public ResponseEntity<ObjectNode> deleteDomain(@RequestBody ObjectNode data) {
        logger.info("deleteDomains(): is initiated.");

        JsonNode list=data.get("domains");
        int totalLength = list.size();
        int validCount = 0;
        int executionCount=0;
        Map<String,Map<String,String>> deleteResponse=new LinkedHashMap<>();
        for (JsonNode objectNode : list) {
            executionCount++;
            String id = objectNode.get("id").asText();
            Map<String, String> response = new LinkedHashMap<>();
            // validation
            if (id.isEmpty()) {
                logger.warn("deleteDomains(): Id is NULL.");
                response.put("status", "Failed");
                response.put("errorCode", "" + HttpStatus.NO_CONTENT.value());
                response.put("errorMessage", "Id is NULL.");
                deleteResponse.put("Id at index: "+executionCount,response);
                continue;
            }
            if (id.length() != 24 && !(ObjectId.isValid(id))) {
                logger.warn("deleteDomains(): Id is Invalid.");
                response.put("status", "Failed");
                response.put("errorCode", "" + HttpStatus.BAD_REQUEST.value());
                response.put("errorMessage", "Invalid Id. Id is: "+id);
                deleteResponse.put("Id at index: "+executionCount,response);
                continue;
            }

            if (urlService.deleteDomain(id)) {
                logger.info("deleteDomains():{} Domain is deleted", id);
                validCount++;
                response.put("status", "Success");
                response.put("message", "Id is: "+id);
                response.put("response", "Domain successfully deleted.");
                deleteResponse.put("Id at index: "+executionCount,response);
            }
            else {
                logger.info("deleteDomains(): No domain found with given Id: {}", id);
                response.put("status","Failed");
                response.put("errorCode",""+HttpStatus.NOT_FOUND.value());
                response.put("response","No domain found with given Id. Id is: "+id);
                deleteResponse.put("Id at index: "+executionCount,response);
            }
        }
        if(totalLength==validCount){
            logger.info("deleteDomain(): All domains are deleted successfully.");
            Map<String,String> values=new LinkedHashMap<>();
            logger.info("deleteDomain(): Domain is deleted");
            values.put("status", "Success");
            values.put("response", "All Domains successfully deleted.");
            values.put("deletedCount",""+validCount);
            return new ResponseEntity<>(objectMapper.convertValue(values, ObjectNode.class), HttpStatus.OK);
        }
        logger.info("deleteDomain(): Few domains are deleted successfully and rest all are Invalid.");
        return new ResponseEntity<>(objectMapper.convertValue(deleteResponse, ObjectNode.class), HttpStatus.PARTIAL_CONTENT);
    }

}