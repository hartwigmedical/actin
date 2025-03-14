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

        val (knowableTreatments, undeterminedTreatments) = record.oncologicalHistory.filter {
            it.categories().containsAll(setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY)) &&
                    it.isOfType(type) == true
        }.partition {
            it.treatmentHistoryDetails?.cycles != null
        }
        val matchingTreatments = knowableTreatments.filter {
            it.treatmentHistoryDetails?.cycles!! > minCycles
        }

        return when {
            matchingTreatments.isNotEmpty() -> EvaluationFactory.pass("Patient is currently getting chemoradiotherapy with $type chemotherapy and at least $minCycles cycles")
            undeterminedTreatments.isNotEmpty() -> EvaluationFactory.undetermined("Undetermined if patient is currently getting chemoradiotherapy with $type chemotherapy and at least $minCycles cycles" )
            else -> EvaluationFactory.fail("No chemo radio therapy with $type with at least $minCycles is currently received")
        }
    }
}