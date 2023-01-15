package ru.nsu.org.main.lab4snake.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

public class MyLogger {
        private static volatile Logger instance;
        private static final String uuid = UUID.randomUUID().toString();

        public static Logger getLogger() {
            if (instance == null) {
                synchronized (MyLogger.class) {
                    if (instance == null) {
                        instance = LoggerFactory.getLogger("APP");
                    }
                }
            }
            MDC.put("userid", uuid);
            return instance;
        }
}