package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
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
    public void canFindToxicityQuestionnaireEntries() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        List<QuestionnaireEntry> toxicities = model.toxicityQuestionnaireEntries(TestFeedFactory.TEST_SUBJECT);

        assertEquals(2, toxicities.size());
    }

    @Test
    public void canDetermineLatestQuestionnaire() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        QuestionnaireEntry entry = model.latestQuestionnaireEntry(TestFeedFactory.TEST_SUBJECT);
        assertNotNull(entry);
        assertEquals(LocalDate.of(2021, 8, 1), entry.authoredDateTime());

        assertNull(model.latestQuestionnaireEntry("Does not exist"));
    }

    @Test
    public void canRetrieveEncounterEntries() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();
        assertNotNull(model.encounterEntries(TestFeedFactory.TEST_SUBJECT));
    }

    @Test
    public void canRetrieveLabEntries() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();
        assertNotNull(model.labEntries(TestFeedFactory.TEST_SUBJECT));
    }

    @Test
    public void canRetrieveBloodPressureEntries() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();
        assertNotNull(model.bloodPressureEntries(TestFeedFactory.TEST_SUBJECT));
    }

    @Test
    public void canRetrieveIntoleranceEntries() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();
        assertNotNull(model.intoleranceEntries(TestFeedFactory.TEST_SUBJECT));
    }
}