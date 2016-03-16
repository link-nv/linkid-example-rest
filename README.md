linkID REST Example
===================

This webapp shows how to integrate linkID using the [linkID angularJS directive](https://github.com/link-nv/linkid-sdk/wiki/angularJS) and as well shows the REST calls implementation of the backend which can also be used for a mobile app integration. Checkout the [linkID iOS example](https://github.com/link-nv/linkid-example-ios).

The [LinkIDResource](https://github.com/link-nv/linkid-example-rest/blob/master/src/main/java/net/link/safeonline/sdk/example/rest/LinkIDResource.java) class shows the startAuthentication and pollAuthentication calls. 

The webapp is configured to use the [linkID production service](https://service.linkid.be) using the example-mobile test application so you can test this out immediately.
A live version of it can be found [here](https://demo.linkid.be/linkid-example-rest).

## startAuthentication

This operation will start a new linkID authentication

**request**

```
https://demo.linkid.be/linkid-example-rest/restv1/linkid/startAuthentication
```

**response**

```
{"qrCodeInfo":{"qrCodeURL":"linkidmauth://MAUTH/3/UYWOP8/eA==","qrContent":"MAUTH/3/UYWOP8/eA==","mobile":false,"qrEncoded":"...","qrImage":"..."},"sessionId":"UYWOP8"}
```

## pollAuthentication

This operation will poll an existing linkID authentication and return the current state of it.

**request**

```
https://demo.linkid.be/linkid-example-rest/restv1/linkid/pollAuthentication?sessionId=FyHx4l
```

**response**

```
{"authnResponse":null,"authenticationState":"STARTED","paymentState":null,"paymentMenuURL":null}
```