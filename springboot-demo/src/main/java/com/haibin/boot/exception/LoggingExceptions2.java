package com.haibin.boot.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class LoggingExceptions2 {

    private static Logger logger = Logger.getLogger("LoggingException2");
    static void logEeception(Exception e) {
        StringWriter trace = new StringWriter();
        e.printStackTrace(new PrintWriter(trace));
        logger.severe(trace.toString());
    }

    public static void main(String[] args) {
        try {
            throw new NullPointerException();
        }catch (NullPointerException e) {
            logEeception(e);
        }

    }

}
