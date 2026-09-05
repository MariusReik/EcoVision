package no.ecovision.activity;

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

import no.ecovision.auth.RegisterRequest;
import no.ecovision.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Phase 3: POST, GET and DELETE /api/activities. Fixtures are test-only, not seeded
 * production data - V2__seed_reference_data.sql is deliberately still empty pending
 * cited sources (CLAUDE.md: never invent an emission factor).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class ActivityControllerTest {

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
        registry.add("ecovision.jwt.secret", () -> "activity-test-signing-key-at-least-32-bytes!!");
        registry.add("ecovision.jwt.expiration-minutes", () -> "60");
    }

    private static final String ELECTRICITY = "electricity";

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
                INSERT INTO activity_type (code, category, display_name, unit)
                VALUES (?, 'ENERGY', 'Electricity', 'kWh')
                """, ELECTRICITY);

        jdbcTemplate.update("""
                INSERT INTO emission_factor
                    (activity_type_code, region, accounting_basis, factor_kg_co2e,
                     source, source_year, valid_from, valid_to)
                VALUES (?, 'GLOBAL', 'LOCATION', '0.445',
                        'TEST FIXTURE - global average grid factor', 2025, '2025-01-01', NULL)
                """, ELECTRICITY);
    }

    @Test
    void create_validActivity_computesEmissionsServerSideAndStoresTheFactorUsed() throws Exception {
        String token = registerAndGetToken("activity.owner@example.com");

        var request = new CreateActivityRequest(ELECTRICITY, new BigDecimal("10"), LocalDate.of(2025, 6, 1), "commute");

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.activityTypeCode").value(ELECTRICITY))
                .andExpect(jsonPath("$.quantity").value(10))
                // 10 * 0.445
                .andExpect(jsonPath("$.emissionsKg").value(4.45))
                .andExpect(jsonPath("$.note").value("commute"));

        var owner = userRepository.findByEmailIgnoreCase("activity.owner@example.com").orElseThrow();
        Object storedUserId = jdbcTemplate.queryForObject(
                "SELECT user_id FROM activity_log WHERE activity_type_code = ?", Object.class, ELECTRICITY);
        assertThat(storedUserId.toString()).isEqualTo(owner.getId().toString());
    }

    @Test
    void create_clientSuppliedEmissionsIsIgnored_becauseTheRequestDtoHasNoSuchField() throws Exception {
        // POST /api/activities must never accept an emissions value from the client
        // (CLAUDE.md). CreateActivityRequest simply has no emissionsKg field to bind to,
        // so this is enforced by the shape of the DTO rather than by runtime logic -
        // this test documents that guarantee.
        String token = registerAndGetToken("no.client.emissions@example.com");

        String rawRequest = """
                {"activityTypeCode": "%s", "quantity": 10, "occurredOn": "2025-06-01", "emissionsKg": 999999}
                """.formatted(ELECTRICITY);

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emissionsKg").value(4.45));
    }

    @Test
    void create_unknownActivityTypeCode_returns404ProblemJson() throws Exception {
        String token = registerAndGetToken("unknown.type@example.com");

        var request = new CreateActivityRequest("teleportation", BigDecimal.TEN, LocalDate.of(2025, 6, 1), null);

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void create_noFactorCoversTheDate_returns422ProblemJson() throws Exception {
        String token = registerAndGetToken("no.factor@example.com");

        var request = new CreateActivityRequest(ELECTRICITY, BigDecimal.TEN, LocalDate.of(2019, 1, 1), null);

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void create_futureOccurredOn_returns400ProblemJson() throws Exception {
        String token = registerAndGetToken("future.date@example.com");

        var request = new CreateActivityRequest(ELECTRICITY, BigDecimal.TEN, LocalDate.now().plusDays(1), null);

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void create_withoutBearerToken_returns401() throws Exception {
        var request = new CreateActivityRequest(ELECTRICITY, BigDecimal.TEN, LocalDate.of(2025, 6, 1), null);

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_ownActivity_returns204AndRemovesTheRow() throws Exception {
        String token = registerAndGetToken("delete.owner@example.com");
        String id = createActivity(token);

        mockMvc.perform(delete("/api/activities/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        Integer remaining = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM activity_log WHERE id = ?::uuid", Integer.class, id);
        assertThat(remaining).isZero();
    }

    @Test
    void delete_activityOwnedByAnotherUser_returns404ProblemJsonAndLeavesTheRow() throws Exception {
        String ownerToken = registerAndGetToken("real.owner@example.com");
        String otherToken = registerAndGetToken("nosy.stranger@example.com");
        String id = createActivity(ownerToken);

        mockMvc.perform(delete("/api/activities/" + id)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"));

        Integer remaining = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM activity_log WHERE id = ?::uuid", Integer.class, id);
        assertThat(remaining).isOne();
    }

    @Test
    void delete_unknownId_returns404ProblemJson() throws Exception {
        String token = registerAndGetToken("delete.unknown@example.com");

        mockMvc.perform(delete("/api/activities/" + java.util.UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void delete_withoutBearerToken_returns401() throws Exception {
        mockMvc.perform(delete("/api/activities/" + java.util.UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void list_returnsOnlyTheCallersActivitiesNewestFirst() throws Exception {
        String ownerToken = registerAndGetToken("list.owner@example.com");
        String otherToken = registerAndGetToken("list.stranger@example.com");
        createActivity(ownerToken, LocalDate.of(2025, 6, 1));
        createActivity(ownerToken, LocalDate.of(2025, 6, 3));
        createActivity(otherToken, LocalDate.of(2025, 6, 2));

        mockMvc.perform(get("/api/activities")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].occurredOn").value("2025-06-03"))
                .andExpect(jsonPath("$.items[1].occurredOn").value("2025-06-01"))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());
    }

    @Test
    void list_respectsFromAndToFilters() throws Exception {
        String token = registerAndGetToken("list.filtered@example.com");
        createActivity(token, LocalDate.of(2025, 1, 1));
        createActivity(token, LocalDate.of(2025, 6, 1));
        createActivity(token, LocalDate.of(2025, 12, 1));

        mockMvc.perform(get("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .param("from", "2025-02-01")
                        .param("to", "2025-11-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].occurredOn").value("2025-06-01"));
    }

    @Test
    void list_paginatesWithACursorAndCoversTheFullSetExactlyOnce() throws Exception {
        String token = registerAndGetToken("list.paged@example.com");
        createActivity(token, LocalDate.of(2025, 1, 1));
        createActivity(token, LocalDate.of(2025, 2, 1));
        createActivity(token, LocalDate.of(2025, 3, 1));

        String firstPageBody = mockMvc.perform(get("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.nextCursor").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String cursor = objectMapper.readTree(firstPageBody).get("nextCursor").asText();

        mockMvc.perform(get("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .param("limit", "2")
                        .param("cursor", cursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].occurredOn").value("2025-01-01"))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());
    }

    @Test
    void list_malformedCursor_returns400ProblemJson() throws Exception {
        String token = registerAndGetToken("list.badcursor@example.com");

        mockMvc.perform(get("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .param("cursor", "not-a-valid-cursor"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void list_withoutBearerToken_returns401() throws Exception {
        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isUnauthorized());
    }

    private String createActivity(String token) throws Exception {
        return createActivity(token, LocalDate.of(2025, 6, 1));
    }

    private String createActivity(String token, LocalDate occurredOn) throws Exception {
        var request = new CreateActivityRequest(ELECTRICITY, new BigDecimal("10"), occurredOn, null);

        String body = mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(body).get("id").asText();
    }

    private String registerAndGetToken(String email) throws Exception {
        var request = new RegisterRequest(email, "password12345", "Activity Owner", "GLOBAL");

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
