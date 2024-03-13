package com.hartwig.actin.clinical.feed.emc

import com.google.common.io.Resources
import com.hartwig.actin.clinical.feed.emc.FeedModel.Companion.fromFeedDirectory
import java.io.IOException
import java.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class FeedModelTest {
    lateinit var model: FeedModel
    lateinit var feedRecord: FeedRecord

    @Before
    fun setUp() {
        model = TestFeedFactory.createProperTestFeedModel()
        feedRecord = model.toNewWay()[TestFeedFactory.TEST_SUBJECT]!!
    }

    @Test
    @Throws(IOException::class)
    fun `Should correctly create from feed directory`() {
        Assert.assertNotNull(fromFeedDirectory(CLINICAL_FEED_DIRECTORY))
    }

    @Test
    fun `Should be able to retrieve toxicity questionnaire entries`() {
        val toxicities = feedRecord.toxicityEntries()
        Assert.assertEquals(3, toxicities.size.toLong())
    }

    @Test
    fun `Should be able to determine latest questionnaire`() {
        val latest = feedRecord.latestQuestionnaireEntry()
        Assert.assertNotNull(latest)
        Assert.assertEquals(LocalDate.of(2021, 8, 1), latest!!.authored)
    }

    @Test
    fun `Should be able to retrieve unique surgery entries`() {
        Assert.assertEquals(1, feedRecord.uniqueSurgeryEntries().size.toLong())
    }

    @Test
    fun `Should be able to retrieve medication entries`() {
        Assert.assertNotNull(feedRecord.medicationEntries())
    }

    @Test
    fun `Should be able to retrieve lab entries`() {
        Assert.assertNotNull(feedRecord.labEntries())
    }

    @Test
    fun `Should be able to retrieve unique vital function entries`() {
        Assert.assertNotNull(feedRecord.vitalFunctionEntries())
        Assert.assertEquals(3, feedRecord.vitalFunctionEntries().size.toLong())
        Assert.assertEquals("Diastolic blood pressure", feedRecord.vitalFunctionEntries()[2].componentCodeDisplay)
    }

    @Test
    fun `Should be able to retrieve intolerance entries`() {
        Assert.assertNotNull(feedRecord.intoleranceEntries())
    }

    @Test
    fun `Should be able to retrieve unique body weight entries`() {
        Assert.assertNotNull(feedRecord.uniqueBodyWeightEntries())
        Assert.assertEquals(3, feedRecord.uniqueBodyWeightEntries().size.toLong())
    }

    companion object {
        private val CLINICAL_FEED_DIRECTORY = Resources.getResource("feed/emc").path
    }
}