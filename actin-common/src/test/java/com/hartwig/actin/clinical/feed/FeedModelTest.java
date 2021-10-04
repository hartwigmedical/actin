package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

import com.google.common.io.Resources;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;

import org.junit.Test;

public class FeedModelTest {

    private static final String CLINICAL_FEED_DIRECTORY = Resources.getResource("clinical/feed").getPath();

    @Test
    public void canCreateFromFeedDirectory() throws IOException {
        assertNotNull(FeedModel.fromFeedDirectory(CLINICAL_FEED_DIRECTORY));
    }

    @Test
    public void canRetrieveSubjects() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        Set<String> subjects = model.subjects();
        assertEquals(1, subjects.size());
        assertEquals(TestFeedFactory.TEST_SUBJECT, subjects.iterator().next());
    }

    @Test
    public void canRetrievePatientEntry() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();
        assertNotNull(model.patientEntry(TestFeedFactory.TEST_SUBJECT));
    }

    @Test
    public void canRetrieveLabEntries() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();
        assertNotNull(model.labEntries(TestFeedFactory.TEST_SUBJECT));
    }

    @Test
    public void canDetermineLatestQuestionnaire() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        QuestionnaireEntry entry = model.latestQuestionnaireEntry(TestFeedFactory.TEST_SUBJECT);
        assertNotNull(entry);
        assertEquals(LocalDate.of(2021, 8, 1), entry.authoredDateTime());

        assertNull(model.latestQuestionnaireEntry("Does not exist"));
    }
}