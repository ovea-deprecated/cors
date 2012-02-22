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

        var urlMatcher = /^(((([^:\/#\?]+:)?(?:\/\/((?:(([^:@\/#\?]+)(?:\:([^:@\/#\?]+))?)@)?(([^:\/#\?]+)(?:\:([0-9]+))?))?)?)?((\/?(?:[^\/\?#]+\/+)*)([^\?#]*)))?(\?[^#]+)?)(#.*)?/,
            markMatcher = /.*(~(\d+)~(\d+)~)/gm,
            expireMatcher = /;\s*Expires\s*=\s*.+/g,
            oldxhr = $.ajaxSettings.xhr,
            sessionCookie = sc in window ? window[sc] : "jsessionid",
            cookies = cks in window ? window[cks] : [],
            ReadyState = {UNSENT:0, OPENED:1, LOADING:3, DONE:4};

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

        function parseUrl(url) {
            if ($.type(url) === "object") {
                return url;
            }
            var matches = urlMatcher.exec(url);
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

        function parseCookies(header) {
            var cookies = [], i = 0, start = 0, end;
            do {
                end = header.indexOf(',', start);
                cookies[i] = (cookies[i] || '') + header.substring(start, end == -1 ? header.length : end);
                start = end + 1;
                if (!expireMatcher.test(cookies[i]) || cookies[i].indexOf(',') != -1) {
                    i++;
                }
            } while (end > 0);
            return cookies;
        }

        var domain = parseUrl(document.location.href).domain,
            XDomainRequestAdapter = function () {
                var self = this,
                    _xdr = new XDomainRequest(),
                    _mime,
                    _setState = function (state) {
                        self.readyState = state;
                        if (typeof self.onreadystatechange === 'function') {
                            self.onreadystatechange.call(self);
                        }
                    },
                    _done = function (state, code) {
                        self.status = code;
                        if (!self.responseType) {
                            _mime = _mime || _xdr.contentType;
                            self.responseType = _mime && _mime.substr(0, 16).toLowerCase() === 'application/json' ? 'json' : 'text';
                        }
                        self.response = self.responseType === 'json' && self.responseText ? JSON.parse(self.responseText) : self.responseText;
                        _setState(state);
                    };
                _xdr.onprogress = function () {
                    _setState(ReadyState.LOADING);
                };
                _xdr.ontimeout = function () {
                    _done(ReadyState.DONE, 408);
                };
                _xdr.onerror = function () {
                    _done(ReadyState.DONE, 500);
                };
                _xdr.onload = function () {
                    // check if we are using a filter which modify the response
                    var m, code = 200, rl = _xdr.responseText.length;
                    if (rl >= 5 && (m = markMatcher.exec(_xdr.responseText.substr(rl - 20)))) {
                        var ml = m[1].length,
                            hl = parseInt(m[3]),
                            cookies = parseCookies(_xdr.responseText.substr(rl - hl - ml, hl));
                        code = parseInt(m[2]);
                        self.responseText = _xdr.responseText.substring(ml + hl);
                        for (var i = 0; i < cookies.length; i++) {
                            document.cookie = cookies[i];
                        }
                    } else {
                        self.responseText = _xdr.responseText;
                    }
                    _done(ReadyState.DONE, code);
                };
                this.readyState = ReadyState.UNSENT;
                this.status = 0;
                this.statusText = '';
                this.responseType = '';
                this.timeout = 0;
                this.withCredentials = false;
                this.overrideMimeType = function (mime) {
                    _mime = mime;
                };
                this.abort = function () {
                    _xdr.abort();
                };
                this.setRequestHeader = function () {
                };
                this.open = function (method, url) {
                    if (this.timeout) {
                        _xdr.timeout = this.timeout;
                    }
                    if (sessionCookie || cookies) {
                        var q = url.indexOf('?');
                        forEachCookie(sessionCookie, function (name, value) {
                            if (q == -1) {
                                url += ';' + name + '=' + value;
                            } else {
                                url = url.substring(0, q) + ';' + name + '=' + value + url.substring(q);
                                q = url.indexOf('?');
                            }
                        });
                        forEachCookie(cookies, function (name, value) {
                            url += (q == -1 ? '?' : '&') + name + '=' + value;
                        });
                    }
                    _setState(ReadyState.OPENED);
                };
                this.send = function (data) {
                    return _xdr.send(data);
                };
                this.getAllResponseHeaders = function () {
                    return '';
                };
                this.getResponseHeader = function () {
                    return null;
                }
            };

        $.ajaxSettings.xhr = function () {
            var target = parseUrl(this.url).domain;
            if (target === "" || target === domain) {
                return oldxhr.call($.ajaxSettings)
            } else {
                try {
                    return new XDomainRequestAdapter();
                } catch (e) {
                }
            }
        };

    }
})(jQuery);
