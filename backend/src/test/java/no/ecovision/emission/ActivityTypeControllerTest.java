package no.ecovision.emission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import no.ecovision.auth.RegisterRequest;
import no.ecovision.user.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** GET /api/activity-types. See ARCHITECTURE.md section 7. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class ActivityTypeControllerTest {

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
        registry.add("ecovision.jwt.secret", () -> "activity-type-test-signing-key-32-bytes!!");
        registry.add("ecovision.jwt.expiration-minutes", () -> "60");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seedFixtures() {
        jdbcTemplate.update("DELETE FROM activity_log");
        userRepository.deleteAll();
        jdbcTemplate.update("DELETE FROM emission_factor");
        jdbcTemplate.update("DELETE FROM activity_type");

        jdbcTemplate.update("""
                INSERT INTO activity_type (code, category, display_name, unit, sort_order, active)
                VALUES ('car_petrol', 'TRANSPORT', 'Car (petrol)', 'km', 10, true)
                """);
        jdbcTemplate.update("""
                INSERT INTO activity_type (code, category, display_name, unit, sort_order, active)
                VALUES ('electricity', 'ENERGY', 'Electricity', 'kWh', 5, true)
                """);
        jdbcTemplate.update("""
                INSERT INTO activity_type (code, category, display_name, unit, sort_order, active)
                VALUES ('retired_type', 'WASTE', 'Retired', 'kg', 1, false)
                """);
    }

    @Test
    void list_returnsOnlyActiveTypesOrderedBySortOrder() throws Exception {
        String token = registerAndGetToken("types.reader@example.com");

        mockMvc.perform(get("/api/activity-types").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("electricity"))
                .andExpect(jsonPath("$[0].category").value("ENERGY"))
                .andExpect(jsonPath("$[0].unit").value("kWh"))
                .andExpect(jsonPath("$[0].displayName").value("Electricity"))
                .andExpect(jsonPath("$[1].code").value("car_petrol"));
    }

    @Test
    void list_withoutBearerToken_returns401() throws Exception {
        mockMvc.perform(get("/api/activity-types")).andExpect(status().isUnauthorized());
    }

    private String registerAndGetToken(String email) throws Exception {
        var request = new RegisterRequest(email, "password12345", "Types Reader", "GLOBAL");

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
