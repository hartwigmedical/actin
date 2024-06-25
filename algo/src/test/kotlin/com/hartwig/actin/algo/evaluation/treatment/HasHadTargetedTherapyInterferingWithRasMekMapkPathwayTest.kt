package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.DrugType.Companion.rasMekMapkDirectlyTargetingDrugSet
import com.hartwig.actin.clinical.datamodel.treatment.DrugType.Companion.rasMekMapkIndirectlyTargetingDrugSet
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.assertj.core.api.Assertions
import org.junit.Test

class HasHadTargetedTherapyInterferingWithRasMekMapkPathwayTest {

    private val function = HasHadTargetedTherapyInterferingWithRasMekMapkPathway()

    @Test
    fun `Should fail for no treatments`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for wrong treatment category`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should pass for correct treatment category`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment("test", TreatmentCategory.TARGETED_THERAPY, rasMekMapkDirectlyTargetingDrugSet)
                )
            )
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry)))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, evaluation)
        Assertions.assertThat(evaluation.passGeneralMessages).containsExactly(
            "Has had targeted therapy interfering with RAS/MEK/MAPK pathway (Test)"
        )
    }

    @Test
    fun `Should resolve to undetermined for drug type with indirect interference with pathway`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment("test", TreatmentCategory.TARGETED_THERAPY, rasMekMapkIndirectlyTargetingDrugSet)
                )
            )
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry)))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        Assertions.assertThat(evaluation.undeterminedGeneralMessages).containsExactly(
            "Has had targeted therapy (Test) - undetermined interference with RAS/MEK/MAPK pathway"
        )
    }

    @Test
    fun `Should resolve to undetermined for possible trial match`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.treatment("trial", true)), isTrial = true
            )
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry)))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        Assertions.assertThat(evaluation.undeterminedGeneralMessages).containsExactly(
            "Has had trial drug - undetermined interference with RAS/MEK/MAPK pathway"
        )
    }

    @Test
    fun `Should fail for wrong drug type`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(setOf(
                TreatmentTestFactory.drugTreatment("test", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR)))
            )
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation)
        Assertions.assertThat(evaluation.failGeneralMessages).containsExactly(
            "Has not received targeted therapy interfering with RAS/MEK/MAPK pathway"
        )
    }
}