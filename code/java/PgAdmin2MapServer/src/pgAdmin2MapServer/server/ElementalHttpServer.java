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
package pgAdmin2MapServer.server;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.util.EntityUtils;
import pgAdmin2MapServer.Pg2MS;
import pgAdmin2MapServer.mapserver.MapfileWriter;

/**
 * Basic, yet fully functional and spec compliant, HTTP/1.1 file server. <p>
 * Please note the purpose of this application is demonstrate the usage of
 * HttpCore APIs. It is NOT intended to demonstrate the most efficient way of
 * building an HTTP file server.
 *
 *
 */
public class ElementalHttpServer {

    public static void main(String[] args) throws Exception {

//        if (args.length < 1) {
//            System.err.println("Please specify document root directory");
//            System.exit(1);
//        }
        String docRoot = "/tmp/";
        //Thread t = new ElementalHttpServer.RequestListenerThread(8080, args[0]);
        Thread t = new ElementalHttpServer.RequestListenerThread(8080, docRoot);
        t.setDaemon(false);
        t.start();
    }

    static class HttpFileHandler implements HttpRequestHandler {

        private final String docRoot;

        public HttpFileHandler(final String docRoot) {
            super();
            this.docRoot = docRoot;
        }

        public void handle(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws HttpException, IOException {

            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            if (request instanceof HttpEntityEnclosingRequest) {
                try {
                    HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                    String entityContent = EntityUtils.toString(entity);
                    String target = URLDecoder.decode(request.getRequestLine().getUri(), "UTF-8");
                    Pg2MS.log("target: " + target + " content: " + entityContent);

                    if (Pg2MS.UPDATE_PG_PARAMS.equals(target)) {
                        //updateLayers(entityContent);
                        Pg2MS.log("updating pgadmin parameters: " + entityContent);
                        Pg2MS.args = entityContent.replace("[", "").replace("]", "").split(",");
                        //generateMap(request, response, context);

                        String genUrl = "http://localhost:" + Pg2MS.serverPort + Pg2MS.GENERATE_MAP;
                        Pg2MS.log("calling: " + genUrl);

                        Desktop.getDesktop().browse(URI.create(genUrl));
                    } else {
                        throw new Exception("invalid endpoint: " + target);
                    }
                } catch (Exception e) {
                    Pg2MS.log("Server Error1: " + e.toString());
                    // todo: custom http codes according to failures
                    response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                }
            } else if (request instanceof BasicHttpRequest) {
                try {
                    BasicHttpRequest req = (BasicHttpRequest) request;
                    String target = URLDecoder.decode(req.getRequestLine().getUri(), "UTF-8");
                    if (Pg2MS.GENERATE_MAP.equals(target)) {
                        generateMap(request, response, context);
                    }
                } catch (Exception e) {
                    // todo: custom http codes according to failures
                    Pg2MS.log("Server Error2 : " + e.toString());
                    response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                }
            }
        }

        /**
         * Returns the HTML map to the caller TODO: refactor to LayerManager
         *
         * @param request
         * @param response
         * @param context
         * @throws Exception
         */
        public void generateMap(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws Exception {

            String m = MapfileWriter.write();
            Pg2MS.log("mapfile written: " + m);

            //URL u = ElementalHttpServer.class.getResource("/pgAdmin2Mapserver/resources/html/ol.html");
            InputStream is = ElementalHttpServer.class.getResourceAsStream("/resources/ol.html");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                //new OpenLayers.Bounds(1682667.23673968, 2182020.94070385, 1719513.08792259, 2242575.97358883)
                if (MapfileWriter.olBounds != null) {
                    //line = line.replace("\"$$BOUNDS$$\"", MapfileWriter.olBounds);
                }
                sb.append(line).append("\n");
            }
            is.close();
            //URL u = Thread.currentThread().getContextClassLoader().getResource("/pgAdmin2Mapserver/resources/html/ol.html");
            //File f = new File(u.toURI());
            Pg2MS.log("sending file: /pgAdmin2Mapserver/resources/html/ol.html");
            response.setStatusCode(HttpStatus.SC_OK);
            StringEntity body = new StringEntity(sb.toString(), ContentType.create("text/html", Charset.forName("UTF-8")));
            //FileEntity body = new FileEntity(f, ContentType.create("text/html", (Charset) null));
            response.setEntity(body);
        }
    }

    public static class RequestListenerThread extends Thread {

        private final ServerSocket serversocket;
        private final HttpParams params;
        private final HttpService httpService;

        public RequestListenerThread(int port, final String docroot) throws IOException {
            this.serversocket = new ServerSocket(port);
            this.params = new SyncBasicHttpParams();
            this.params
                    .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                    .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                    .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                    .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                    .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

            // Set up the HTTP protocol processor
            HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[]{
                        new ResponseDate(),
                        new ResponseServer(),
                        new ResponseContent(),
                        new ResponseConnControl()
                    });

            // Set up request handlers
            HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
            reqistry.register("*", new ElementalHttpServer.HttpFileHandler(docroot));

            // Set up the HTTP service
            this.httpService = new HttpService(
                    httpproc,
                    new DefaultConnectionReuseStrategy(),
                    new DefaultHttpResponseFactory(),
                    reqistry,
                    this.params);
        }

        @Override
        public void run() {
            Pg2MS.log("Server: listening on port " + this.serversocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = this.serversocket.accept();
                    DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                    Pg2MS.log("Incoming connection from " + socket.getInetAddress());
                    conn.bind(socket, this.params);

                    // Start worker thread
                    Thread t = new ElementalHttpServer.WorkerThread(this.httpService, conn);
                    t.setDaemon(true);
                    t.start();
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    System.err.println("I/O error initialising connection thread: " + e.toString());
                    Pg2MS.log("I/O error initialising connection thread: " + e.toString());
                    break;
                }
            }
        }
    }

    static class WorkerThread extends Thread {

        private final HttpService httpservice;
        private final HttpServerConnection conn;

        public WorkerThread(
                final HttpService httpservice,
                final HttpServerConnection conn) {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
        }

        @Override
        public void run() {
            Pg2MS.log("New connection thread");

            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && this.conn.isOpen()) {
                    this.httpservice.handleRequest(this.conn, context);
                }
            } catch (ConnectionClosedException ex) {
                System.err.println("Client closed connection");
                Pg2MS.log("Client closed connection");
            } catch (IOException ex) {
                System.err.println("I/O error: " + ex.toString());
                Pg2MS.log("I/O error: " + ex.toString());
            } catch (HttpException ex) {
                System.err.println("Unrecoverable HTTP protocol violation: " + ex.toString());
                Pg2MS.log("Unrecoverable HTTP protocol violation: " + ex.toString());
            } finally {
                try {
                    this.conn.shutdown();
                } catch (IOException ignore) {
                }
            }
        }
    }
}
