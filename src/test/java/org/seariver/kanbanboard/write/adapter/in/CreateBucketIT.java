package org.seariver.kanbanboard.write.adapter.in;

import helper.IntegrationHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CreateBucketIT extends IntegrationHelper {

    @Test
    void GIVEN_ValidPayload_MUST_ReturnCreated() throws Exception {

        // given
        var uuid = UUID.randomUUID().toString();
        var position = faker.number().randomDouble(5, 1, 10);
        var name = faker.pokemon().name();
        var payload = """
                {
                    "id": "%s",
                    "position": %s,
                    "name": "%s"
                }
                """.formatted(uuid, position, name);

        // when
        mockMvc
                .perform(post("/v1/buckets")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", String.format("/v1/buckets/%s", uuid)));

        // then
        mockMvc
                .perform(get("/v1/buckets"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(3)))
                .andExpect(jsonPath("$[*].id",
                        containsInRelativeOrder(uuid, "6d9db741-ef57-4d5a-ac0f-34f68fb0ab5e", "3731c747-ea27-42e5-a52b-1dfbfa9617db")))
                .andExpect(jsonPath("$[*].position",
                        containsInRelativeOrder(position, 100.15, 200.987)))
                .andExpect(jsonPath("$[*].name", containsInRelativeOrder(name, "FIRST-BUCKET", "SECOND-BUCKET")));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidData")
    void GIVEN_InvalidData_MUST_ReturnBadRequest(String uuid,
                                                 double position,
                                                 String name,
                                                 String[] errorsFields,
                                                 String[] errorsDetails) throws Exception {
        // given
        name = name == null ? null : String.format("\"%s\"", name);
        uuid = uuid == null ? null : String.format("\"%s\"", uuid);
        var payload = """
                {
                    "id": %s,
                    "position": %s,
                    "name": %s
                }
                """.formatted(uuid, position, name);

        // when
        mockMvc
                .perform(post("/v1/buckets")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Invalid field")))
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder(errorsFields)))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder(errorsDetails)));
    }

    @Test
    void GIVEN_MalformedJson_MUST_ReturnBadRequest() throws Exception {

        // given
        var malformedJson = """
                {
                    "id": "786d35e5-83b0-4a1e-96de-5920cbe9180e"
                    position: 10.5,
                    "name": "WHATEVER"
                """;

        // when
        mockMvc
                .perform(post("/v1/buckets")
                        .contentType("application/json")
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Malformed JSON")));
    }

    @Test
    void GIVEN_DuplicatedKey_MUST_ReturnBadRequest() throws Exception {

        var duplicatedUuid = "3731c747-ea27-42e5-a52b-1dfbfa9617db";
        var position = 100.15;

        // given
        var malformedJson = """
                {
                    "id": "%s",
                    "position": %s,
                    "name": "WHATEVER"
                }
                """.formatted(duplicatedUuid, position);

        // when
        mockMvc
                .perform(post("/v1/buckets")
                        .contentType("application/json")
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Invalid field")))
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("code")))
                .andExpect(jsonPath("$.errors[*].detail", containsInAnyOrder("1000")));
    }

    private static Stream<Arguments> provideInvalidData() {

        var validUuid = UUID.randomUUID().toString();
        var validPosition = faker.number().randomDouble(5, 1, 10);
        var validName = "WHATEVER";
        var invalidTextGreatherThan100Chars = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras fringilla elit elementum, ullamcorper turpis consequat.";

        return Stream.of(
                arguments(null, validPosition, validName, args("uuid"), args("must not be null")),
                arguments("", validPosition, validName, args("uuid"), args("invalid UUID format")),
                arguments(validUuid, -1, validName, args("position"), args("must be greater than 0")),
                arguments(validUuid, 0, validName, args("position"), args("must be greater than 0")),
                arguments(validUuid, validPosition, null, args("name"), args("must not be blank")),
                arguments(validUuid, validPosition, "", args("name", "name"), args("must not be blank", "size must be between 1 and 100")),
                arguments(validUuid, validPosition, "      ", args("name"), args("must not be blank")),
                arguments(validUuid, validPosition, invalidTextGreatherThan100Chars, args("name"), args("size must be between 1 and 100"))
        );
    }
}