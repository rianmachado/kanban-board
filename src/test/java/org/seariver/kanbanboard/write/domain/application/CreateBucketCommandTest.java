package org.seariver.kanbanboard.write.domain.application;

import helper.TestHelper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
public class CreateBucketCommandTest extends TestHelper {

    @Test
    void GIVEN_ValidData_MUST_RetrieveSameData() {

        // given
        var uuid = UUID.randomUUID();
        var position = faker.number().randomDouble(3, 1, 10);
        var name = faker.pokemon().name();

        // when
        var command = new CreateBucketCommand(uuid, position, name);

        // then
        assertThat(command.uuid()).isEqualTo(uuid);
        assertThat(command.position()).isEqualTo(position);
        assertThat(command.name()).isEqualTo(name);
    }
}
