package com.developination;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.developination.fitnotes2fit.FitNotesParser.FitNotesParser;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Fitnotes exercises in the map right now: " + FitNotesParser.EXERCISE_TO_FIT_CATEGORY_MAP.keySet().toString();
    }
}