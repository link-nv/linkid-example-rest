package net.link.safeonline.sdk.example.rest;

import net.link.safeonline.sdk.api.auth.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentAmount;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.ws.linkid.LinkIDServiceClient;
import net.link.safeonline.sdk.api.ws.linkid.auth.*;
import net.link.safeonline.sdk.ws.linkid.LinkIDServiceClientImpl;
import net.link.util.logging.Logger;
import net.link.util.ws.security.username.AbstractWSSecurityUsernameTokenCallback;

import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


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
    private static final String linkIDPaymentFinishMessage = "Payment done, hurray!";

    //
    //
    // linkID Authentication example
    //
    //

    @GET
    @Path("startAuthentication")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startAuthentication(@HeaderParam("user-agent") String userAgent) {

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
    @Path("pollAuthentication")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pollAuthentication(@QueryParam("sessionId") String sessionId) {

        try {
            LinkIDServiceClient linkIDServiceClient = new LinkIDServiceClientImpl(
                    linkIDLocation,
                    null,
                    create(linkIDUsername, linkIDPassword)
            );

            LinkIDAuthPollResponse pollResponse = linkIDServiceClient.authPoll(sessionId, null);

            if (pollResponse.getAuthenticationState() == LinkIDAuthenticationState.AUTHENTICATED) {

                LinkIDAuthnResponse linkIDAuthnResponse = pollResponse.getAuthnResponse();

                if (null != linkIDAuthnResponse) {
                    logger.dbg("userID: %s", linkIDAuthnResponse.getUserId());
                }

            }

            return Response.ok(pollResponse).cacheControl(CacheControl.valueOf("no-store")).build();

        } catch (LinkIDAuthPollException e) {
            logger.err("Something went wrong when polling");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

    //
    //
    // linkID Payment example
    //
    //

    @GET
    @Path("startPayment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startPayment(@HeaderParam("user-agent") String userAgent) {

        logger.inf("Initiating linkID log-in");

        try {
            LinkIDServiceClient linkIDServiceClient = new LinkIDServiceClientImpl(
                    linkIDLocation,
                    null,
                    create(linkIDUsername, linkIDPassword)
            );

            // Create a paymentContext
            // let's take NEN EURO for now
            double amount = 100;
            LinkIDCurrency currency = LinkIDCurrency.EUR;

            LinkIDPaymentContext linkIDPaymentContext = new LinkIDPaymentContext.Builder(new LinkIDPaymentAmount(amount, currency))
                    .build();

            LinkIDAuthenticationContext context = new LinkIDAuthenticationContext.Builder(linkIDAppName)
                    .finishedMessage(linkIDPaymentFinishMessage)
                    .paymentContext( linkIDPaymentContext )
                    .build();

            LinkIDAuthSession linkIDAuthSession = linkIDServiceClient.authStart(context, userAgent);

            return Response.ok(linkIDAuthSession).build();

        } catch (LinkIDAuthException e) {
            logger.err("Something went wrong initiating the authentication session");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("pollPayment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pollPayment(@QueryParam("sessionId") String sessionId) {

        try {
            LinkIDServiceClient linkIDServiceClient = new LinkIDServiceClientImpl(
                    linkIDLocation,
                    null,
                    create(linkIDUsername, linkIDPassword)
            );

            LinkIDAuthPollResponse pollResponse = linkIDServiceClient.authPoll(sessionId, null);

            if (pollResponse.getAuthenticationState() == LinkIDAuthenticationState.AUTHENTICATED) {

                LinkIDAuthnResponse linkIDAuthnResponse = pollResponse.getAuthnResponse();

                if (null != linkIDAuthnResponse) {
                    logger.dbg("userID: %s", linkIDAuthnResponse.getUserId());
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
