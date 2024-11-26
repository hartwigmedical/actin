package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.clinical.feed.emc.FeedModel.Companion.fromFeedDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class FeedModelTest {

    private val model: FeedModel = TestFeedFactory.createProperTestFeedModel()
    private val feedRecord: FeedRecord = model.read().single()

    @Test
    fun `Should correctly create from feed directory`() {
        assertThat(fromFeedDirectory(FEED_DIRECTORY)).isNotNull()
    }

    @Test
    fun `Should be able to retrieve toxicity questionnaire entries`() {
        val toxicities = feedRecord.toxicityEntries
        assertThat(toxicities.size).isEqualTo(3)
    }

    @Test
    fun `Should be able to determine latest questionnaire`() {
        val latest = feedRecord.latestQuestionnaireEntry
        assertThat(latest).isNotNull()
        assertThat(latest!!.authored).isEqualTo(LocalDate.of(2021, 8, 1))
    }

    @Test
    fun `Should be able to retrieve unique surgery entries`() {
        assertThat(feedRecord.uniqueSurgeryEntries.size).isEqualTo(1)
    }

    @Test
    fun `Should be able to retrieve medication entries`() {
        assertThat(feedRecord.medicationEntries).isNotNull()
    }

    @Test
    fun `Should be able to retrieve lab entries`() {
        assertThat(feedRecord.labEntries).isNotNull()
    }

    @Test
    fun `Should be able to retrieve unique vital function entries`() {
        assertThat(feedRecord.uniqueVitalFunctionEntries)
        assertThat(feedRecord.uniqueVitalFunctionEntries.size).isEqualTo(3)
        assertThat(feedRecord.uniqueVitalFunctionEntries[2].componentCodeDisplay).isEqualTo("Diastolic blood pressure")
    }

    @Test
    fun `Should be able to retrieve intolerance entries`() {
        assertThat(feedRecord.intoleranceEntries).isNotNull()
    }

    @Test
    fun `Should be able to retrieve unique body weight entries`() {
        assertThat(feedRecord.uniqueBodyWeightEntries).isNotNull()
        assertThat(feedRecord.uniqueBodyWeightEntries.size).isEqualTo(3)
    }
}