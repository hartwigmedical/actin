package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.google.common.io.Resources;
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;

import org.junit.Test;

public class FeedModelTest {

    private static final String CLINICAL_FEED_DIRECTORY = Resources.getResource("feed").getPath();
    private static final String CURATION_DIRECTORY = Resources.getResource("curation").getPath();

    @Test
    public void canCreateFromFeedDirectory() throws IOException {
        assertNotNull(FeedModel.fromFeedAndCurationDirectories(CLINICAL_FEED_DIRECTORY, CURATION_DIRECTORY));
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

        List<DigitalFileEntry> toxicities = model.toxicityEntries(TestFeedFactory.TEST_SUBJECT);

        assertEquals(3, toxicities.size());
    }

    @Test
    public void canDetermineLatestQuestionnaire() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        QuestionnaireEntry latest = model.latestQuestionnaireEntry(TestFeedFactory.TEST_SUBJECT);
        assertNotNull(latest);
        assertEquals(LocalDate.of(2021, 8, 1), latest.authored());
        assertNull(model.latestQuestionnaireEntry("Does not exist"));
    }

    @Test
    public void canRetrieveUniqueSurgeryEntries() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        assertEquals(1, model.uniqueSurgeryEntries(TestFeedFactory.TEST_SUBJECT).size());
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
    public void canRetrieveVitalFunctionEntries() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        assertNotNull(model.vitalFunctionEntries(TestFeedFactory.TEST_SUBJECT));
    }

    @Test
    public void canRetrieveIntoleranceEntries() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        assertNotNull(model.intoleranceEntries(TestFeedFactory.TEST_SUBJECT));
    }

    @Test
    public void canRetrieveBodyWeightEntries() {
        FeedModel model = TestFeedFactory.createProperTestFeedModel();

        assertNotNull(model.uniqueBodyWeightEntries(TestFeedFactory.TEST_SUBJECT));
    }
}