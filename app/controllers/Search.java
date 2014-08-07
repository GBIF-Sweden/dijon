/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONException;
import org.json.JSONObject;
import play.Logger;
//import play.mvc.Results.Chunks;

import play.data.Form;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.WS;
import play.mvc.*;
import static play.mvc.Controller.response;
import static play.mvc.Results.ok;
import views.xml.*;

/**
 *
 * @author korbinus
 */
public class Search extends Controller {

    private static String solrURL = "http://localhost:8338/solr";          // change this URL or port according to your Solr installation
    private static SolrServer server = new HttpSolrServer(solrURL);

    /**
     * Make a solr search with the arguments parsed
     * @return JSONObject with the rows found in the index 
     * @throws JSONException 
     */
    @With(CorsWrapper.class)
    public static Result searchOccurrences() throws JSONException {

        Parser parser;
        
        try {
            parser = new Parser(request().body().asJson());            
        } catch(IllegalArgumentException e) {
            Logger.error("ERR-0312: IllegalArgumentException");
            Logger.error(e.getMessage());
            return badRequest("ERR-0312");
        }

        SolrQuery query = new SolrQuery();

        Logger.debug("Solr query: "+parser.getQuery());
        
        query.setQuery(parser.getQuery())
                .setParam("fl","dwc_occurrenceID, dwc_catalogNumber, dwc_basisOfRecord,"
                        + " dwc_scientificName, dwc_country, dwc_stateProvince, dc_modified,"
                        + " dwc_eventDate, dwc_recordedBy, dwc_fieldNumber, "
                        + " bourgogneVersion, point_idx, "
                        + " dwc_institutionCode, dwc_collectionCode, "
                        + " dwc_decimalLatitude, dwc_decimalLongitude")
                .setStart(parser.getPage()*parser.getSize())
                .setRows(parser.getSize());
        
        // Manage the sorting options
        // Note: we can have several columns to sort after but only one order
        List<String> sort = parser.getSort();
        String order = parser.getOrder();
        for(String s : sort) {
            query.addSort(new SortClause(s,order));
        }
        
        try {
            QueryResponse rsp = server.query(query);
            SolrDocumentList docList = rsp.getResults();
//            Logger.debug("NumFound: "+ String.valueOf(docList.getNumFound()) );
            JSONObject returnResults = new JSONObject();
            Map<Integer, Object> solrDocMap = new HashMap<Integer, Object>();
            int counter = 1;
            for (Map singleDoc : docList) {
                solrDocMap.put(counter, new JSONObject(singleDoc));
                counter++;
            }
            returnResults.put("matches", String.valueOf(docList.getNumFound()));
            returnResults.put("response", solrDocMap);
            return ok(returnResults.toString());
        } catch (SolrServerException e) {
            Logger.error("Unable to reach the index");
            return ok("Unable to reach the index");
        }
    }

    /**
     * Returns the number of documents in the solr index
     */
    @With(CorsWrapper.class)
    public static Result numberOfDocuments() throws JSONException {
        JSONObject response = new JSONObject();
        long nbDocs = getNumberOfDocuments("*:*");
        if(nbDocs> -1)
            return ok(response.put("response",String.valueOf(nbDocs)).toString());
        else {
            return ok(response.put("response","Unable to reach the index").toString());
        }
    }
    
    /**
     * Return the number of documents responding to the query
     * @param query solr string containing the query
     * @return the number of documents found or -1 if error
     */
    private static long getNumberOfDocuments(String query){
        SolrQuery q = new SolrQuery(query);
        
        try {
            Logger.debug("Number of documents for: "+query);
            q.setRows(0);   // don't actually request any data
            long result = server.query(q).getResults().getNumFound();
            Logger.debug(result+" documents found");
            return result;
        } catch (SolrServerException e) {
            Logger.error("Unable to reach the index");
            return (long) -1;
        }
    }

