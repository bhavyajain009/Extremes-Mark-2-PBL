package com.extremis.servlet;

import com.extremis.dao.DBConnection;
import com.extremis.service.RandomizerScheduler;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.logging.Logger;

@WebListener
public class RandomizerLifecycleListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(RandomizerLifecycleListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Extremis Attendance Application Initializing...");
        DBConnection.initSchema();
        RandomizerScheduler.getInstance().start();
        LOGGER.info("Extremis Random Check-In System and Database Initialized.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Extremis Attendance Application Shutting Down...");
        RandomizerScheduler.getInstance().stop();
        LOGGER.info("Extremis Random Check-In Background Scheduler Stopped.");
    }
}
