package com.nickmcdowall.example;

import com.googlecode.yatspec.junit.SequenceDiagramExtension;
import com.googlecode.yatspec.junit.WithParticipants;
import com.googlecode.yatspec.sequence.Participant;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.example.repository.FishRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.googlecode.yatspec.sequence.Participants.ACTOR;
import static com.googlecode.yatspec.sequence.Participants.PARTICIPANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@ImportAutoConfiguration({FeignAutoConfiguration.class})
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = FishApp.class)
@ActiveProfiles("test")
@Import({
        TestConfig.class
})
@ExtendWith(SequenceDiagramExtension.class)
public class FishAppTest implements WithParticipants {

    @Autowired
    private FishRepository fishRepository;

    @Autowired
    private FishClient fishClient;

    @Autowired
    private TestState testState;

    @Test
    void saveAndFind() {
        fishClient.post(new NewFishRequest("nick"));

        assertThat(fishRepository.countFishByName("nick")).isEqualTo(1);
    }

    @Override
    public List<Participant> participants() {
        return List.of(
                ACTOR.create("User"),
                PARTICIPANT.create("FishApp")
        );
    }
}