    /**
     * Make a solr search with the arguments parsed
     * @return JSONObject with the rows found in the index 
     * @throws JSONException 
     */
    @With(CorsWrapper.class)
    public static Result XdownloadOccurrences() throws JSONException {
        // get the query and parse it
        Set<Map.Entry<String,String[]>> entries = request().queryString().entrySet();
        entries.remove("sort");
        entries.remove("order");
        entries.remove("page");
        entries.remove("size");
        final Parser parser = new Parser(entries);

        Date date = new Date();
        String fileName = "gbif-"+date.getTime()+".csv";
//        response().setHeader("content-type","text/csv");
        response().setContentType("text/csv; charset=utf-8");
        response().setHeader("content-disposition","attachement;filename='"+fileName+"'");    
        
        // fixed value for development. Replace with a query result when ready
        final long totalNumberOfRecords = getNumberOfDocuments(parser.getQuery());
        
        final int size = 100000;   
        
        Chunks<String> chunks = new StringChunks() {
            public void onReady(Chunks.Out<String> out) {

                String solrQuery;           
                HttpClient httpClient = new HttpClient();

                            
                // prepare the solr query
                String solr = solrURL + "/select?q=";
                try {
                    solr += URLEncoder.encode(parser.getQuery(),"UTF-8");
                } catch(UnsupportedEncodingException e) {
                    solr += parser.getQuery();
                }
                solr += "&fl=*&wt=csv&rows="+size;     
                
                long offset = 0;
                solrQuery = solr+"&start="+offset;
                
                while(offset < totalNumberOfRecords) {
                    
                    Logger.debug("Solr query: "+solrQuery);

                    GetMethod getMethod = new GetMethod(solrQuery);
                    try{
                        int status = httpClient.executeMethod(getMethod);
                        if(status == 200) {
//                            Logger.debug(getMethod.getResponseBodyAsString());
                            out.write(getMethod.getResponseBodyAsString());
                        }
                    }catch(Exception e){
                        Logger.debug(e.getMessage());
                        out.write("Exception encountered. Sorry.");
                    }
                    offset += size;
                    solrQuery = solr+"&start="+offset+"&csv.header=false";   //by default csv.header is on but we don't want it after first loop
                }
                out.close();
            }
        };
                
        // Serves this stream with 200 OK
        return ok(chunks);
    }    
    
    /**
     * Make a solr search with the arguments parsed
     * @return JSONObject with the rows found in the index 
     * @throws JSONException 
     */
    @With(CorsWrapper.class)
    public static Promise<Result> downloadOccurrences() {
        
        Parser parser;
        
        Set<Map.Entry<String,String[]>> entries = request().queryString().entrySet();
        entries.remove("sort");
        entries.remove("order");
        entries.remove("page");
        entries.remove("size");
        parser = new Parser(entries);

        String solr = solrURL + "/select?q=";
        String parameters="*:*";
        try{
            solr += URLEncoder.encode(parser.getQuery(),"UTF-8");
            parameters = parser.getQuery();
            Logger.debug("Query parameters: "+parameters);
        } catch(UnsupportedEncodingException e) {
            Logger.debug("Exception: " + e);
        }
        solr += "&rows=1000000";
        solr += "&fl=*&wt=csv";

        Logger.debug("Solr query: "+solr);

        Date date = new Date();
        String fileName = "gbif-"+date.getTime()+".csv";
        response().setHeader("content-type","text/csv: charset=utf-8");
        response().setHeader("content-disposition","attachement;filename='"+fileName+"'");            

        // we should actually limit the number of records with something like
        // String.valueOf(getNumberOfDocuments(parser.getQuery()))
        // but due to server limitation allow only 1 000 000 records
        final Promise<Result> resultPromise = WS.url(solrURL + "/select")
                .setQueryParameter("q", parameters)
                .setQueryParameter("start", "0")
                .setQueryParameter("rows", "1000000")
                .setQueryParameter("fl", "*")
                .setQueryParameter("wt", "csv")
                .get().map(
                new Function<WS.Response, Result>() {
                    public Result apply(WS.Response response) {
                        return ok(response.getBody());
                    }
                }
        );
        return resultPromise;
    }    
}
