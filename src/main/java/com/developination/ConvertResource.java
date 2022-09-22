package com.developination;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.developination.fitnotes2fit.FitNotesParser.FitNotesParser;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("")
public class ConvertResource {
    @Inject
    ConvertService convertService;
    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance hello(Object[] exercises);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/index")
    public TemplateInstance hello() {
        return Templates.hello(FitNotesParser.EXERCISE_TO_FIT_CATEGORY_MAP.keySet().toArray());
    }

    @POST
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/convert")
    public Response convert(@MultipartForm MultipartFormDataInput data) {
        Response.ResponseBuilder response = Response.ok();
        String message = convertService.convert(data);
        return response.status(201).entity(message).build();
    }
}
