package no.ecovision.dashboard;

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

import java.math.BigDecimal;
import java.time.LocalDate;

import no.ecovision.activity.CreateActivityRequest;
import no.ecovision.auth.RegisterRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Phase 4: GET /api/dashboard/summary. Fixtures are test-only, not seeded production
 * data - V2__seed_reference_data.sql is deliberately still empty pending cited sources
 * (CLAUDE.md: never invent an emission factor).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class DashboardControllerTest {

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
        registry.add("ecovision.jwt.secret", () -> "dashboard-test-signing-key-at-least-32-bytes!!");
        registry.add("ecovision.jwt.expiration-minutes", () -> "60");
    }

    private static final String ELECTRICITY = "electricity";
    private static final String CAR = "car";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seedFixtures() {
        jdbcTemplate.update("DELETE FROM activity_log");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM emission_factor");
        jdbcTemplate.update("DELETE FROM activity_type");

        jdbcTemplate.update("""
                INSERT INTO activity_type (code, category, display_name, unit) VALUES
                    (?, 'ENERGY', 'Electricity', 'kWh'),
                    (?, 'TRANSPORT', 'Car', 'km')
                """, ELECTRICITY, CAR);

        jdbcTemplate.update("""
                INSERT INTO emission_factor
                    (activity_type_code, region, accounting_basis, factor_kg_co2e,
                     source, source_year, valid_from, valid_to)
                VALUES
                    (?, 'GLOBAL', NULL, '0.5', 'TEST FIXTURE - grid factor', 2025, '2025-01-01', NULL),
                    (?, 'GLOBAL', NULL, '0.2', 'TEST FIXTURE - car factor', 2025, '2025-01-01', NULL)
                """, ELECTRICITY, CAR);
    }

    @Test
    void summary_aggregatesOnlyTheCallersActivities() throws Exception {
        String token = registerAndGetToken("dash.owner@example.com");
        String strangerToken = registerAndGetToken("dash.stranger@example.com");

        // ENERGY: 10 * 0.5 = 5.0 and 6 * 0.5 = 3.0, both on 2025-06-01 -> 8.0 that day
        logActivity(token, ELECTRICITY, "10", LocalDate.of(2025, 6, 1));
        logActivity(token, ELECTRICITY, "6", LocalDate.of(2025, 6, 1));
        // TRANSPORT: 100 * 0.2 = 20.0 on 2025-06-02
        logActivity(token, CAR, "100", LocalDate.of(2025, 6, 2));
        // Someone else's data must not leak in.
        logActivity(strangerToken, CAR, "500", LocalDate.of(2025, 6, 2));

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalKg").value(28.0))
                .andExpect(jsonPath("$.byCategory.length()").value(2))
                // ORDER BY t.category, stored as a string enum -> alphabetical.
                .andExpect(jsonPath("$.byCategory[0].category").value("ENERGY"))
                .andExpect(jsonPath("$.byCategory[0].totalKg").value(8.0))
                .andExpect(jsonPath("$.byCategory[1].category").value("TRANSPORT"))
                .andExpect(jsonPath("$.byCategory[1].totalKg").value(20.0))
                .andExpect(jsonPath("$.dailySeries.length()").value(2))
                .andExpect(jsonPath("$.dailySeries[0].date").value("2025-06-01"))
                .andExpect(jsonPath("$.dailySeries[0].totalKg").value(8.0))
                .andExpect(jsonPath("$.dailySeries[1].date").value("2025-06-02"))
                .andExpect(jsonPath("$.dailySeries[1].totalKg").value(20.0));
    }

    @Test
    void summary_respectsFromAndToWindow() throws Exception {
        String token = registerAndGetToken("dash.windowed@example.com");
        logActivity(token, ELECTRICITY, "10", LocalDate.of(2025, 1, 15));
        logActivity(token, ELECTRICITY, "10", LocalDate.of(2025, 6, 15));
        logActivity(token, ELECTRICITY, "10", LocalDate.of(2025, 12, 15));

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + token)
                        .param("from", "2025-04-01")
                        .param("to", "2025-09-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalKg").value(5.0))
                .andExpect(jsonPath("$.dailySeries.length()").value(1))
                .andExpect(jsonPath("$.dailySeries[0].date").value("2025-06-15"));
    }

    @Test
    void summary_noActivities_returnsZeroTotalAndEmptyBreakdowns() throws Exception {
        String token = registerAndGetToken("dash.empty@example.com");

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalKg").value(0))
                .andExpect(jsonPath("$.byCategory.length()").value(0))
                .andExpect(jsonPath("$.dailySeries.length()").value(0));
    }

    @Test
    void summary_withoutBearerToken_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }

    private void logActivity(String token, String typeCode, String quantity, LocalDate occurredOn) throws Exception {
        var request = new CreateActivityRequest(typeCode, new BigDecimal(quantity), occurredOn, null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private String registerAndGetToken(String email) throws Exception {
        var request = new RegisterRequest(email, "password12345", "Dash Owner", "GLOBAL");
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
