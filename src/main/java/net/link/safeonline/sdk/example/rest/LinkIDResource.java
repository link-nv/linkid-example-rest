package net.link.safeonline.sdk.example.rest;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.link.safeonline.sdk.api.auth.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentAmount;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.ws.linkid.LinkIDServiceClient;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollResponse;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthSession;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthenticationState;
import net.link.safeonline.sdk.configuration.LinkIDConfig;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.util.logging.Logger;


@Path("linkid")
public class LinkIDResource {

    private static final Logger logger = Logger.get( LinkIDResource.class );

    // the message linkID client will display when log-in
    private static final String linkIDAuthenticationMessage = "Example REST app";

    // the message linkID client will display when logged in successful
    private static final String linkIDFinishMessage        = "we did it, hurray!";
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

        logger.inf( "Initiating linkID log-in" );

        try {
            LinkIDServiceClient linkIDServiceClient = LinkIDServiceFactory.getLinkIDService( LinkIDConfig.get() );

            LinkIDAuthenticationContext context = new LinkIDAuthenticationContext.Builder( LinkIDConfig.get().name() ).authenticationMessage(
                    linkIDAuthenticationMessage ).finishedMessage( linkIDFinishMessage ).build();

            LinkIDAuthSession linkIDAuthSession = linkIDServiceClient.authStart( context, userAgent );

            return Response.ok( linkIDAuthSession ).build();

        }
        catch (LinkIDAuthException ignored) {
            logger.err( "Something went wrong initiating the authentication session" );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
    }

    @GET
    @Path("pollAuthentication")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pollAuthentication(@QueryParam("sessionId") String sessionId) {

        try {
            LinkIDServiceClient linkIDServiceClient = LinkIDServiceFactory.getLinkIDService( LinkIDConfig.get() );

            LinkIDAuthPollResponse pollResponse = linkIDServiceClient.authPoll( sessionId, null );

            if (pollResponse.getAuthenticationState() == LinkIDAuthenticationState.AUTHENTICATED) {

                LinkIDAuthnResponse linkIDAuthnResponse = pollResponse.getAuthnResponse();

                if (null != linkIDAuthnResponse) {
                    logger.dbg( "userID: %s", linkIDAuthnResponse.getUserId() );
                }

            }

            return Response.ok( pollResponse ).cacheControl( CacheControl.valueOf( "no-store" ) ).build();

        }
        catch (LinkIDAuthPollException ignored) {
            logger.err( "Something went wrong when polling" );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
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

        logger.inf( "Initiating linkID log-in" );

        try {
            LinkIDServiceClient linkIDServiceClient = LinkIDServiceFactory.getLinkIDService( LinkIDConfig.get() );

            // Create a paymentContext
            // let's take NEN EURO for now
            double amount = 100;
            LinkIDCurrency currency = LinkIDCurrency.EUR;

            LinkIDPaymentContext linkIDPaymentContext = new LinkIDPaymentContext.Builder( new LinkIDPaymentAmount( amount, currency ) ).build();

            LinkIDAuthenticationContext context = new LinkIDAuthenticationContext.Builder( LinkIDConfig.get().name() ).finishedMessage(
                    linkIDPaymentFinishMessage ).paymentContext( linkIDPaymentContext ).build();

            LinkIDAuthSession linkIDAuthSession = linkIDServiceClient.authStart( context, userAgent );

            return Response.ok( linkIDAuthSession ).build();

        }
        catch (LinkIDAuthException ignored) {
            logger.err( "Something went wrong initiating the authentication session" );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
    }

    @GET
    @Path("pollPayment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pollPayment(@QueryParam("sessionId") String sessionId) {

        try {
            LinkIDServiceClient linkIDServiceClient = LinkIDServiceFactory.getLinkIDService( LinkIDConfig.get() );

            LinkIDAuthPollResponse pollResponse = linkIDServiceClient.authPoll( sessionId, null );

            if (pollResponse.getAuthenticationState() == LinkIDAuthenticationState.AUTHENTICATED) {

                LinkIDAuthnResponse linkIDAuthnResponse = pollResponse.getAuthnResponse();

                if (null != linkIDAuthnResponse) {
                    logger.dbg( "userID: %s", linkIDAuthnResponse.getUserId() );
                }

            }

            return Response.ok( pollResponse ).cacheControl( CacheControl.valueOf( "no-store" ) ).build();

        }
        catch (LinkIDAuthPollException ignored) {
            logger.err( "Something went wrong when polling" );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }

    }
}
