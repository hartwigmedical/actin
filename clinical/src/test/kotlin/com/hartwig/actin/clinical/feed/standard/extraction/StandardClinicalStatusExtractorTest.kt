package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.feed.standard.EhrTestData
import com.hartwig.actin.clinical.feed.standard.ProvidedComplication
import com.hartwig.actin.clinical.feed.standard.ProvidedWhoEvaluation
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val SOME_DATE = LocalDate.of(2024, 10, 4)

class StandardClinicalStatusExtractorTest {

    private val extractor = StandardClinicalStatusExtractor()

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
}