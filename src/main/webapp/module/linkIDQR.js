'use strict';

/* Directives */


angular.module('linkIDQR', [])

    .directive('linkidQr', ['$resource', '$timeout', '$window', '$sce', function ($resource, $timeout, $window, $sce) {
        return {
            scope: {
                displayDownloadButtons: '=',
                initHref: '@',
                initData: '=',
                pollHref: '@',
                onQr: '&',
                onSuccess: '&',
                onFailed: '&',
                onExpired: '&',
                onPaymentAdd: '&',
                onSession: '&',
                onPayment: '&',
                onPaymentFailed: '&',
                loadText: '@',
                autoLoad: '=',
                continueInBrowserInfo: '@',
                imgUrl: '@'
            },
            priority: 0,
            template: '<div id="linkid-login">'
            + '<div class="loading" data-ng-show="loading"></div>'
            + '<div class="linkid-login-link" data-ng-hide="hideLogo">'
            + '<a id="main_login" class="linkid-login btn btn-large large-text" data-ng-click="startLoading();">'
            + '<img data-ng-src="{{imgUrl}}/linkidqr/linkid-logo-zw.png" alt="signup" class="login">'
            + '</a> '
            + '</div>'
            + '<div data-ng-if="loginStartPressed">'
            + '<img class="linkIDQR" data-ng-show="finished" data-ng-src="{{imgUrl}}/linkidqr/done.png">'
            + '<a href="javascript:;" data-ng-click="initializeVariables();" class="linkIDQRRefresh"><img class="linkIDQR" data-ng-show="refresh" data-ng-src="{{imgUrl}}/linkidqr/refresh.png"></a>'
            + '<img class="linkIDQR failed" data-ng-show="failed" data-ng-src="{{imgUrl}}/linkidqr/failed.png">'
            + '<div class="text-center" data-ng-show="continueInBrowser"><p data-ng-show="continueInBrowserInfo">{{continueInBrowserInfo}}</p><p data-ng-hide="continueInBrowserInfo">Continue your payment in the linkID tab</p></div>'
            + '</div>'
            + '<div data-ng-if="loginStartPressed && !finished && !failed" data-ng-class="{ hideQR : hideQR }" class="linkIDQR linkid-login-link">'
            + '<a data-ng-cloak data-ng-href="{{session.qrCodeInfo.qrCodeURL}}">'
            + '<img data-ng-if="session.qrCodeInfo.qrEncoded" data-ng-src="data:image/png;base64,{{session.qrCodeInfo.qrEncoded}}" class="fading" style="width: 100%; height: 100%" />'
            + '<span data-ng-if="loadText && !session.qrCodeInfo.qrEncoded" class="linkidLoadText fading">{{loadText}}</span>'
            + '</a>'
            + '</div>'
            + '</div>'
            + '<div class="dowloadApp" data-ng-if="displayDownloadButtons">'
            + '<a href="https://itunes.apple.com/us/app/linkid-for-mobile/id522371545?mt=8" target="_blank">'
            + '<img data-ng-src="{{imgUrl}}/linkidqr/appstore.png" />'
            + '</a>'
            + '<a href="https://play.google.com/store/apps/details?id=net.link.qr" target="_blank">'
            + '<img data-ng-src="{{imgUrl}}/linkidqr/playstore.png" />'
            + '</a>'
            + '<a href="https://play.google.com/store/apps/details?id=net.link.qr" target="_blank">'
            + '<img data-ng-src="{{imgUrl}}/linkidqr/windows.png" />'
            + '</a>'
            + '</div>',

            link: function (scope, element, attrs, modelCtrl) {

                scope.currentTimeout = null;
                scope.pollingInterval = 1500;

                scope.initializeVariables = function (forceAutoLoad) {
                    if (scope.currentTimeout) {
                        $timeout.cancel(scope.currentTimeout);
                    }

                    // init scope vars
                    scope.session = {};
                    scope.sessionState = null;
                    scope.status = {};
                    scope.status.polling = false;

                    //design variables
                    scope.refresh = false;
                    scope.loading = false;
                    scope.continueInBrowser = false;
                    scope.hideLogo = false;
                    scope.hideQR = false;
                    scope.finished = false;
                    scope.failed = false;

                    if (forceAutoLoad) {
                        scope.startLoading();
                    }
                };
                // Initialize variables on load
                scope.initializeVariables(false);

                scope.setAuthnEndState = function (failed) {
                    scope.status.polling = false;
                    scope.session = null;

                    scope.loading = false;

                    if (failed) {
                        scope.failed = true;
                        scope.finished = false;
                    } else {
                        scope.failed = false;
                        scope.finished = true;
                    }

                    scope.status.polling = false;
                    scope.refresh = true;
                };

                scope.startLoading = function () {

                    // START LOADING ANIMATION
                    scope.loading = true;
                    scope.loginStartPressed = true;

                    // monitor init-href attribute
                    if (scope.initHref) {

                        var initData = {};
                        if (scope.initData) {
                            initData = scope.initData;
                        }
                        $resource(scope.initHref).get(initData, function (data) {
                            scope.session = data;
                        }, function () {
                            // HIDE THE LOGO AND SHOW THE QR
                            scope.loading = false;
                            scope.hideLogo = true;
                            scope.setAuthnEndState(true);
                        });
                    }
                }
                ;

                if (scope.autoLoad) {
                    scope.startLoading();
                }

                // CREATE BUTTON FOR MOBILE DEVICES
                scope.mobileInitializeQr = function () {
                    scope.mobile = /ipad|iphone|android|windows phone/i.test(navigator.userAgent.toLowerCase());
                    if (scope.mobile) {
                        scope.startLoading();
                    }
                };
                scope.mobileInitializeQr();

                //show the QR on mobile devices
                scope.showQR = function () {
                    scope.mobile = false;
                };

                // poll linkid function
                scope.poll = function () {

                    if (scope.status.polling) {

                        var expectedSessionId = scope.session.sessionId;

                        scope.sessionState = $resource(scope.pollHref).get(
                            {
                                sessionId: expectedSessionId
                            },
                            function (data) {
                                scope.handleResult(data, expectedSessionId);
                            }, function () {
                                scope.setAuthnEndState(true);
                            })
                    }
                };

                scope.setPollingTimeOut = function () {
                    if (scope.status.polling) {
                        scope.currentTimeout = $timeout(
                            function () {
                                scope.poll();
                            }, scope.pollingInterval);
                    }
                };

                // monitor session object
                scope.$watch('session.sessionId', function (sessionId, oldId) {
                    if (sessionId) {
                        // trust linkID url
                        scope.session.qrCodeURL = $sce.trustAsResourceUrl(scope.session.qrCodeURL);
                        // start polling
                        scope.status.polling = true;

                        // HIDE THE LOGO AND SHOW THE QR
                        scope.loading = false;
                        scope.hideLogo = true;

                        scope.setPollingTimeOut();

                        if (scope.onQr != undefined) {
                            scope.onQr();
                        }
                    }
                }, true);

                // monitor session state
                scope.handleResult = function (sessionState, expectedSessionId) {

                    // autn callbacks
                    if (sessionState && sessionState.authenticationState) {

                        if (scope.session.sessionId !== expectedSessionId) {
                            return;
                        }

                        if (sessionState.authenticationState === 'AUTHENTICATED') {

                            if (!sessionState.paymentState) {
                                scope.setAuthnEndState(false);
                            }

                            scope.loading = false;
                            scope.refresh = false;
                            scope.hideLogo = true;
                            scope.finished = true;

                            // use timeout for being able to show success on login
                            if ('onSuccess' in attrs) {
                                $timeout(function () {
                                    scope.onSuccess({state: sessionState});
                                }, 500);
                            }
                        }
                        else if (sessionState.authenticationState === 'FAILED') {

                            scope.setAuthnEndState(true);

                            if ('onFailed' in attrs) {
                                scope.onFailed({state: sessionState});
                            }
                        }
                        else if (sessionState.authenticationState === 'EXPIRED') {

                            scope.setAuthnEndState(true);

                            if ('onExpired' in attrs) {
                                scope.onExpired({state: sessionState});
                            }
                        }
                        else if (sessionState.authenticationState === 'RETRIEVED') {

                            // linkID session is retrieved,
                            // the loading animation is hidden,
                            // along with the linkid button and logo,
                            // to make place for the qr code
                            scope.loading = true;
                            scope.refresh = true;
                            scope.hideQR = true;
                            if (scope.onSession() != undefined) {
                                scope.onSession();
                            }

                            scope.setPollingTimeOut();
                        }
                        else if (sessionState.authenticationState === 'PAYMENT_ADD') {

                            scope.loading = true;
                            scope.refresh = true;
                            scope.hideQR = true;
                            if (sessionState.paymentMenuURL) {

                                scope.continueInBrowser = true;
                                $window.open(sessionState.paymentMenuURL, '_blank');
                            }

                            if ('onPaymentAdd' in attrs) {
                                scope.onPaymentAdd();
                            }
                        }
                        else {
                            scope.setPollingTimeOut();
                        }
                    }
                    // payment callbacks
                    if (sessionState && sessionState.paymentState) {
                        if (sessionState.paymentState === 'WAITING_FOR_UPDATE' || sessionState.paymentState === 'PAYED') {
                            scope.status.polling = false;
                            scope.session = {};

                            scope.loading = false;
                            scope.refresh = false;
                            scope.hideLogo = true;
                            scope.finished = true;

                            // use timeout for being able to show success on login
                            if ('onPayment' in attrs) {
                                $timeout(scope.onPayment, 500);
                            }
                        }
                        else if (sessionState.paymentState === 'FAILED') {
                            if ('onPaymentFailed' in attrs) {
                                scope.onPaymentFailed();
                            }
                            else {
                                scope.loading = false;
                                scope.failed = true;
                                scope.status.polling = false;
                                scope.refresh = true;
                            }
                        }
                    }
                };

                scope.$on("$stateChangeSuccess", function () {
                    scope.status.polling = false;
                });

                // cancel polling if route changes (= user navigates away)
                scope.$on("$routeChangeSuccess", function (event, current, previous) {
                    if (current) {
                        scope.status.polling = false;
                    }
                });

                // cancel polling if scope gets destroyed
                scope.$on("$destroy", function () {
                    scope.status.polling = false;
                });
            }
        };
    }])
;
