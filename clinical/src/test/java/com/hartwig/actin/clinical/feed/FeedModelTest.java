package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.google.common.io.Resources;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;

import org.junit.Test;

public class FeedModelTest {

    private static final String CLINICAL_FEED_DIRECTORY = Resources.getResource("feed").getPath();

    @Test
    public void canCreateFromFeedDirectory() throws IOException {
        assertNotNull(FeedModel.fromFeedDirectory(CLINICAL_FEED_DIRECTORY));
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnInvalidSubject() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        model.patientEntry("does not exist");
    }

    @Test
    public void canRetrieveSubjects() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        Set<String> subjects = model.subjects();
        assertEquals(1, subjects.size());
        assertTrue(subjects.contains(TestFeedFactory.TEST_SUBJECT));
    }

    @Test
    public void canRetrievePatientEntry() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();
        assertNotNull(model.patientEntry(TestFeedFactory.TEST_SUBJECT));
    }

    @Test
    public void canRetrieveToxicityQuestionnaireEntries() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        List<QuestionnaireEntry> toxicities = model.toxicityQuestionnaireEntries(TestFeedFactory.TEST_SUBJECT);

        assertEquals(2, toxicities.size());
    }

    @Test
    public void canDetermineLatestQuestionnaire() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        assertNotNull(model.latestQuestionnaireEntry(TestFeedFactory.TEST_SUBJECT));
        assertNull(model.latestQuestionnaireEntry("Does not exist"));
    }

    @Test
    public void canRetrieveEncounterEntries() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        assertNotNull(model.encounterEntries(TestFeedFactory.TEST_SUBJECT));
    }

    @Test
    public void canRetrieveMedicationEntries() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        assertNotNull(model.medicationEntries(TestFeedFactory.TEST_SUBJECT));
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