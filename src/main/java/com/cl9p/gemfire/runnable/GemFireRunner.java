package com.cl9p.gemfire.runnable;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

public class GemFireRunner implements Runnable {

        private ServerSocket server = null;
        private Thread thread = null;
        private volatile boolean stopping = false;
        private String directory = null;
        private final Vector handlers;
        private static Logger logger = Logger.getLogger(GemFireRunner.class);

        public static native void toto();

        public GemFireRunner() {
            super();
            logger.info("instance " + this.hashCode() +
                    " created");
            this.handlers = new Vector();
        }

        protected void finalize() {
            logger.info("instance " + this.hashCode() +
                    " garbage collected");
        }

        public ApplicationContext loadSpringContainer() {
            String springContextFile = "gemfire-context.xml";
            return new ClassPathXmlApplicationContext(springContextFile);
        }


        /**
         * Main method
         *
         * @param args is the list of arguments being passed in
         * @throws Exception will throw when something bad happens
         */

        public static void main(String[] args) throws Exception {
            GemFireRunner app = new GemFireRunner();
            app.loadSpringContainer();
            logger.info("instance " + app.hashCode() +
                    " init " + args.length);
            int port = 1200;
            for (int i = 0; i < args.length; i++) {
                logger.info("arg " + i +
                        " = " + args[i]);


            }
            if (args.length > 0 && args[0].length() > 0)
                port = Integer.parseInt(args[0]);
            if (args.length > 1)
                app.directory = args[1];
            else
                app.directory = "/tmp";

        /* Dump a message */
            logger.info("loading on port " + port);
            Runtime.getRuntime().addShutdownHook(new ShutdownHook(app));
        /* Set up this simple daemon */
            app.server = new ServerSocket(port);
            app.thread = new Thread(app);
            app.start();
        }

        public void start() {
        /* Dump a message */
            logger.info("starting...");

        /* Start */
            this.thread.start();
        }

        public void stop()
                throws IOException, InterruptedException {
        /* Dump a message */
            logger.info("stopping");

        /* Close the ServerSocket. This will make our thread to terminate */
            this.stopping = true;
            this.server.close();

        /* Wait for the main thread to exit and dump a message */
            this.thread.join(5000);
            logger.info("stopped");
        }

        public void destroy() {
            logger.info("instance " + this.hashCode() +
                    " destroy");
        }

        public void run() {
            int number = 0;

            logger.info("started acceptor loop");
            try {
                while (!this.stopping) {
                    Socket socket = this.server.accept();
                    Handler handler = new Handler(socket, this);
                    handler.setConnectionNumber(number++);
                    handler.setDirectoryName(this.directory);
                    new Thread(handler).start();
                }
            } catch (IOException e) {
            /* Don't dump any error message if we are stopping. A IOException
               is generated when the ServerSocket is closed in stop() */
                if (!this.stopping) e.printStackTrace(System.err);
            }

        /* Terminate all handlers that at this point are still open */
            Enumeration openhandlers = this.handlers.elements();
            while (openhandlers.hasMoreElements()) {
                Handler handler = (Handler) openhandlers.nextElement();
                logger.info("dropping connection " +
                        handler.getConnectionNumber());
                handler.close();
            }

            logger.info("exiting acceptor loop");
        }

        protected void addHandler(Handler handler) {
            synchronized (handler) {
                this.handlers.add(handler);
            }
        }

        protected void removeHandler(Handler handler) {
            synchronized (handler) {
                this.handlers.remove(handler);
            }
        }

        public static class ShutdownHook extends Thread {
            private final GemFireRunner instance;

            public ShutdownHook(GemFireRunner instance) {
                this.instance = instance;
            }

