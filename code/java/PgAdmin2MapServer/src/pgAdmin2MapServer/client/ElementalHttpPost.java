/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package pgAdmin2MapServer.client;

import java.net.Socket;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import pgAdmin2MapServer.Pg2MS;
import pgAdmin2MapServer.Pg2MS;

/**
 * Elemental example for executing multiple POST requests sequentially.
 */
public class ElementalHttpPost {

    public static HttpResponse post(String postUrl, String postParams) throws Exception {
        HttpParams params = new SyncBasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUserAgent(params, "Test/1.1");
        HttpProtocolParams.setUseExpectContinue(params, true);

        HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpRequestInterceptor[]{
                    // Required protocol interceptors
                    new RequestContent(),
                    new RequestTargetHost(),
                    // Recommended protocol interceptors
                    new RequestConnControl(),
                    new RequestUserAgent(),
                    new RequestExpectContinue()});

        HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

        HttpContext context = new BasicHttpContext(null);

        HttpHost host = new HttpHost("localhost", Pg2MS.serverPort);

        DefaultHttpClientConnection conn = new DefaultHttpClientConnection();
        ConnectionReuseStrategy connStrategy = new NoConnectionReuseStrategy();

        context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, host);

        try {

            HttpEntity requestBody = new StringEntity(postParams, "UTF-8");

            if (!conn.isOpen()) {
                Socket socket = new Socket(host.getHostName(), host.getPort());
                conn.bind(socket, params);
            }
            BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST", postUrl);
            request.setEntity(requestBody);
            Pg2MS.log(">> Request URI: " + request.getRequestLine().getUri());

            request.setParams(params);
            httpexecutor.preProcess(request, httpproc, context);
            HttpResponse response = httpexecutor.execute(request, conn, context);
            response.setParams(params);
            httpexecutor.postProcess(response, httpproc, context);

//            Pg2MS.log("<< Response: " + response.getStatusLine());
//            Pg2MS.log(EntityUtils.toString(response.getEntity()));
//            Pg2MS.log("==============");
            return response;
        } finally {
            conn.close();
        }
    }

    public static void main(String[] args) throws Exception {
        post("/updatePgParams", "ceci est un test");
    }
}
