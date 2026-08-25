package no.ecovision.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

import no.ecovision.auth.EmailAlreadyRegisteredException;
import no.ecovision.auth.InvalidCredentialsException;

/**
 * Translates domain exceptions to RFC 9457 application/problem+json (CLAUDE.md: no bare
 * 500s with stack traces). Framework-level errors (malformed JSON, bean validation, 404,
 * 405) already come back as problem+json via spring.mvc.problemdetails.enabled in
 * application.yml; this class only needs to cover exceptions specific to this codebase,
 * plus a catch-all that guarantees nothing unhandled ever leaks a stack trace or message.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ProblemDetail handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Email already registered");
        problem.setType(URI.create("https://ecovision.no/problems/email-already-registered"));
        return problem;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Invalid credentials");
        problem.setType(URI.create("https://ecovision.no/problems/invalid-credentials"));
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal server error");
        return problem;
    }
}
