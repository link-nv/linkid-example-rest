package net.link.safeonline.sdk.example.rest;

import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.auth.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.ws.linkid.LinkIDServiceClient;
import net.link.safeonline.sdk.api.ws.linkid.auth.*;
import net.link.safeonline.sdk.auth.filter.LinkIDLoginManager;
import net.link.safeonline.sdk.ws.linkid.LinkIDServiceClientImpl;
import net.link.util.logging.Logger;
import net.link.util.ws.security.username.AbstractWSSecurityUsernameTokenCallback;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.List;
import java.util.Map;


@Path("linkid")
public class LinkIDResource {

    private static final Logger logger = Logger.get(LinkIDResource.class);

    //linkID credentials
    private static final String linkIDAppName = "example-mobile";
    private static final String linkIDUsername = "example-mobile";
    private static final String linkIDPassword = "6E6C1CB7-965C-48A0-B2B0-6B65674BE19F";
    private static final String linkIDLocation = "https://demo.linkid.be/linkid-ws-username";

    // the message linkID client will display when log-in
    private static final String linkIDAuthenticationMessage = "Example REST app";

    // the message linkID client will display when logged in successful
    private static final String linkIDFinishMessage = "we did it, hurray!";

    @GET
    @Path("start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response start(@HeaderParam("user-agent") String userAgent) {

        logger.inf("Initiating linkID log-in");

        try {
            LinkIDServiceClient linkIDServiceClient = new LinkIDServiceClientImpl(
                    linkIDLocation,
                    null,
                    create(linkIDUsername, linkIDPassword)
            );

            LinkIDAuthenticationContext context = new LinkIDAuthenticationContext.Builder(linkIDAppName)
                    .authenticationMessage(linkIDAuthenticationMessage)
                    .finishedMessage(linkIDFinishMessage)
                    .build();

            LinkIDAuthSession linkIDAuthSession = linkIDServiceClient.authStart(context, userAgent);

            return Response.ok(linkIDAuthSession).build();

        } catch (LinkIDAuthException e) {
            logger.err("Something went wrong initiating the authentication session");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("poll")
    @Produces(MediaType.APPLICATION_JSON)
    public Response poll(@QueryParam("sessionId") String sessionId) {

        try {
            LinkIDServiceClient linkIDServiceClient = new LinkIDServiceClientImpl(
                    linkIDLocation,
                    null,
                    create(linkIDUsername, linkIDPassword)
            );

            LinkIDAuthPollResponse pollResponse = linkIDServiceClient.authPoll(sessionId, null);

            if (pollResponse.getLinkIDAuthenticationState() == LinkIDAuthenticationState.AUTHENTICATED) {

                LinkIDAuthnResponse linkIDAuthnResponse = pollResponse.getLinkIDAuthnResponse();

                if ( null != linkIDAuthnResponse ) {
                    logger.dbg("username: %s", linkIDAuthnResponse.getUserId());
                }

            }

            return Response.ok(pollResponse).cacheControl(CacheControl.valueOf("no-store")).build();

        } catch (LinkIDAuthPollException e) {
            logger.err("Something went wrong when polling");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

    private static AbstractWSSecurityUsernameTokenCallback create(final String username, final String password) {

        return new AbstractWSSecurityUsernameTokenCallback() {
            @Override
            public String getUsername() {

                return username;
            }

            @Override
            public String getPassword() {

                return password;
            }

            @Nullable
            @Override
            public String handle(final String username) {

                return null;
            }

            @Override
            public boolean isInboundHeaderOptional() {

                return true;
            }
        };
    }
}
