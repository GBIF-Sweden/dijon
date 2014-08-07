/**
 * @author: Marie-Elise Lecoq
 * @author: Mickaël Graf
 */
package controllers;

import play.mvc.*;

public class Cors extends Controller{

    private static Result groundCors() {
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setHeader("Access-Control-Allow-Headers", "Content-Type");
        response().setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        return ok();        
    }
    public static Result cors() {
        return groundCors();
    }
    public static Result cors1(String params) {
        return groundCors();
    }
    public static Result cors2(String param1, String param2) {
        return groundCors();
    }
    public static Result cors3(String param1, String param2, String param3) {
        return groundCors();
    }
    public static Result cors4(String param1, String param2, String param3, String param4) {
        return groundCors();
    }
    public static Result cors5(String param1, Integer param2) {
        return groundCors();
    }
    
}