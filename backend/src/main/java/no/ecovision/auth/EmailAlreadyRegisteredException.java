package no.ecovision.auth;

/** Thrown on registration when the email (case-insensitively) already has an account. */
public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException(String email) {
        super("An account already exists for email '%s'".formatted(email));
    }
}
