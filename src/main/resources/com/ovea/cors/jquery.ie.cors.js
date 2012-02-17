/*
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * https://gist.github.com/1114981
 *
 * By default, support transferring session cookie with XDomainRequest for IE. The cookie value is by default 'jsessionid'
 *
 * You can change the session cookie value like this, before including this script:
 *
 * window.XDR_SESSION_COOKIE_NAME = 'ID';
 *
 * Or if you want to disable cookie session support:
 *
 * window.XDR_SESSION_COOKIE_NAME = null;
 *
 * If you need to convert other cookies as headers:
 *
 * window.XDR_COOKIE_HEADERS = ['PHP_SESSION'];
 *
 */
(function ($) {

    var ns = '__jquery_xdomain__',
        sc = 'XDR_SESSION_COOKIE_NAME',
        cks = 'XDR_COOKIE_HEADERS';

    if ($.browser.msie && 'XDomainRequest' in window && !(ns in $)) {

        $[ns] = $.support.cors = true;

        function parseUrl(url) {
            if ($.type(url) === "object") {
                return url;
            }
            var matches = /^(((([^:\/#\?]+:)?(?:\/\/((?:(([^:@\/#\?]+)(?:\:([^:@\/#\?]+))?)@)?(([^:\/#\?]+)(?:\:([0-9]+))?))?)?)?((\/?(?:[^\/\?#]+\/+)*)([^\?#]*)))?(\?[^#]+)?)(#.*)?/.exec(url);
            return matches ? {
                href:matches[0] || "",
                hrefNoHash:matches[1] || "",
                hrefNoSearch:matches[2] || "",
                domain:matches[3] || "",
                protocol:matches[4] || "",
                authority:matches[5] || "",
                username:matches[7] || "",
                password:matches[8] || "",
                host:matches[9] || "",
                hostname:matches[10] || "",
                port:matches[11] || "",
                pathname:matches[12] || "",
                directory:matches[13] || "",
                filename:matches[14] || "",
                search:matches[15] || "",
                hash:matches[16] || ""
            } : {};
        }

        function forEachCookie(names, fn) {
            if (typeof names == 'string') {
                names = [names];
            }
            for (var i = 0; i < names.length; i++) {
                var cookie = new RegExp('(?:^|; )' + names[i] + '=([^;]*)', 'i').exec(document.cookie);
                cookie = cookie && cookie[1];
                if (cookie) {
                    fn.call(null, names[i], cookie);
                }
            }
        }

        var oldxhr = $.ajaxSettings.xhr,
            sessionCookie = sc in window ? window[sc] : "jsessionid",
            cookies = cks in window ? window[cks] : [],
            domain = parseUrl(document.location.href).domain,
            ReadyState = {UNSENT:0, OPENED:1, HEADERS_RECEIVED:2, LOADING:3, DONE:4};

        var XDomainRequestAdapter = function () {
            var self = this,
                _xdr = this._xdr = new XDomainRequest(),
                done = function (state, code) {
                    self.readyState = state;
                    self.status = code;
                    if (typeof self.onreadystatechange === 'function') {
                        self.onreadystatechange.call(self);
                    }
                };
            this._requestHeaders = {};
            this._responseHeaders = {};
            _xdr.onprogress = function () {
                self.readyState = ReadyState.LOADING;
            };
            _xdr.ontimeout = function () {
                done(ReadyState.DONE, 408);
            };
            _xdr.onerror = function () {
                done(ReadyState.DONE, 500);
            };
            _xdr.onload = function () {
                // check if we are using a filter which modify the response
                if (_xdr.responseText.length > 3) {

                }
                self.responseHeaders = {};
                done(ReadyState.DONE, 200);
            };
            this.readyState = ReadyState.UNSENT;
        };
        XDomainRequestAdapter.prototype = {
            setRequestHeader:function (name, value) {
                //TODO MATHIEU
            },
            abort:function () {
                this._xdr.abort();
            },
            open:function (method, url, async, uname, pswd) {
                //TODO MATHIEU
                this.readyState = ReadyState.OPENED;
            },
            send:function (data) {
                //TODO MATHIEU
            },
            getAllResponseHeaders:function () {
                //TODO MATHIEU
            },
            getResponseHeader:function (name) {
                //TODO MATHIEU
            }
        };

        $.ajaxSettings.xhr = function () {
            var target = parseUrl(this.url).domain;
            if (target === "" || target === domain) {
                return oldxhr.call($.ajaxSettings)
            } else {
                try {
                    return new XDomainRequestAdapter();


                    xdr.setRequestHeader = function () {

                    };
                    if (!xdr.getAllResponseHeaders) {
                        xdr.getAllResponseHeaders = $.noop;
                    }
                    if (sessionCookie || cookies) {
                        var open = xdr.open;
                        xdr.open = function (method, url) {
                            var args = arguments;
                            forEachCookie(sessionCookie, function (name, value) {
                                var q = args[1].indexOf('?');
                                if (q == -1) {
                                    args[1] += ';' + name + '=' + value;
                                } else {
                                    args[1] = args[1].substring(0, q) + ';' + name + '=' + value + args[1].substring(q);
                                }
                            });
                            forEachCookie(cookies, function (name, value) {
                                args[1] += (args[1].indexOf('?') == -1 ? '?' : '&') + name + '=' + value;
                            });
                            return open.apply(this, args);
                        };
                    }
                    xdr.onprogress = function () {
                    };
                    xdr.ontimeout = function () {
                    };
                    xdr.onload = function () {
                        xdr.readyState = 4;
                        xdr.status = 200;
                        if (typeof xdr.onreadystatechange === 'function') {
                            xdr.onreadystatechange.call(xdr, null, false);
                        }
                    };
                    xdr.onerror = xdr.ontimeout = function () {
                        xdr.readyState = 4;
                        xdr.status = 500;
                        if (typeof xdr.onreadystatechange === 'function') {
                            xdr.onreadystatechange.call(xdr, null, false);
                        }
                    };
                    return xdr;
                } catch (e) {
                }
            }
        };

    }
})(jQuery);
