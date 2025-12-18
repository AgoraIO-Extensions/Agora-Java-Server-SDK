package io.agora.rtc.example.cli;

import io.agora.rtc.example.common.AgoraServiceInitializer;
import io.agora.rtc.example.common.AgoraTaskManager;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.common.TaskLauncher;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CliLauncher {
    private static final ThreadPoolExecutor stressExecutorService = new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            1L,
            TimeUnit.SECONDS,
            new SynchronousQueue<>());

    private static volatile boolean shouldStop = false;
    private static volatile CountDownLatch taskFinishLatch = null;
    private static AgoraTaskManager agoraTaskManager;

    /**
     * Initialize log4j2 logging for consistent format with Spring Boot mode
     * Bridge JUL (AgoraLogger) to SLF4J/Log4j2 so all logs go to the same file
     */
    private static void initializeLogging() {
        // Set system property to specify log4j2 configuration file
        System.setProperty("log4j2.configurationFile", "log4j2-spring.xml");

        try {
            // This will trigger log4j2 initialization
            org.apache.logging.log4j.LogManager.getLogger(CliLauncher.class);

            // Bridge JUL to SLF4J/Log4j2 - all JUL logs will be redirected to Log4j2
            // This ensures AgoraLogger (which uses JUL) outputs to the same file as
            // SampleLogger
            java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
            rootLogger.setLevel(java.util.logging.Level.ALL);

            // Remove all default handlers from JUL
            java.util.logging.Handler[] handlers = rootLogger.getHandlers();
            for (java.util.logging.Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            // Install SLF4J bridge handler - redirects JUL logs to SLF4J/Log4j2
            org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
            org.slf4j.bridge.SLF4JBridgeHandler.install();

            System.out.println("Logging initialized successfully:");
            System.out.println("  ✅ Log4j2: Console + File (logs/linux_server.log)");
            System.out.println("  ✅ JUL → SLF4J Bridge: All JUL logs redirected to Log4j2");
            System.out.println("  📝 All logs from both SampleLogger and AgoraLogger → logs/linux_server.log");
        } catch (Exception e) {
            System.err.println("Warning: Failed to initialize logging: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printUsageAndExit() {
        System.out.println("Usage: ./build.sh cli [jsonFileName|basicClassName]");
        System.out.println("");
        System.out.println("Examples:");
        System.out.println("  ./build.sh cli                    # Run all basic test cases");
        System.out.println("  ./build.sh cli pcm_send.json      # Run specific JSON config");
        System.out.println("  ./build.sh cli SendPcmFileTest    # Run specific basic test");
        System.out.println("  ./build.sh cli ReceiverPcmVadTest # Run specific basic test");
        System.exit(1);
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--") && arg.contains("=")) {
                int idx = arg.indexOf('=');
                String key = arg.substring(2, idx);
                String val = arg.substring(idx + 1);
                map.put(key, val);
            } else if (arg.startsWith("--")) {
                map.put(arg.substring(2), "true");
            } else if (!arg.isEmpty() && !arg.startsWith("--")) {
                // Support direct argument without -- prefix
                map.put("task", arg);
            }
        }
        return map;
    }

    private static void startInputListener() {
        Thread inputThread = new Thread(() -> {
            try {
                java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(System.in));
                System.out.println("Enter 1 to stop the service and exit:");
                String line;
                while ((line = br.readLine()) != null && !shouldStop) {
                    if ("1".equals(line.trim())) {
                        System.out.println("Stop command received. Stopping all tasks...");
                        shouldStop = true;

                        // Stop all running tasks, same logic as ServerController
                        try {
                            if (agoraTaskManager != null) {
                                SampleLogger.log("Cleaning up AgoraTaskManager...");
                                agoraTaskManager.cleanup();
                            }
                        } catch (Exception e) {
                            SampleLogger.error("Error during cleanup: " + e.getMessage());
                            e.printStackTrace();
                        }

                        // Release Agora Service
                        try {
                            SampleLogger.log("Destroying AgoraService...");
                            AgoraServiceInitializer.destroyAgoraService();
                        } catch (Exception e) {
                            SampleLogger.error("Error destroying AgoraService: " + e.getMessage());
                        }

                        if (taskFinishLatch != null) {
                            taskFinishLatch.countDown();
                        }
                        break;
                    } else {
                        System.out.println("Unknown command: " + line + ". Enter 1 to stop.");
                    }
                }
            } catch (Exception e) {
                SampleLogger.error("Error in input listener: " + e.getMessage());
            }
        }, "Cli-Input-Listener");
        inputThread.setDaemon(false);
        inputThread.start();
    }

    private static boolean startBasicTask(String taskName) {
        try {
            shouldStop = false;
            taskFinishLatch = new CountDownLatch(1);

            // Use TaskLauncher to start the basic task
            boolean success = TaskLauncher.startBasicTask(taskName);
            if (!success) {
                return false;
            }

            // Start input listener
            startInputListener();

            // Wait for task to finish or user input
            taskFinishLatch.await();

            return true;
        } catch (Exception e) {
            SampleLogger.error("Error starting basic task: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean startJsonTask(String configFileName) {
        try {
            shouldStop = false;
            taskFinishLatch = new CountDownLatch(1);

            // Use TaskLauncher to start the JSON task
            boolean success = TaskLauncher.startJsonTask(configFileName, agoraTaskManager);
            if (!success) {
                return false;
            }

            // Start input listener
            startInputListener();

            // Wait for task to finish or user input
            taskFinishLatch.await();

            return true;
        } catch (Exception e) {
            SampleLogger.error("Error starting JSON task: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        // Initialize log4j2 for consistent logging format with Spring Boot mode
        initializeLogging();

        Map<String, String> options = parseArgs(args);

        // Support both --task=xxx and direct argument
        String task = options.getOrDefault("task", "");
        if (task.isEmpty()) {
            // Try to get from configFileName for backward compatibility
            task = options.getOrDefault("configFileName", "");
        }

        // If task is empty, run all basic test cases
        if (task.isEmpty()) {
            SampleLogger.log("No task specified, running all basic test cases...");
            try {
                boolean success = TaskLauncher.startAllBasicCase();
                if (success) {
                    SampleLogger.log("All basic test cases completed successfully");
                    System.exit(0);
                } else {
                    SampleLogger.error("Some basic test cases failed");
                    System.exit(1);
                }
            } catch (Exception e) {
                SampleLogger.error("Error running all basic test cases: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            return;
        }

        // Initialize AgoraTaskManager
        agoraTaskManager = new AgoraTaskManager(new AgoraTaskManager.AgoraTaskListener() {
            @Override
            public void onAllTaskFinished() {
                SampleLogger.log("All tasks finished");
                if (taskFinishLatch != null) {
                    taskFinishLatch.countDown();
                }
            }
        });

        try {
            boolean success = false;

            // Check if it's a JSON file (ends with .json) or a basic class name
            if (task.endsWith(".json")) {
                success = startJsonTask(task);
            } else {
                success = startBasicTask(task);
            }

            if (!success) {
                SampleLogger.error("Failed to start task: " + task);
                System.exit(1);
            }
        } catch (Exception e) {
            SampleLogger.error("Error in main: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                // Shutdown stress test thread pool
                stressExecutorService.shutdown();
                if (!stressExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    SampleLogger
                            .error("stressExecutorService did not terminate within 60 seconds, forcing shutdown...");
                    stressExecutorService.shutdownNow();
                    if (!stressExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        SampleLogger.error("stressExecutorService did not terminate even after forced shutdown.");
                    }
                }
            } catch (InterruptedException e) {
                stressExecutorService.shutdownNow();
                Thread.currentThread().interrupt();
            }

            // Only cleanup if not already stopped by user input (pressing '1')
            if (!shouldStop) {
                try {
                    if (agoraTaskManager != null) {
                        SampleLogger.log("Cleaning up AgoraTaskManager (normal exit)...");
                        agoraTaskManager.cleanup();
                        // Note: cleanup() already calls releaseAgoraService(), so we don't need to call
                        // it again
                    }
                } catch (Exception e) {
                    SampleLogger.error("Error during cleanup: " + e.getMessage());
                }
            } else {
                SampleLogger.log("Cleanup already performed by stop command (pressing '1')");
            }

            System.exit(0);
        }
    }
}
