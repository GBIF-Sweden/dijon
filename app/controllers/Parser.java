/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import play.Logger;

/**
 * Class for parsing a HTTP request and returning a query string for Solr
 * @author korbinus
 */
public class Parser {
    
    ArrayList<String> parameters = new ArrayList<String>();
    Integer page = 0;                               // default value
    Integer size = 100;                             // default value
    String separator = ",";                         // separator for sorting columns
    boolean bbox = false;

    // default values for sorting: scientific name ascending
//    String sort = "dwc_scientificName";
    List<String> sortColumns = new ArrayList<String>();
//    sortColumns.add("dwc_scientificName");
//    sort.add(0,"dwc_scientificName");
    String order = "asc";                           // default sort order
    
    /**
     * Constructor from JSON object (typically from a POST request)
     * @param json
     * @throws IllegalArgumentException 
     */
    public Parser (JsonNode json) throws IllegalArgumentException {
        Logger.info("POST Request: "+json.toString());

        Map<String, String> map = new HashMap<String, String>();
        // first we convert the json to a map
        for(Iterator<String> iterator = json.fieldNames(); iterator.hasNext();){
            String key = iterator.next();
            String value = json.get(key).asText();
            Logger.debug(key+":"+value);
            // IE sends empty parameters and this results in queries returning 
            // 0 result.
            if(value.length() > 0) {
                map.put(key, key+":\""+value+"\"");
            }
        }

        extractParameters(map);
    }

    /**
     * 
     * @param entries 
     */
    public Parser (Set<Map.Entry<String,String[]>> entries) {
        Logger.info("GET Request: "+entries.toString());
        
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String,String[]> entry : entries) {
            String[] values = entry.getValue();
            String key = entry.getKey();
            for(String value : values) {
                // IE sends empty parameters and this results in queries returning 
                // 0 result.
                if(value.length() > 0) {
                    map.put(key,key+":\""+value+"\"");
                    Logger.debug(key + " " + value);
                }
            }
        }    
        
        extractParameters(map);
    }
    
    /**
     * 
     * @param map 
     */
    private void extractParameters(Map<String, String>map){
        // Grab the specific stuff and remove it from parameters
        // that will free us from a lot of "if" in the loop in the contructor
        if(map.containsKey("bb_ne") && map.containsKey("bb_sw")){
            String bb_ne = cleanseValue(map, "bb_ne");
            String bb_sw = cleanseValue(map, "bb_sw");
            map.put("point_idx","point_idx:[" + bb_sw+ " TO " + bb_ne + "]");
            map.remove("bb_ne");
            map.remove("bb_sw");
        } 
        // transforms the date(s) into stuff that's respectful to schema.xml in solr
        if(map.containsKey("eventDateLow") && map.containsKey("eventDateHigh")){
            String eventDateLow = cleanseValue(map, "eventDateLow");
            String eventDateHigh = cleanseValue(map, "eventDateHigh");
            map.put("dwc_eventDate", "dwc_eventDate:[" + eventDateLow + " TO " + eventDateHigh + "]");
            map.remove("eventDateLow");
            map.remove("eventDateHigh");
        } else if(map.containsKey("eventDateLow")) {
            map.put("dwc_eventDate", "dwc_eventDate:\"" + cleanseValue(map, "eventDateLow") + "\"");
            map.remove("eventDateLow");
        } else if(map.containsKey("eventDateHigh")) {               // it shouldn't be like this but we're nice
            map.put("dwc_eventDate", "dwc_eventDate:\"" + cleanseValue(map, "eventDateHigh") + "\"");
            map.remove("eventDateHigh");            
        }
        // get the sort
        if(map.containsKey("sort")) {
            String sort = cleanseValue(map, "sort");
            if(sort.indexOf(separator) > -1) {                      // if we have several sorting columns
                sortColumns = Arrays.asList(sort.split(separator));
            } else {                                                // otherwise only one sorting column
                sortColumns.add(sort);
            }
            map.remove("sort");
        } else {
            sortColumns.add("dwc_scientificName");                  // default sorting column
        }
        if(map.containsKey("order")){
            String order = cleanseValue(map, "order");
            map.remove("order");
        }
        if(map.containsKey("page")) {
            page = Integer.parseInt(cleanseValue(map, "page"));
            Logger.debug("page: "+page.toString());
        }
        if(map.containsKey(size)) {
            size = Integer.parseInt(cleanseValue(map, "size"));
            Logger.debug("size: "+size.toString());
        }

        map.remove("page");
        map.remove("size");
        
        parameters = new ArrayList<String>(map.values());
        Logger.debug("Number of parameters: "+parameters.size());        
    }
    
    private String cleanseValue(Map<String, String>map, String key) {
        return map.get(key).replace(key+":\"", "").replace("\"", "");
    }
    
    public String getQuery() {
        return StringUtils.join(parameters, " AND "); 
    }
    
    public Integer getPage() {
        return page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public boolean isBoundingBox() {
        return bbox;
    }
    
    public List<String> getSort() {
        return sortColumns;
    }
    
    public String getOrder() {
        return order;
    }
}
