package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toECG
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toInfectionStatus
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toOption
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toSecondaryPrimaries
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toStage
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toWHO
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.Test

class QuestionnaireCurationTest {
    @Test
    fun shouldCurateOptionsWhenCurationExists() {
        val curated = toOption("YES")
        assertThat(curated.curated).isTrue()
        assertThat(curated.errors).isEmpty()
    }

    @Test
    fun shouldNotCurateOptionsAndReturnErrorWhenCurationDoesNotExist() {
        val curated = toOption("Not an option")
        assertThat(curated.curated).isNull()
        assertThat(curated.errors).containsExactly(QuestionnaireCurationError("Unrecognized questionnaire option: 'Not an option'"))
    }

    @Test
    fun shouldCurateTumorStageWhenCurationExists() {
        val curated = toStage("IIb")
        assertThat(curated.curated).isEqualTo(TumorStage.IIB)
        assertThat(curated.errors).isEmpty()
    }

    @Test
    fun shouldNotCurateTumorStageAndReturnErrorWhenCurationDoesNotExist() {
        val curated = toStage("Not a stage")
        assertThat(curated.curated).isNull()
        assertThat(curated.errors).containsExactly(QuestionnaireCurationError("Unrecognized questionnaire tumor stage: 'Not a stage'"))
    }

    @Test
    fun shouldCurateWhoStatusWhenCurationExists() {
        val curated = toWHO("1")
        assertThat(curated.curated).isEqualTo(1)
        assertThat(curated.errors).isEmpty()
    }

    @Test
    fun shouldNotCurateWhoStatusAndReturnErrorWhenNotBetweenZeroAndFive() {
        val curated = toWHO("7")
        assertThat(curated.curated).isEqualTo(null)
        assertThat(curated.errors).containsExactly(QuestionnaireCurationError("WHO status not between 0 and 5: '7'"))
    }

    @Test
    fun shouldNotCurateWhoStatusAndReturznErrorWhenNotANumber() {
        val curated = toWHO("string")
        assertThat(curated.curated).isEqualTo(null)
        assertThat(curated.errors).containsExactly(QuestionnaireCurationError("WHO status not an integer: 'string'"))
    }

    @Test
    fun shouldNotCurateWhoStatusAndReturnErrorWhenWhoInvalid() {
        val curated = toWHO("7")
        assertThat(curated.curated).isEqualTo(null)
        assertThat(curated.errors).containsExactly(QuestionnaireCurationError("WHO status not between 0 and 5: '7'"))
    }

    @Test
    fun shouldExtractSecondaryPrimaryAndLastTreatmentDateWhenAvailable() {
        Assert.assertEquals(listOf("sarcoma | Feb 2020"), toSecondaryPrimaries("sarcoma", "Feb 2020"))
    }

    @Test
    fun shouldExtractSecondaryPrimaryWhenLastTreatmentDateNotAvailable() {
        Assert.assertEquals(listOf("sarcoma"), toSecondaryPrimaries("sarcoma", ""))
    }

    @Test
    fun shouldReturnNullForEmptyInfectionStatus() {
        val infectionStatus = toInfectionStatus("")
        assertThat(infectionStatus.curated).isNull()
        assertThat(infectionStatus.errors).isEmpty()
    }

    @Test
    fun shouldReturnNullForUnknownInfectionStatus() {
        val infectionStatus = toInfectionStatus("unknown")
        assertThat(infectionStatus.curated).isNull()
        assertThat(infectionStatus.errors).isEmpty()
    }

    @Test
    fun shouldReturnDescriptionVerbatimForWhenCurationDoesNotExistInfectionStatus() {
        val infectionStatus = toInfectionStatus("new infection status")
        val curated = infectionStatus.curated!!
        assertThat(curated.description()).isEqualTo("new infection status")
        assertThat(curated.hasActiveInfection()).isTrue()
        assertThat(infectionStatus.errors).isEmpty()
    }

    @Test
    fun shouldExtractPositiveInfectionStatus() {
        val infectionDescription = "yes"
        val infectionStatus = toInfectionStatus(infectionDescription)
        Assert.assertNotNull(infectionStatus)
        val curated = infectionStatus.curated!!
        Assert.assertTrue(curated.hasActiveInfection())
        Assert.assertEquals(infectionDescription, curated.description())
    }

    @Test
    fun shouldExtractNegativeInfectionStatus() {
        val infectionDescription = "no"
        val infectionStatus = toInfectionStatus(infectionDescription)
        Assert.assertNotNull(infectionStatus)
        val curated = infectionStatus.curated!!
        Assert.assertFalse(curated.hasActiveInfection())
        Assert.assertEquals(infectionDescription, curated.description())
    }

    @Test
    fun shouldExtractInfectionDescriptionAndSetActive() {
        val infectionDescription = "infection"
        val infectionStatus = toInfectionStatus(infectionDescription)
        Assert.assertNotNull(infectionStatus)
        val curated = infectionStatus.curated!!
        Assert.assertTrue(curated.hasActiveInfection())
        Assert.assertEquals(infectionDescription, curated.description())
    }

    @Test
    fun shouldReturnNullForEmptyECG() {
        val ecg = toECG("")
        assertThat(ecg.curated).isNull()
        assertThat(ecg.errors).isEmpty()
    }

    @Test
    fun shouldReturnNullForUnknownECG() {
        val ecg = toECG("unknown")
        assertThat(ecg.curated).isNull()
        assertThat(ecg.errors).isEmpty()
    }

    @Test
    fun shouldReturnDescriptionVerbatimForWhenCurationDoesNotExistECG() {
        val infectionStatus = toECG("new ECG value")
        val curated = infectionStatus.curated!!
        assertThat(curated.aberrationDescription()).isEqualTo("new ECG value")
        assertThat(curated.hasSigAberrationLatestECG()).isTrue()
        assertThat(infectionStatus.errors).isEmpty()
    }

    @Test
    fun shouldExtractPositiveECG() {
        val description = "yes"
        val status = toECG(description)
        Assert.assertNotNull(status)
        val curated = status.curated!!
        Assert.assertTrue(curated.hasSigAberrationLatestECG())
        Assert.assertEquals(description, curated.aberrationDescription())
    }

    @Test
    fun shouldExtractNegativeECG() {
        val description = "no"
        val status = toECG(description)
        Assert.assertNotNull(status)
        val curated = status.curated!!
        Assert.assertFalse(curated.hasSigAberrationLatestECG())
        Assert.assertEquals(description, curated.aberrationDescription())
    }

    @Test
    fun shouldExtractECGDescriptionAndIndicatePresence() {
        val description = "ECG"
        val status = toECG(description)
        Assert.assertNotNull(status)
        val curated = status.curated!!
        Assert.assertTrue(curated.hasSigAberrationLatestECG())
        Assert.assertEquals(description, curated.aberrationDescription())
    }
}