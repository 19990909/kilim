/* Copyright (c) 2006, Sriram Srinivasan
 *
 * You may distribute this software under the terms of the license 
 * specified in the file "License"
 */

package kilim.http;

import java.io.IOException;

import kilim.nio.NioSelectorScheduler;


/**
 * A very rudimentary HTTP server bound to a specific given port.
 */
public class HttpServer {
    public NioSelectorScheduler nio;


    public HttpServer() {
    }


    public HttpServer(int port, Class<? extends HttpSession> httpSessionClass) throws IOException {
        this.nio=new NioSelectorScheduler();
        this.listen(port, httpSessionClass);
    }


    /**
     * Sets up a listener on the supplied port, and when a fresh connection
     * comes in, it creates a new instance of the httpSessionClass task and
     * exceutes it on the supplied scheduler. It is the httpSession task's
     * responsbility to close the socket.
     * 
     * @param port
     *            . Port to listen for http connections.
     * @param httpSessionClass
     *            class of task to instantiation on incoming connection
     * @param httpSessionScheduler
     *            the scheduler on which to schedule the http session task.
     * @throws IOException
     */
    public void listen(int port, Class<? extends HttpSession> httpSessionClass) throws IOException {
        this.nio.listen(port, httpSessionClass);
    }
}
