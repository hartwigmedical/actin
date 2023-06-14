package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toECG
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toInfectionStatus
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toOption
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toSecondaryPrimaries
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toStage
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toWHO
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class QuestionnaireCurationTest {
    @Test
    fun canCurateOption() {
        Assert.assertTrue(toOption("YES")!!)
        Assert.assertFalse(toOption("no")!!)
        Assert.assertNull(toOption(null))
        Assert.assertNull(toOption(Strings.EMPTY))
        Assert.assertNull(toOption("-"))
        Assert.assertNull(toOption("nvt"))
        Assert.assertNull(toOption("not an option"))
    }

    @Test
    fun canCurateStage() {
        Assert.assertEquals(TumorStage.IIB, toStage("IIb"))
        Assert.assertEquals(TumorStage.II, toStage("2"))
        Assert.assertEquals(TumorStage.III, toStage("3"))
        Assert.assertEquals(TumorStage.IV, toStage("4"))
        Assert.assertNull(toStage(null))
        Assert.assertNull(toStage(Strings.EMPTY))
        Assert.assertNull(toStage("not a stage"))
    }

    @Test
    fun canCurateWHO() {
        Assert.assertEquals(1, (toWHO("1") as Int).toLong())
        Assert.assertNull(toWHO(null))
        Assert.assertNull(toWHO(Strings.EMPTY))
        Assert.assertNull(toWHO("-1"))
        Assert.assertNull(toWHO("12"))
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
        Assert.assertNull(infectionStatus)
    }

    @Test
    fun shouldReturnNullForUnknownInfectionStatus() {
        val infectionStatus = toInfectionStatus("unknown")
        Assert.assertNull(infectionStatus)
    }

    @Test
    fun shouldExtractPositiveInfectionStatus() {
        val infectionDescription = "yes"
        val infectionStatus = toInfectionStatus(infectionDescription)
        Assert.assertNotNull(infectionStatus)
        Assert.assertTrue(infectionStatus!!.hasActiveInfection())
        Assert.assertEquals(infectionDescription, infectionStatus.description())
    }

    @Test
    fun shouldExtractNegativeInfectionStatus() {
        val infectionDescription = "no"
        val infectionStatus = toInfectionStatus(infectionDescription)
        Assert.assertNotNull(infectionStatus)
        Assert.assertFalse(infectionStatus!!.hasActiveInfection())
        Assert.assertEquals(infectionDescription, infectionStatus.description())
    }

    @Test
    fun shouldExtractInfectionDescriptionAndSetActive() {
        val infectionDescription = "infection"
        val infectionStatus = toInfectionStatus(infectionDescription)
        Assert.assertNotNull(infectionStatus)
        Assert.assertTrue(infectionStatus!!.hasActiveInfection())
        Assert.assertEquals(infectionDescription, infectionStatus.description())
    }

    @Test
    fun shouldReturnNullForEmptyECG() {
        Assert.assertNull(toECG(""))
    }

    @Test
    fun shouldReturnNullForUnknownECG() {
        Assert.assertNull(toECG("unknown"))
    }

    @Test
    fun shouldExtractPositiveECG() {
        val description = "yes"
        val status = toECG(description)
        Assert.assertNotNull(status)
        Assert.assertTrue(status!!.hasSigAberrationLatestECG())
        Assert.assertEquals(description, status.aberrationDescription())
    }

    @Test
    fun shouldExtractNegativeECG() {
        val description = "no"
        val status = toECG(description)
        Assert.assertNotNull(status)
        Assert.assertFalse(status!!.hasSigAberrationLatestECG())
        Assert.assertEquals(description, status.aberrationDescription())
    }

    @Test
    fun shouldExtractECGDescriptionAndIndicatePresence() {
        val description = "ECG"
        val status = toECG(description)
        Assert.assertNotNull(status)
        Assert.assertTrue(status!!.hasSigAberrationLatestECG())
        Assert.assertEquals(description, status.aberrationDescription())
    }
}