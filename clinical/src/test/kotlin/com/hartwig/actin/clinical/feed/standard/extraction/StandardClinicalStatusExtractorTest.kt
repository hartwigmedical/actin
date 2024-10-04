package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.feed.standard.EhrTestData
import com.hartwig.actin.clinical.feed.standard.ProvidedComplication
import com.hartwig.actin.clinical.feed.standard.ProvidedPriorOtherCondition
import com.hartwig.actin.clinical.feed.standard.ProvidedWhoEvaluation
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.ECG
import com.hartwig.actin.datamodel.clinical.ECGMeasure
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val SOME_DATE = LocalDate.of(2024, 10, 4)

class StandardClinicalStatusExtractorTest {

    private val ecgCuration = mockk<CurationDatabase<ECGConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val extractor = StandardClinicalStatusExtractor(ecgCuration)

    @Test
    fun `Should support all clinical status fields unknown in provided data`() {
        assertThat(extractor.extract(EhrTestData.createEhrPatientRecord()).extracted).isEqualTo(ClinicalStatus(hasComplications = false))
    }

    @Test
    fun `Should take most recent WHO from who evaluations`() {
        val clinicalStatus = extractor.extract(
            EhrTestData.createEhrPatientRecord().copy(whoEvaluations = listOf(ProvidedWhoEvaluation(1, SOME_DATE)))
        )
        assertThat(clinicalStatus.extracted.who).isEqualTo(1)
    }

    @Test
    fun `Should set has complications to true when patient has complications`() {
        val clinicalStatus = extractor.extract(
            EhrTestData.createEhrPatientRecord()
                .copy(complications = listOf(ProvidedComplication("complication", startDate = SOME_DATE, endDate = null)))
        )
        assertThat(clinicalStatus.extracted.hasComplications).isTrue()
    }

    @Test
    fun `Should allow curation of ECG from prior other conditions`() {
        val conditionName = "condition"
        val ecgConfig = ECGConfig(
            conditionName,
            ignore = false,
            interpretation = "interpretation",
            isQTCF = true,
            qtcfValue = 1,
            qtcfUnit = "qtcfUnit",
            isJTC = true,
            jtcValue = 2,
            jtcUnit = "jtcUnit",
            hasSigAberrationLatestECG = true
        )
        every { ecgCuration.find(conditionName) } returns setOf(
            ecgConfig
        )
        val clinicalStatus = extractor.extract(
            EhrTestData.createEhrPatientRecord()
                .copy(priorOtherConditions = listOf(ProvidedPriorOtherCondition(conditionName, startDate = SOME_DATE, endDate = null)))
        )
        assertThat(clinicalStatus.extracted.ecg).isEqualTo(
            ECG(
                hasSigAberrationLatestECG = true,
                aberrationDescription = ecgConfig.interpretation,
                qtcfMeasure = ECGMeasure(ecgConfig.qtcfValue, ecgConfig.qtcfUnit),
                jtcMeasure = ECGMeasure(ecgConfig.jtcValue, ecgConfig.jtcUnit)
            )
        )
    }

}