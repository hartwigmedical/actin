package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCycles(private val type: TreatmentType, private val minCycles: Int) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentMatches = ChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCyclesEvaluator.treatmentmatches(
            record.oncologicalHistory,
            type
        ) { it.treatmentHistoryDetails?.cycles?.let { cycles -> cycles >= minCycles } }

        val typeString = type.display()
        return when {
            true in treatmentMatches -> EvaluationFactory.pass("Had received chemoradiotherapy with $typeString chemotherapy and at least $minCycles cycles")
            null in treatmentMatches -> EvaluationFactory.undetermined("Undetermined if patient received chemoradiotherapy with $typeString chemotherapy and at least $minCycles cycles")
            else -> EvaluationFactory.fail("Has not received chemoradiotherapy with $typeString chemotherapy")
        }
    }
}