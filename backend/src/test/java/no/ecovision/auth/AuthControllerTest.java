package no.ecovision.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import no.ecovision.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ecovision")
            .withUsername("ecovision")
            .withPassword("ecovision");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("ecovision.jwt.secret", () -> "integration-test-signing-key-at-least-32-bytes!!");
        registry.add("ecovision.jwt.expiration-minutes", () -> "60");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void cleanUsers() {
        userRepository.deleteAll();
    }

    @Test
    void register_createsUser_andReturnsATokenThatDecodesToThatUser() throws Exception {
        var request = new RegisterRequest("new.user@example.com", "correct horse battery", "New User", "NO");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("new.user@example.com"))
                .andExpect(jsonPath("$.user.displayName").value("New User"))
                .andExpect(jsonPath("$.user.region").value("NO"))
                .andExpect(jsonPath("$.user.id").exists())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).doesNotContain("password");

        var savedUser = userRepository.findByEmailIgnoreCase("new.user@example.com").orElseThrow();
        String token = objectMapper.readTree(body).get("token").asText();
        Jwt decoded = jwtDecoder.decode(token);
        assertThat(decoded.getSubject()).isEqualTo(savedUser.getId().toString());
    }

    @Test
    void register_duplicateEmailCaseInsensitive_returns409ProblemJson() throws Exception {
        registerUser("dup@example.com", "password12345", "First", "GLOBAL");

        var request = new RegisterRequest("DUP@example.com", "password12345", "Second", "GLOBAL");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void register_invalidRegionCode_returns400ProblemJson() throws Exception {
        var request = new RegisterRequest("bad.region@example.com", "password12345", "Someone", "Norway");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void login_correctCredentials_returnsAToken() throws Exception {
        registerUser("login.ok@example.com", "password12345", "Login Ok", "GLOBAL");

        var request = new LoginRequest("login.ok@example.com", "password12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("login.ok@example.com"));
    }

    @Test
    void login_wrongPassword_returns401ProblemJson() throws Exception {
        registerUser("login.bad@example.com", "password12345", "Login Bad", "GLOBAL");

        var request = new LoginRequest("login.bad@example.com", "totally-wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void login_unknownEmail_returns401JustLikeAWrongPassword() throws Exception {
        var request = new LoginRequest("nobody@example.com", "whatever12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void requestWithNoBearerToken_isRejectedForAnyRouteOtherThanAuthAndHealth() throws Exception {
        // No controller is mapped to /api/me yet (that's user-profile work, not phase 2),
        // but the security filter chain must reject it before dispatch ever gets there -
        // "authenticated by default" has to hold for routes that don't exist yet either.
        mockMvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    private void registerUser(String email, String password, String displayName, String region) throws Exception {
        var request = new RegisterRequest(email, password, displayName, region);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
