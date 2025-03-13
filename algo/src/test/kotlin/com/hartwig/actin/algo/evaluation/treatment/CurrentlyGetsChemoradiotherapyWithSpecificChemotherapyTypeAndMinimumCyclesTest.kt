package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.junit.Test

class CurrentlyGetsChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCyclesTest {
    @Test
    fun `Fail if there is no treatments`() {
        val record = TreatmentTestFactory.withTreatmentHistory(emptyList())
        assert(EvaluationResult.FAIL, DrugType.ALK_INHIBITOR, 10, record)
    }

    @Test
    fun `Should pass if there is a match` () {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(TreatmentTestFactory.drugTreatment("Alk Inhibitor",
                TreatmentCategory.CHEMOTHERAPY,
                setOf(DrugType.ALK_INHIBITOR))
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(cycles = 10)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assert(EvaluationResult.PASS, DrugType.ALK_INHIBITOR, 1, record)
    }

    private fun assert(evaluationResult: EvaluationResult, type: TreatmentType, minCycles: Int, record: PatientRecord) {
        val evaluation = CurrentlyGetsChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCycles(type, minCycles).evaluate(record)
        return EvaluationAssert.assertEvaluation(evaluationResult, evaluation)
    }
}