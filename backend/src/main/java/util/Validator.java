package util;

/**
 * Validation utility class for common validation logic
 */
public class Validator {

    /**
     * Validate ID is positive
     */
    public static void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive number");
        }
    }

    /**
     * Validate rating value (1-5)
     */
    public static void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    /**
     * Validate string is not empty
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    /**
     * Validate price is positive
     */
    public static void validatePrice(Double price, String fieldName) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
    }
}
