package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.util.Set;

import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class FeedModelTest {

    @Test
    public void canRetrieveSubjects() {
        FeedModel model = createTestFeedModel();

        Set<String> subjects = model.subjects();
        assertEquals(1, subjects.size());
        assertEquals(TestFeedFactory.TEST_SUBJECT, subjects.iterator().next());
    }

    @Test
    public void canDetermineLatestQuestionnaire() {
        FeedModel model = createTestFeedModel();

        QuestionnaireEntry entry = model.latestQuestionnaireForSubject(TestFeedFactory.TEST_SUBJECT);
        assertNotNull(entry);
        assertEquals(LocalDate.of(2021, 8, 1), entry.authoredDateTime());

        assertNull(model.latestQuestionnaireForSubject("Does not exist"));
    }

    @NotNull
    private static FeedModel createTestFeedModel() {
        return new FeedModel(TestFeedFactory.createTestFeed());
    }
}