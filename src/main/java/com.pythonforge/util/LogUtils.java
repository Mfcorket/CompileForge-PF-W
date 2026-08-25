package com.pythonforge.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class LogUtils {

    private LogUtils() {
    }

    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    public static void info(Logger logger, String message) {
        logger.log(Level.INFO, message);
    }

    public static void warning(Logger logger, String message) {
        logger.log(Level.WARNING, message);
    }

    public static void error(Logger logger, String message) {
        logger.log(Level.SEVERE, message);
    }
}