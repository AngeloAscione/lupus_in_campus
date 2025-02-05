package NC12.LupusInCampus.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {
    private static final Logger logger = LoggerFactory.getLogger(LoggerUtil.class);
    private static final String LOG_FILE = "logs/app.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs an info message to console and file
     * @param message The message to log
     */
    public static void logInfo(String message) {
        String formattedMessage = formatMessage("INFO", message);
        logger.info(formattedMessage);
        writeToFile(formattedMessage);
    }

    /**
     * Logs an error message to console and file
     * @param message The error message
     * @param exception The exception (optional)
     */
    public static void logError(String message, Exception exception) {
        String formattedMessage = formatMessage("ERROR", message);
        logger.error(formattedMessage, exception);
        writeToFile(formattedMessage + " - Exception: " + (exception != null ? exception.getMessage() : "No exception"));
    }

    /**
     * Formats log message with timestamp
     */
    private static String formatMessage(String level, String message) {
        return "[" + LocalDateTime.now().format(formatter) + "] [" + level + "] " + message;
    }

    /**
     * Writes logs to a file
     */
    private static void writeToFile(String message) {
        try (FileWriter fileWriter = new FileWriter(LOG_FILE, true)) {
            fileWriter.write(message + "\n");
        } catch (IOException e) {
            logger.error("Failed to write log to file", e);
        }
    }
}
