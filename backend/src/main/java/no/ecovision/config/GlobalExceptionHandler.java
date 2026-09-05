package no.ecovision.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

import no.ecovision.activity.ActivityNotFoundException;
import no.ecovision.activity.ActivityTypeNotFoundException;
import no.ecovision.activity.InvalidCursorException;
import no.ecovision.auth.EmailAlreadyRegisteredException;
import no.ecovision.auth.InvalidCredentialsException;
import no.ecovision.emission.FactorNotFoundException;

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

    @ExceptionHandler(ActivityTypeNotFoundException.class)
    public ProblemDetail handleActivityTypeNotFound(ActivityTypeNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Activity type not found");
        problem.setType(URI.create("https://ecovision.no/problems/activity-type-not-found"));
        return problem;
    }

    @ExceptionHandler(ActivityNotFoundException.class)
    public ProblemDetail handleActivityNotFound(ActivityNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Activity not found");
        problem.setType(URI.create("https://ecovision.no/problems/activity-not-found"));
        return problem;
    }

    @ExceptionHandler(InvalidCursorException.class)
    public ProblemDetail handleInvalidCursor(InvalidCursorException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid cursor");
        problem.setType(URI.create("https://ecovision.no/problems/invalid-cursor"));
        return problem;
    }

    @ExceptionHandler(FactorNotFoundException.class)
    public ProblemDetail handleFactorNotFound(FactorNotFoundException ex) {
        // 422, not 404/400: the request is well-formed and the activity type is real,
        // but no emission factor covers it. See ARCHITECTURE.md section 5, decision 2 -
        // never substitute a guess, reject the write instead.
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("No applicable emission factor");
        problem.setType(URI.create("https://ecovision.no/problems/factor-not-found"));
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
