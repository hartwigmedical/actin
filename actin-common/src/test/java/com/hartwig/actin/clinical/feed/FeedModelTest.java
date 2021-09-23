package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.util.Set;

import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;

import org.junit.Test;

public class FeedModelTest {

    @Test
    public void canRetrieveSubjects() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        Set<String> subjects = model.subjects();
        assertEquals(1, subjects.size());
        assertEquals(TestFeedFactory.TEST_SUBJECT, subjects.iterator().next());
    }

    @Test
    public void canDetermineLatestQuestionnaire() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        QuestionnaireEntry entry = model.latestQuestionnaireForSubject(TestFeedFactory.TEST_SUBJECT);
        assertNotNull(entry);
        assertEquals(LocalDate.of(2021, 8, 1), entry.authoredDateTime());

        assertNull(model.latestQuestionnaireForSubject("Does not exist"));
    }
}