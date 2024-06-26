package com.roosterteeth.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for logging utilities.
 */
public class LogUtility {
    private static final Logger logger = LoggerFactory.getLogger("log");

    private LogUtility(){
        throw new IllegalStateException("Utility class!");
    }

    /**
     * Logs with logger an info statement
     * @param method The method calling the logger
     * @param message The message to log
     */
    public static void logInfo(String method,String message){
        String statement = String.format("%s%s - %s","Method - ",method,message);
        logger.info(statement);
    }

    /**
     * Logs with logger an info statement
     * @param message The message to log
     */
    public static void logInfo(String message){
        logInfo(generateCallingMethodName(),message);
    }

    /**
     * Gets the method that called the log utility
     * @return The calling method's name
     */
    public static String generateCallingMethodName(){
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }

    /**
     * Logs with logger an error message
     * @param message The error message
     */
    public static void logError(String message) {
        logError(generateCallingMethodName(),message);
    }

    /**
     * Logs with logger an error message
     * @param method The method with the error
     * @param message The message to log
     */
    public static void logError(String method,String message){
        String statement = String.format("%s%s - %s","Method - ",method,message);
        logger.error(statement);
    }
}
