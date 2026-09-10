package no.ecovision.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import no.ecovision.auth.RegisterRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** GET and PATCH /api/me. See ARCHITECTURE.md section 7. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class UserControllerTest {

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
        registry.add("ecovision.jwt.secret", () -> "user-controller-test-signing-key-32-bytes!!");
        registry.add("ecovision.jwt.expiration-minutes", () -> "60");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUsers() {
        userRepository.deleteAll();
    }

    @Test
    void getMe_returnsTheAuthenticatedUsersProfile() throws Exception {
        String token = registerAndGetToken("me.owner@example.com", "GLOBAL");

        mockMvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me.owner@example.com"))
                .andExpect(jsonPath("$.region").value("GLOBAL"))
                .andExpect(jsonPath("$.accountingBasis").value("LOCATION"));
    }

    @Test
    void getMe_withoutBearerToken_returns401() throws Exception {
        mockMvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void patchMe_updatesOnlyTheSuppliedFields() throws Exception {
        String token = registerAndGetToken("me.patch@example.com", "GLOBAL");

        mockMvc.perform(patch("/api/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"region": "NO", "accountingBasis": "MARKET"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.region").value("NO"))
                .andExpect(jsonPath("$.accountingBasis").value("MARKET"))
                .andExpect(jsonPath("$.displayName").value("Me Owner"));
    }

    @Test
    void patchMe_invalidRegionCode_returns400ProblemJson() throws Exception {
        String token = registerAndGetToken("me.badregion@example.com", "GLOBAL");

        mockMvc.perform(patch("/api/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"region": "Norway"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void patchMe_withoutBearerToken_returns401() throws Exception {
        mockMvc.perform(patch("/api/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    private String registerAndGetToken(String email, String region) throws Exception {
        var request = new RegisterRequest(email, "password12345", "Me Owner", region);

        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }
}
