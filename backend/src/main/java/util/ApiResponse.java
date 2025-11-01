package util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating standardized API responses
 */
public class ApiResponse {

    /**
     * Create error response
     */
    public static Map<String, Object> error(String message, String code) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("code", code);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * Create success response with data
     */
    public static Map<String, Object> success(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * Create success response
     */
    public static Map<String, Object> success(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
