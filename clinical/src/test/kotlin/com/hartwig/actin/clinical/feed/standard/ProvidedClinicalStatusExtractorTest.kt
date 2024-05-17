package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ProvidedClinicalStatusExtractorTest {

    @Test
    fun `Should extract clinical status with WHO status and has complications`() {
        val ehrPatientRecord = createEhrPatientRecord().copy(
            whoEvaluations = listOf(
                ProvidedWhoEvaluation(
                    evaluationDate = LocalDate.of(2024, 2, 23),
                    status = "1",
                )
            ),
            complications = listOf(
                ProvidedComplication(name = "complication", startDate = LocalDate.of(2024, 2, 26), endDate = LocalDate.of(2024, 2, 26))
            )
        )
        val result = ProvidedClinicalStatusExtractor().extract(ehrPatientRecord)
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).isEqualTo(
            ClinicalStatus(
                who = 1,
                hasComplications = true
            )
        )
    }

    @Test
    fun `Should extract clinical status with WHO status as range and no complications`() {
        val ehrPatientRecord = createEhrPatientRecord().copy(
            whoEvaluations = listOf(
                ProvidedWhoEvaluation(
                    evaluationDate = LocalDate.of(2024, 2, 23),
                    status = "1-2",
                )
            )
        )
        val result = ProvidedClinicalStatusExtractor().extract(ehrPatientRecord)
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).isEqualTo(
            ClinicalStatus(
                who = 1,
                hasComplications = false
            )
        )
    }

    @Test
    fun `Should extract clinical status when there are no who evaluations`() {
        val ehrPatientRecord = createEhrPatientRecord()
        val result = ProvidedClinicalStatusExtractor().extract(ehrPatientRecord)
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).isEqualTo(
            ClinicalStatus(
                who = null,
                hasComplications = false
            )
        )
    }

}