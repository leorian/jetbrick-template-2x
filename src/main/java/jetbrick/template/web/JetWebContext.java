/**
 * Copyright 2013-2014 Guoqiang Chen, Shanghai, China. All rights reserved.
 *
 *   Author: Guoqiang Chen
 *    Email: subchen@gmail.com
 *   WebURL: https://github.com/subchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrick.template.web;

import java.util.Map;
import java.util.HashMap;
import javax.servlet.*;
import javax.servlet.http.*;
import jetbrick.web.servlet.map.*;

public final class JetWebContext extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public static final String APPLICATION = "application";
    public static final String SESSION = "session";
    public static final String REQUEST = "request";
    public static final String RESPONSE = "response";

    public static final String APPLICATION_SCOPE = "applicationScope";
    public static final String SESSION_SCOPE = "sessionScope";
    public static final String REQUEST_SCOPE = "requestScope";

    public static final String PARAMETER = "parameter";
    public static final String PARAMETER_VALUES = "parameterValues";

    public static final String CONTEXT_PATH = "CONTEXT_PATH";
    public static final String WEBROOT_PATH = "WEBROOT_PATH";
    public static final String BASE_PATH = "BASE_PATH";

    //@formatter:off
    private enum TYPE {
        REQUEST_SCOPE,
        SESSION, SESSION_SCOPE,
        PARAMETER, PARAMETER_VALUES,
        CONTEXT_PATH, WEBROOT_PATH, BASE_PATH,
    }
    //@formatter:on

    private final HttpServletRequest request;
    private final boolean sessionSupport;

    public JetWebContext(HttpServletRequest request, HttpServletResponse response) {
        this(request, response, true);
    }

    public JetWebContext(HttpServletRequest request, HttpServletResponse response, boolean sessionSupport) {
        this.request = request;
        this.sessionSupport = sessionSupport;

        put(REQUEST, request);
        put(REQUEST_SCOPE, TYPE.REQUEST_SCOPE);

        put(RESPONSE, response);

        if (sessionSupport) {
            put(SESSION, TYPE.SESSION);
            put(SESSION_SCOPE, TYPE.SESSION_SCOPE);
        }

        put(PARAMETER, TYPE.PARAMETER);
        put(PARAMETER_VALUES, TYPE.PARAMETER_VALUES);

        put(CONTEXT_PATH, TYPE.CONTEXT_PATH);
        put(WEBROOT_PATH, TYPE.WEBROOT_PATH);
        put(BASE_PATH, TYPE.BASE_PATH);
    }

    @Override
    public Object get(Object key) {
        String name = (String) key;
        if (name == null) return null;

        Object value = super.get(name);
        if (value != null) {
            if (value instanceof TYPE) {
                value = createImplicitWebObject((TYPE) value);
                put(name, value); // resolved
            }
            return value;
        }

        value = request.getAttribute(name);
        if (value != null) {
            return value;
        }

        if (sessionSupport) {
            value = request.getSession().getAttribute(name);
            if (value != null) {
                return value;
            }
        }

        return request.getServletContext().getAttribute(name);
    }

    private Object createImplicitWebObject(TYPE type) {
        switch (type) {
        case REQUEST_SCOPE:
            return new RequestAttributeMap(request);
        case SESSION:
            return request.getSession();
        case SESSION_SCOPE:
            return new SessionAttributeMap(request);
        case PARAMETER:
            return new RequestParameterMap(request);
        case PARAMETER_VALUES:
            return new RequestParameterValuesMap(request);
        case CONTEXT_PATH:
            return request.getContextPath();
        case WEBROOT_PATH:
            return getWebrootPath();
        case BASE_PATH:
            return getWebrootPath().concat("/");
        default:
            return null;
        }
    }

    private String getWebrootPath() {
        StringBuilder sb = new StringBuilder();
        String schema = request.getScheme();
        int port = request.getServerPort();
        sb.append(schema);
        sb.append("://");
        sb.append(request.getServerName());
        if (!(port == 80 && "http".equals(schema)) && !(port == 443 && "https".equals(schema))) {
            sb.append(':').append(request.getServerPort());
        }
        sb.append(request.getContextPath());
        return sb.toString();
    }
}
