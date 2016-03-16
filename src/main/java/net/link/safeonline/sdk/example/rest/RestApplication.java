package net.link.safeonline.sdk.example.rest;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;


@ApplicationPath("restv1")
public class RestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {

        Set<Class<?>> classes = new HashSet<Class<?>>();

        //
        // Resources
        //
        classes.add( LinkIDResource.class );

        return classes;
    }
}
