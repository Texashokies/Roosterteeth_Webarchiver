package com.roosterteeth.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtility {
    private static Logger logger = LoggerFactory.getLogger("log");

    private LogUtility(){
        throw new IllegalStateException("Utility class!");
    }

    public static void logInfo(String method,String message){
        String statement = String.format("%s%s - %s","Method - ",method,message);
        logger.info(statement);
    }

    public static void logInfo(String message){
        logInfo(generateCallingMethodName(),message);
    }

    public static String generateCallingMethodName(){
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }

}
