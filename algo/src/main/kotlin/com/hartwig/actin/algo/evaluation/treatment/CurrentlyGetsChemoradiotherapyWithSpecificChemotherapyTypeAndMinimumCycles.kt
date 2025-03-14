package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class CurrentlyGetsChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCycles(
    private val type: TreatmentType,
    private val minCycles: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentMatches = record.oncologicalHistory.groupBy { it ->
            when {
                (it.categories().containsAll(setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY)) &&
                        it.isOfType(type) == true &&
                        it.treatmentHistoryDetails?.cycles?.let { it > minCycles } == true) -> {
                    true
                }
                (it.categories().isNotEmpty() &&
                        it.isOfType(type) == false &&
                        it.treatmentHistoryDetails?.cycles != null) -> {
                    false
                }
                else -> {
                    null
                }
            }
        }

        return when {
            treatmentMatches.isEmpty() -> EvaluationFactory.fail("No treatments received")
            true in treatmentMatches -> EvaluationFactory.pass("Patient is currently getting chemoradiotherapy with $type chemotherapy and at least $minCycles cycles")
            null in treatmentMatches -> EvaluationFactory.undetermined("Undetermined if patient is currently getting chemoradiotherapy with $type chemotherapy and at least $minCycles cycles")
            else -> EvaluationFactory.fail("No chemo radio therapy with $type with at least $minCycles is currently received")
        }
    }
}