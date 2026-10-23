package com.extremis.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RandomizerScheduler {

    private static final Logger LOGGER = Logger.getLogger(RandomizerScheduler.class.getName());
    private static final RandomizerScheduler INSTANCE = new RandomizerScheduler();

    private final RandomCheckInService randomCheckInService = new RandomCheckInService();
    private ScheduledExecutorService executorService;
    private boolean running = false;

    private RandomizerScheduler() {}

    public static RandomizerScheduler getInstance() {
        return INSTANCE;
    }

    public synchronized void start() {
        if (running) {
            return;
        }

        executorService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Extremis-RandomizerScheduler");
            t.setDaemon(true);
            return t;
        });

        // Run tick every 10 seconds with 2-second initial delay
        executorService.scheduleWithFixedDelay(this::tick, 2, 10, TimeUnit.SECONDS);
        running = true;
        LOGGER.info("RandomizerScheduler background thread started successfully.");
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }

        try {
            if (executorService != null) {
                executorService.shutdownNow();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error shutting down RandomizerScheduler", e);
        } finally {
            running = false;
            LOGGER.info("RandomizerScheduler stopped.");
        }
    }

    public void tick() {
        try {
            // 1. Activate any random location checks whose scheduled start time has arrived
            randomCheckInService.activateDueLocationChecks();

            // 2. Process expired active location checks (records failure, does NOT mark absent per Rule 11)
            randomCheckInService.processExpiredLocationChecks();

            // 3. Backward compatibility for legacy challenges
            randomCheckInService.scheduleDailyChallengesIfMissing(randomCheckInService.getEffectiveDate());
            randomCheckInService.activateDueChallenges();
            randomCheckInService.processExpiredChallenges();
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "Unexpected error in RandomizerScheduler tick", t);
        }
    }

    public boolean isRunning() {
        return running;
    }
}
