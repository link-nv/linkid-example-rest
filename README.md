linkID REST Example
===================

Example linkID REST webapp for using linkID from mobile clients

The webapp has 2 operations, which a mobile app can call to start a linkID authentication and poll for the current state of it. 

## start

This operation will start a new linkID sesssion, and return the session ID to be used in later polling, the QR code's URL and the base64 encoded QR code image.

**request**

`http://192.168.0.199:9090/restv1/linkid/start?language=nl`

**response**

`
{"sessionId":"FyHx4l",
 "qrCodeURL":"linkidmauthurldemo://MAUTH/3/FyHx4l/eA==",
 "authenticationContext":"<base64-encoded>",
 "qrCodeImageEncoded":"<base64-encoded>"}
`

EVtziz


## poll

This operation will poll an existing linkID session and return the current state of it.

**request**

`http://192.168.0.199:9090/restv1/linkid/poll?sessionId=FyHx4l&language=nl`

**response**

`
{"authenticationState":"EXPIRED","paymentState":null,"paymentMenuURL":null}
`
