package no.ecovision.auth;

/**
 * Thrown on login when the email is unknown or the password does not match. Deliberately
 * carries no detail distinguishing the two cases, so the API response cannot be used to
 * enumerate registered emails.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
