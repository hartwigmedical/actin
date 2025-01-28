package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class CurrentlyGetsChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCycles(
    private val selector: MedicationSelector,
    private val type: TreatmentType,
    private val minCycles: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Undetermined if patient is currently getting chemoradiotherapy with $type chemotherapy and at least $minCycles cycles"
        )
    }
}