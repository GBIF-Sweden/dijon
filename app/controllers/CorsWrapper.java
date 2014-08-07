/**
 * @author: Marie-Elise Lecoq
 */
package controllers;
 
import play.mvc.*;
import play.libs.F;
 
 
public class CorsWrapper extends play.mvc.Action.Simple {
    public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable {
        ctx.response().setHeader("Access-Control-Allow-Origin", "*");
        ctx.response().setHeader("Access-Control-Allow-Headers", "Content-Type");
        return delegate.call(ctx);
    }  
}

