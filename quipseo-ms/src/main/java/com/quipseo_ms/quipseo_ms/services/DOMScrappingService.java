package com.quipseo_ms.quipseo_ms.services;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DOMScrappingService {

    public Map<String, Object> domScrapping(Document document,String url) throws IOException, URISyntaxException {
        Map<String, Object> totalData = new LinkedHashMap<>();

        URI uri=new URI(url);

        Map<String,String> urlInfo=new LinkedHashMap<>();
        urlInfo.put("URL",url);
        urlInfo.put("URL Scheme", uri.getScheme());
        urlInfo.put("Domain", uri.getHost());
        urlInfo.put("Path", uri.getPath());
        urlInfo.put("Query", uri.getQuery());

        ArrayList<Map<String,String>> urlData=new ArrayList<>();
        urlData.add(urlInfo);

        List<Map<String, Object>> headerData = processElements(document.select("h1,h2,h3,h4,h5,h6"));

        List<Map<String, Object>> linkData = processElements(document.select("a"));

        totalData.put("URL structure ",urlData);
        totalData.put("headings", headerData);
        totalData.put("links", linkData);

        return totalData;
    }

    public List<Map<String, Object>> processElements(Elements elements) {
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (Element element : elements) {
            Map<String, Object> elementData = new LinkedHashMap<>();
            elementData.put("tag", element.tagName());
            elementData.put("content", element.text());

            if (element.tagName().equals("a")) {
                for (Attribute attribute : element.attributes()) {
                elementData.put(attribute.getKey(), attribute.getValue());
                }
            }

            dataList.add(elementData);
        }
        return dataList;
    }
}
