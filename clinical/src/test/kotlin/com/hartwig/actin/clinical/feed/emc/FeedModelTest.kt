package com.hartwig.actin.clinical.feed.emc

import com.google.common.io.Resources
import com.hartwig.actin.clinical.feed.emc.FeedModel.Companion.fromFeedDirectory
import org.junit.Assert
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class FeedModelTest {

    @Test
    @Throws(IOException::class)
    fun `Should correctly create from feed directory`() {
        Assert.assertNotNull(fromFeedDirectory(CLINICAL_FEED_DIRECTORY))
    }

    @Test(expected = IllegalStateException::class)
    fun `Should crash on invalid subject`() {
        val model = TestFeedFactory.createProperTestFeedModel()
        model.patientEntry("does not exist")
    }

    @Test
    fun `Should be able to retrieve subjects`() {
        val model = TestFeedFactory.createProperTestFeedModel()
        val subjects: Set<String> = model.subjects()
        Assert.assertEquals(1, subjects.size.toLong())
        Assert.assertTrue(subjects.contains(TestFeedFactory.TEST_SUBJECT))
    }

    @Test
    fun `Should be able to retrieve patient entry`() {
        val model = TestFeedFactory.createProperTestFeedModel()
        Assert.assertNotNull(model.patientEntry(TestFeedFactory.TEST_SUBJECT))
    }

    @Test
    fun `Should be able to retrieve toxicity questionnaire entries`() {
        val model = TestFeedFactory.createProperTestFeedModel()
        val toxicities = model.toxicityEntries(TestFeedFactory.TEST_SUBJECT)
        Assert.assertEquals(3, toxicities.size.toLong())
    }

    @Test
    fun `Should be able to determine latest questionnaire`() {
        val model = TestFeedFactory.createProperTestFeedModel()
        val latest = model.latestQuestionnaireEntry(TestFeedFactory.TEST_SUBJECT)
        Assert.assertNotNull(latest)
        Assert.assertEquals(LocalDate.of(2021, 8, 1), latest!!.authored)
        Assert.assertNull(model.latestQuestionnaireEntry("Does not exist"))
    }

    @Test
    fun `Should be able to retrieve unique surgery entries`() {
        val model = TestFeedFactory.createProperTestFeedModel()
        Assert.assertEquals(1, model.uniqueSurgeryEntries(TestFeedFactory.TEST_SUBJECT).size.toLong())
    }

    @Test
    fun `Should be able to retrieve medication entries`() {
        val model = TestFeedFactory.createProperTestFeedModel()
        Assert.assertNotNull(model.medicationEntries(TestFeedFactory.TEST_SUBJECT))
    }

    @Test
    fun `Should be able to retrieve lab entries`() {
        val model = TestFeedFactory.createProperTestFeedModel()
        Assert.assertNotNull(model.labEntries(TestFeedFactory.TEST_SUBJECT))
    }

    @Test
    fun `Should be able to retrieve unique vital function entries`() {
        val model = TestFeedFactory.createProperTestFeedModel()
        Assert.assertNotNull(model.vitalFunctionEntries(TestFeedFactory.TEST_SUBJECT))
        Assert.assertEquals(3, model.vitalFunctionEntries(TestFeedFactory.TEST_SUBJECT).size.toLong())
        Assert.assertEquals("Diastolic blood pressure", model.vitalFunctionEntries(TestFeedFactory.TEST_SUBJECT)[2].componentCodeDisplay)
    }

    @Test
    fun `Should be able to retrieve intolerance entries`() {
        val model = TestFeedFactory.createProperTestFeedModel()
        Assert.assertNotNull(model.intoleranceEntries(TestFeedFactory.TEST_SUBJECT))
    }

    @Test
    fun `Should be able to retrieve unique body weight entries`() {
        val model = TestFeedFactory.createProperTestFeedModel()
        Assert.assertNotNull(model.uniqueBodyWeightEntries(TestFeedFactory.TEST_SUBJECT))
        Assert.assertEquals(3, model.uniqueBodyWeightEntries(TestFeedFactory.TEST_SUBJECT).size.toLong())
    }

    companion object {
        private val CLINICAL_FEED_DIRECTORY = Resources.getResource("feed.emc").path
    }
}