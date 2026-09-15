package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.jupiter.api.Test

class HasHadTreatmentWithCategoryOfTypesAsMostRecentTest {
    private val function = HasHadTreatmentWithCategoryOfTypesAsMostRecent(TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.EGFR_INHIBITOR_GEN_3))
    private val functionWithoutType = HasHadTreatmentWithCategoryOfTypesAsMostRecent(TreatmentCategory.TARGETED_THERAPY, null)

    @Test
    fun `Should fail if treatment history empty`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(emptyList())),
            "No prior anti cancer drugs in provided treatments"
        )
    }

    @Test
    fun `Should fail if no anti cancer drugs in treatment history`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                setOf(drugTreatment("Prednisone", TreatmentCategory.SUPPORTIVE_TREATMENT, setOf(DrugType.CORTICOSTEROID))), 2023, 5
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(treatmentHistory)),
            "No prior anti cancer drugs in provided treatments"
        )
    }

    @Test
    fun `Should evaluate specified category if type is null`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                setOf(drugTreatment("Osimertinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.EGFR_INHIBITOR_GEN_3))), 2020, 5
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            functionWithoutType.evaluate(withTreatmentHistory(treatmentHistory)),
            "Targeted therapy in provided treatments as most recent treatment line"
        )
    }

    @Test
    fun `Should pass if most recent drug is of right type`() {
        val treatmentHistoryEntry = listOf(
            treatmentHistoryEntry(
                setOf(drugTreatment("Osimertinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.EGFR_INHIBITOR_GEN_3))), 2020, 5
            ),
            treatmentHistoryEntry(
                setOf(drugTreatment("Alectinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR))), 2019, 7
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistory(treatmentHistoryEntry)),
            "EGFR inhibitor (3rd gen) targeted therapy in provided treatments as most recent treatment line"
        )
    }

    @Test
    fun `Should fail if right type but not most recent`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                setOf(drugTreatment("Osimertinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.EGFR_INHIBITOR_GEN_3))), 2020, 5
            ),
            treatmentHistoryEntry(
                setOf(drugTreatment("Alectinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR))), 2021, 5
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(treatmentHistory)),
            "EGFR inhibitor (3rd gen) targeted therapy in provided treatments but not as the most recent treatment line"
        )
    }

    @Test
    fun `Should fail if type null and specified category not most recent`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                setOf(drugTreatment("Osimertinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.EGFR_INHIBITOR_GEN_3))), 2020, 5
            ),
            treatmentHistoryEntry(setOf(drugTreatment("Carboplatin", TreatmentCategory.CHEMOTHERAPY)), 2021, 5)
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithoutType.evaluate(withTreatmentHistory(treatmentHistory)),
            "targeted therapy in provided treatments but not as the most recent treatment line"
        )
    }

    @Test
    fun `Should fail if type not in treatment history`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                setOf(drugTreatment("Alectinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR))), 2021, 5
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(treatmentHistory)),
            "No egfr inhibitor (3rd gen) targeted therapy in provided treatments"
        )
    }

    @Test
    fun `Should evaluate to undetermined if start year missing in a prior treatment`() {
        val treatmentHistoryEntry = listOf(
            treatmentHistoryEntry(
                setOf(drugTreatment("Alectinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR))), 2021, 5
            ),
            treatmentHistoryEntry(
                setOf(drugTreatment("Osimertinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.EGFR_INHIBITOR_GEN_3)))
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistory(treatmentHistoryEntry)),
            "EGFR inhibitor (3rd gen) targeted therapy in provided treatments but undetermined if most recent (date unknown)"
        )
    }
}