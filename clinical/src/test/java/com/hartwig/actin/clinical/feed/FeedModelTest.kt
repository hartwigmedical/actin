package com.hartwig.actin.clinical.feed

import com.google.common.io.Resources
import com.hartwig.actin.clinical.feed.FeedModel.Companion.fromFeedDirectory
import org.junit.Assert
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class FeedModelTest {
    @Test
    @Throws(IOException::class)
    fun canCreateFromFeedDirectory() {
        Assert.assertNotNull(fromFeedDirectory(CLINICAL_FEED_DIRECTORY))
    }

    @Test(expected = IllegalStateException::class)
    fun crashOnInvalidSubject() {
        val model = TestFeedFactory.createProperTestFeedModel()
        model.patientEntry("does not exist")
    }

    @Test
    fun canRetrieveSubjects() {
        val model = TestFeedFactory.createProperTestFeedModel()
        val subjects: Set<String> = model.subjects()
        Assert.assertEquals(1, subjects.size.toLong())
        Assert.assertTrue(subjects.contains(TestFeedFactory.TEST_SUBJECT))
    }

    @Test
    fun canRetrievePatientEntry() {
        val model = TestFeedFactory.createProperTestFeedModel()
        Assert.assertNotNull(model.patientEntry(TestFeedFactory.TEST_SUBJECT))
    }

    @Test
    fun canRetrieveToxicityQuestionnaireEntries() {
        val model = TestFeedFactory.createProperTestFeedModel()
        val toxicities = model.toxicityEntries(TestFeedFactory.TEST_SUBJECT)
        Assert.assertEquals(3, toxicities.size.toLong())
    }

    @Test
    fun canDetermineLatestQuestionnaire() {
        val model = TestFeedFactory.createProperTestFeedModel()
        val latest = model.latestQuestionnaireEntry(TestFeedFactory.TEST_SUBJECT)
        Assert.assertNotNull(latest)
        Assert.assertEquals(LocalDate.of(2021, 8, 1), latest!!.authored)
        Assert.assertNull(model.latestQuestionnaireEntry("Does not exist"))
    }

    @Test
    fun canRetrieveUniqueSurgeryEntries() {
        val model = TestFeedFactory.createProperTestFeedModel()
        Assert.assertEquals(1, model.uniqueSurgeryEntries(TestFeedFactory.TEST_SUBJECT).size.toLong())
    }

    @Test
    fun canRetrieveMedicationEntries() {
        val model = TestFeedFactory.createProperTestFeedModel()
        Assert.assertNotNull(model.medicationEntries(TestFeedFactory.TEST_SUBJECT))
    }

    @Test
    fun canRetrieveLabEntries() {
        val model = TestFeedFactory.createProperTestFeedModel()
        Assert.assertNotNull(model.labEntries(TestFeedFactory.TEST_SUBJECT))
    }

    @Test
    fun canRetrieveVitalFunctionEntries() {
        val model = TestFeedFactory.createProperTestFeedModel()
        Assert.assertNotNull(model.vitalFunctionEntries(TestFeedFactory.TEST_SUBJECT))
    }

    @Test
    fun canRetrieveIntoleranceEntries() {
        val model = TestFeedFactory.createProperTestFeedModel()
        Assert.assertNotNull(model.intoleranceEntries(TestFeedFactory.TEST_SUBJECT))
    }

    @Test
    fun canRetrieveBodyWeightEntries() {
        val model = TestFeedFactory.createProperTestFeedModel()
        Assert.assertNotNull(model.uniqueBodyWeightEntries(TestFeedFactory.TEST_SUBJECT))
    }

    companion object {
        private val CLINICAL_FEED_DIRECTORY = Resources.getResource("feed").path
    }
}