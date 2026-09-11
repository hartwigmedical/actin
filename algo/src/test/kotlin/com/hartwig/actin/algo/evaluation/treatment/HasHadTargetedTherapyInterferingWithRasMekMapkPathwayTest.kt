package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.DrugType.Companion.RAS_MEK_MAPK_DIRECTLY_TARGETING_DRUG_SET
import com.hartwig.actin.datamodel.clinical.treatment.DrugType.Companion.RAS_MEK_MAPK_INDIRECTLY_TARGETING_DRUG_SET
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.jupiter.api.Test

class HasHadTargetedTherapyInterferingWithRasMekMapkPathwayTest {

    private val function = HasHadTargetedTherapyInterferingWithRasMekMapkPathway()

    @Test
    fun `Should fail for no treatments`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())),
            "No targeted therapy interfering with RAS/MEK/MAPK pathway in provided treatments"
        )
    }

    @Test
    fun `Should fail for wrong treatment category`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry))),
            "No targeted therapy interfering with RAS/MEK/MAPK pathway in provided treatments"
        )
    }

    @Test
    fun `Should pass for specific drug type`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment("test", TreatmentCategory.TARGETED_THERAPY, RAS_MEK_MAPK_DIRECTLY_TARGETING_DRUG_SET)
                )
            )
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry)))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            evaluation,
            "Targeted therapy interfering with RAS/MEK/MAPK pathway (Test) in provided treatments"
        )
    }

    @Test
    fun `Should warn for drug type with indirect interference with pathway`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "test",
                        TreatmentCategory.TARGETED_THERAPY,
                        RAS_MEK_MAPK_INDIRECTLY_TARGETING_DRUG_SET
                    )
                )
            )
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry)))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            evaluation,
            "Targeted therapy (Test) indirectly interfering with RAS/MEK/MAPK pathway in provided treatments"
        )
    }

    @Test
    fun `Should resolve to undetermined for possible trial match`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.treatment("trial", true)), isTrial = true
            )
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry)))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Trial drug in provided treatments - undetermined interference with RAS/MEK/MAPK pathway"
        )
    }

    @Test
    fun `Should fail for wrong drug type`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment("test", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR))
                )
            )
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry)))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            evaluation,
            "No targeted therapy interfering with RAS/MEK/MAPK pathway in provided treatments"
        )
    }
}