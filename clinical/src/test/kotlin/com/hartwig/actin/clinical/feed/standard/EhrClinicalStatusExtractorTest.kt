package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class EhrClinicalStatusExtractorTest {

    @Test
    fun `Should extract clinical status with who status and has complications`() {
        val ehrPatientRecord = createEhrPatientRecord().copy(
            whoEvaluations = listOf(
                EhrWhoEvaluation(
                    evaluationDate = LocalDate.of(2024, 2, 23),
                    status = "1",
                )
            ),
            complications = listOf(
                EhrComplication(name = "complication", startDate = LocalDate.of(2024, 2, 26), endDate = LocalDate.of(2024, 2, 26))
            )
        )
        val result = EhrClinicalStatusExtractor().extract(ehrPatientRecord)
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).isEqualTo(
            ClinicalStatus(
                who = 1,
                hasComplications = true
            )
        )
    }

    @Test
    fun `Should extract clinical status with who status as range and no complications`() {
        val ehrPatientRecord = createEhrPatientRecord().copy(
            whoEvaluations = listOf(
                EhrWhoEvaluation(
                    evaluationDate = LocalDate.of(2024, 2, 23),
                    status = "1-2",
                )
            )
        )
        val result = EhrClinicalStatusExtractor().extract(ehrPatientRecord)
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
        val result = EhrClinicalStatusExtractor().extract(ehrPatientRecord)
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).isEqualTo(
            ClinicalStatus(
                who = null,
                hasComplications = false
            )
        )
    }

}