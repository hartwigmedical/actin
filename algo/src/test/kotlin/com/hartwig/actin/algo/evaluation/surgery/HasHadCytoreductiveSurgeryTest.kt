package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.surgery.SurgeryTestFactory.withOncologicalHistory
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import org.junit.Test


class HasHadCytoreductiveSurgeryTest {

    private val function = HasHadCytoreductiveSurgery()

    private fun treatmentHistoryEntry(
        categories: Set<TreatmentCategory>, name: String
    ): TreatmentHistoryEntry {
        return ImmutableTreatmentHistoryEntry.builder()
            .addTreatments(ImmutableOtherTreatment.builder().name(name).isSystemic(false).categories(categories).build())
            .build()
    }

    @Test
    fun `Should fail with no surgeries`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                withOncologicalHistory(
                    listOf(
                        treatmentHistoryEntry(
                            setOf(TreatmentCategory.HORMONE_THERAPY),
                            "Hormone therapy"
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail with non cytoreductive surgery`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withOncologicalHistory(listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), "Nephrectomy"))))
        )
    }

    @Test
    fun `Should pass with history of cytoreductive surgery or HIPEC`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withOncologicalHistory(listOf(treatmentHistoryEntry(setOf(TreatmentCategory.CHEMOTHERAPY), "HIPEC"))))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                withOncologicalHistory(
                    listOf(
                        treatmentHistoryEntry(
                            setOf(TreatmentCategory.SURGERY),
                            "Cytoreductive surgery"
                        )
                    )
                )
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                withOncologicalHistory(
                    listOf(
                        treatmentHistoryEntry(
                            setOf(TreatmentCategory.SURGERY),
                            "Colorectal cancer cytoreduction"
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should return undetermined if surgery name not specified`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withOncologicalHistory(listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), "Surgery"))))
        )
    }

}