import static controllers.routes.ref.Search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.*;

import play.mvc.*;
import play.test.*;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.RequiredValidator;
import play.i18n.Lang;
import play.libs.F;
import play.libs.F.*;
import play.Logger;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;
import play.mvc.Http.Request;
import static play.mvc.Http.Status.OK;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest {

    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertThat(a).isEqualTo(2);
    }

    @Test
    public void renderTemplate() {
        Content html = views.html.index.render("Your new application is ready.");
        assertThat(contentType(html)).isEqualTo("text/html");
        assertThat(contentAsString(html)).contains("Your new application is ready.");
    }
    
    @Test
    public void occurrenceSearchCheck() {
        running(fakeApplication(), new Runnable(){
            public void run() {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("dwc_scientificName","Hercostomus nobilitatus");
                parameters.put("dwc_continent", "Europe");
                parameters.put("dwc_country", "Sweden");
                parameters.put("page","0");
                parameters.put("size","10");
 
                FakeRequest fakeRequest = new FakeRequest(POST, "/api/search/occurrences")
                        .withHeader("Content-Type", "application/x-www-form-urlencoded")
                        ;
                for(String key : parameters.keySet()) {
                    fakeRequest.withHeader(key, parameters.get(key));
                }

                Result result = callAction(controllers.routes.ref.Search.searchOccurrences(), 
                        fakeRequest);
                Logger.debug(contentAsString(result));
                int responseCode = status(result);
                assertThat(responseCode).isEqualTo(OK);
                
            }
        });
    }
    
    @Test
    public void boundingBoxSearchCheck() {
        running(fakeApplication(), new Runnable() {

            public void run() {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("dwc_scientificName","Agenioideus cinctellus");
                parameters.put("bb_ne", "60,60");
                parameters.put("bb_sw", "10,10");
                parameters.put("page","0");
                parameters.put("size","10");     
                
                FakeRequest fakeRequest = new FakeRequest(POST, "/api/search/occurrences")
                        .withHeader("Content-Type", "application/x-www-form-urlencoded")
                        ;
                for(String key : parameters.keySet()) {
                    fakeRequest.withHeader(key, parameters.get(key));
                }

                Result result = callAction(controllers.routes.ref.Search.searchOccurrences(), 
                        fakeRequest);
                Logger.debug(contentAsString(result));
                int responseCode = status(result);
                assertThat(responseCode).isEqualTo(OK);                
                
            }
        });
    }
    
    @Test
    public void numberOfDocumentsCheck() {
        running(fakeApplication(), new Runnable() {
            
            public void run() {
                FakeRequest fakeRequest = new FakeRequest(GET, "/api/izeure/size");
                
                Result result = callAction(controllers.routes.ref.Search.numberOfDocuments(),
                        fakeRequest);
                
                Logger.debug(contentAsString(result));
                int responseCode = status(result);
                assertThat(responseCode).isEqualTo(OK);                
            }
        });
    }
}
