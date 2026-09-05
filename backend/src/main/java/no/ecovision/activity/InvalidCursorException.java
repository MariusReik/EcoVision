package no.ecovision.activity;

/**
 * Thrown when a client-supplied cursor does not decode to a valid keyset position.
 * The cursor is an opaque token from the client's point of view; this is the only
 * validation it gets.
 */
public class InvalidCursorException extends RuntimeException {

    public InvalidCursorException(String cursor) {
        super("Cursor '%s' is not valid".formatted(cursor));
    }
}