            public void run() {
                logger.info("Shutting down");
                try {
                    instance.stop();
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }

        public static class Handler implements Runnable {

            private final GemFireRunner parent;
            private String directory = null; // Only set before thread is started
            private final Socket socket;
            private int number = 0; // Only set before thread is started

            public Handler(Socket s, GemFireRunner p) {
                super();
                this.socket = s;
                this.parent = p;
            }

            public void run() {
                this.parent.addHandler(this);
                logger.info("connection " + this.number +
                        " opened from " + this.socket.getInetAddress());
                try {
                    InputStream in = this.socket.getInputStream();
                    OutputStream out = this.socket.getOutputStream();
                    handle(in, out);
                    this.socket.close();
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
                logger.info("connection " + this.number +
                        " closed");
                this.parent.removeHandler(this);
            }

            public void close() {
                try {
                    this.socket.close();
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }

            public void setConnectionNumber(int number) {
                this.number = number;
            }

            public int getConnectionNumber() {
                return (this.number);
            }

            public void setDirectoryName(String directory) {
                this.directory = directory;
            }

            public String getDirectoryName() {
                return (this.directory);
            }

            public void createFile(String name)
                    throws IOException {
                OutputStream file = new FileOutputStream(name, true);
                PrintStream out = new PrintStream(file);
                SimpleDateFormat fmt = new SimpleDateFormat();

                out.println(fmt.format(new Date()));
                out.close();
                file.close();
            }

            public void createDir(String name)
                    throws IOException {
                File file = new File(name);
                boolean ok = file.mkdirs();
                if (!ok)
                    throw new IOException("mkdirs for " + name + " failed");
                createFile(name);
            }

            public void handle(InputStream in, OutputStream os) {
                PrintStream out = null;
                out = new PrintStream(os, true);

                while (true) {
                    try {
                    /* If we don't have data in the System InputStream, we want
                       to ask to the user for an option. */
                        if (in.available() == 0) {
                            out.println();
                            out.println("Please select one of the following:");
                            out.println("    1) Shutdown");
                            out.println("    2) Create a file");
                            out.println("    3) Disconnect");
                            out.println("    4) Cause a core of the JVM");
                            out.println("    5) Create a directory");
                            out.print("Your choice: ");
                        }

                    /* Read an option from the client */
                        int x = in.read();

                        switch (x) {
                        /* If the socket was closed, we simply return */
                            case -1:
                                return;

                        /* Attempt to shutdown */
                            case '1':
                                out.println("Attempting a shutdown...");
                                try {
                                    System.exit(0);
                                } catch (IllegalStateException e) {
                                    out.println();
                                    out.println("Can't shutdown now");
                                    e.printStackTrace(out);
                                }
                                break;

                        /* Create a file */
                            case '2':
                                String name = this.getDirectoryName() +
                                        "/sf.icp.gemfire.base.SpringBootLoader." +
                                        this.getConnectionNumber() +
                                        ".tmp";
                                try {
                                    this.createFile(name);
                                    out.println("File '" + name + "' created");
                                } catch (IOException e) {
                                    e.printStackTrace(out);
                                }
                                break;

                        /* Disconnect */
                            case '3':
                                out.println("Disconnecting...");
                                return;

                        /* Crash JVM in a native call: It need an so file ;-) */
                            case '4':
                                System.load(System.getProperty("native.library", "./Native.so"));
                                toto();
                                break;

                        /* Create a directory (PR 30177 with 1.4.x and 1.5.0 */
                            case '5':
                                String name1 = this.getDirectoryName() +
                                        "/a/b/c/d/e" +
                                        "/com.cl9p.jetty.runnable.JettyLoader." +
                                        this.getConnectionNumber() +
                                        ".tmp";
                                try {
                                    this.createDir(name1);
                                    out.println("File '" + name1 + "' created");
                                } catch (IOException e) {
                                    e.printStackTrace(out);
                                }
                                break;


                        /* Discard any carriage return / newline characters */
                            case '\r':
                            case '\n':
                                break;

                        /* We got something that we weren't supposed to get */
                            default:
                                out.println("Unknown option '" + (char) x + "'");
                                break;

                        }

                    /* If we get an IOException we return (disconnect) */
                    } catch (IOException e) {
                        logger.error("IOException in " +
                                "connection " +
                                this.getConnectionNumber());
                        return;
                    }
                }
            }
        }
    }


