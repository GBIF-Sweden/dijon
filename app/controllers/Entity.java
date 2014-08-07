/**
 * Class for reading entity data from bourgogne database.
 * @author: korbinus (Mickaël Graf)
 */
package controllers;

import se.gbif.bourgogne.PID;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import play.mvc.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import views.xml.*;

import se.gbif.bourgogne.models.*;
import se.gbif.bourgogne.*;
import se.gbif.bourgogne.utilities.Gz;

public class Entity extends Controller {
	
	private static final int JSON_FORMAT = 1;
	private static final int XML_FORMAT = 2;
	
	private static final String OUT_OF_RANGE = "Error: out of range";
	
	private static final String MIME_JSON = "application/json";
	private static final String MIME_XML = "text/xml";
	
	public static String baseUrl = "/api/occurrence/";
	
	private static Integer bodySize;

	private static String grabRawEntityBody(String strId) throws IOException {
		// convert UUID to byte[]
		PID id = new PID(strId);
		
		// get the relevant record
		Entities entity = Entities.find.byId(id.toBytes());
		
		return Gz.decompress(entity.body);
	}

	private static String getVersion(String body, Integer version) throws JSONException {

		JSONObject root = new JSONObject(body);
		bodySize = root.length();
		
		if(version == 0 || version > bodySize) {
			return OUT_OF_RANGE;
		}
                
		return root.getJSONObject(version.toString()).toString();
		
	}

        @With(CorsWrapper.class)
        public static Result lastOccurrenceVersion(String strId) {
            try {
                String body = grabRawEntityBody(strId);
                JSONObject root = new JSONObject(body);
		bodySize = root.length();
                
                JSONObject response = new JSONObject();
                response.put("id",strId);
                response.put("version", bodySize.toString());
                
                return ok(response.toString()).as(MIME_JSON);
                
            } catch (Exception e) {
			return ok("Error: " + e.getMessage());
            }
        }
        
        @With(CorsWrapper.class)
	public static Result occurrenceJson(String strId, Integer version) {
		
		try {
			String body = grabRawEntityBody(strId);
			String dwrString = getVersion(body, version);

			if(dwrString.equals(OUT_OF_RANGE)){
				return redirect(baseUrl + strId + "/" + bodySize + "/json");
			}
			return ok(dwrString,"utf-8").as(MIME_JSON);
		} catch (Exception e) {
			return ok("Error: " + e.getMessage());
		}
	}
	
        @With(CorsWrapper.class)
	public static Result occurrenceXml(String strId, Integer version) {
		
		try {
			String body = grabRawEntityBody(strId);
			String dwrString = getVersion(body, version);

			if(dwrString.equals(OUT_OF_RANGE)){
				return redirect(baseUrl + strId + "/" + bodySize + "/xml");
			}
			JSONObject dwr = new JSONObject(dwrString);
			return ok(simpleDarwinRecordSet.render(org.json.XML.toString(dwr)), "utf-8").as(MIME_XML);
		} catch (Exception e) {
			return ok("Error: " + e.getMessage());
		}
	}
	
        @With(CorsWrapper.class)
	public static Result rawOccurrence(String strId) throws IOException {
		
		try {
			String body = grabRawEntityBody(strId);
			
			return ok(simpleDarwinRecordSet.render(
					"<warning>This document is for control ONLY. Don't expect any correct XML nor JSON.</warning>"
					+"<json-content>"
					+body
					+"</json-content>"
					), "utf-8").as(MIME_XML);
		} catch (Exception e) {
			return ok("Error: " + e.getMessage());
		}
		
	}
}

