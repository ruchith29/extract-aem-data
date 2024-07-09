package com.quipseo_ms.quipseo_ms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quipseo_ms.quipseo_ms.services.DOMScrappingService;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DOMScrappingController {

    @Autowired
    private DOMScrappingService domScrappingService;
    private final Logger logger= LoggerFactory.getLogger(DOMScrappingController.class);

    @GetMapping("/quip/v2/domScrapping")
    public  ResponseEntity<ObjectNode> domScrapping(@RequestParam String url) throws IOException, URISyntaxException {
        ObjectMapper objectMapper=new ObjectMapper();

        logger.info("domScrapping(): Validation of the given url started.");
        UrlValidator urlValidator = new UrlValidator();

        if(!urlValidator.isValid(url)){
            logger.error("domScrapping(): Given url is not valid.");
            Map<String, String> response = new HashMap<>();
            response.put("status", "failed");
            response.put("errorCode",""+ HttpStatus.BAD_REQUEST.value());
            response.put("errorMessage", "Given url is not valid.");

            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.BAD_REQUEST);
        }

        Document document;
        try{
            document=Jsoup.connect(url).get();
            logger.info("domScrapping(): Connection established with webpage successfully.");
        }
        catch (Exception exception) {
            logger.error("No webpage found with the given URL.");
            Map<String, String> response = new HashMap<>();
            response.put("status", "failed");
            response.put("errorCode",""+ HttpStatus.BAD_REQUEST.value());
            response.put("errorMessage", "No webpage found with the given URL.");
            return new ResponseEntity<>(objectMapper.convertValue(response, ObjectNode.class), HttpStatus.BAD_REQUEST);
        }

        logger.info("domScrapping(): Given url is valid.");

        return new ResponseEntity<>(objectMapper.convertValue(domScrappingService.domScrapping(document,url), ObjectNode.class),HttpStatus.OK);
    }
}