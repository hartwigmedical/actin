package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

class HasHadSomeSpecificTreatments(private val treatments: List<Treatment>, private val minTreatmentLines: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val namesToMatch = treatments.map { it.name().lowercase() }.toSet()
        val matchTreatments = record.clinical().treatmentHistory()
            .filter { it.treatments().any { treatment -> treatment.name().lowercase() in namesToMatch } }
            .map(TreatmentHistoryEntry::treatmentName)

        return if (matchTreatments.size >= minTreatmentLines) {
            EvaluationFactory.pass("Has received ${concat(namesToMatch)} ${matchTreatments.size} times")
        } else {
            EvaluationFactory.fail("Has not received ${concat(namesToMatch)} at least $minTreatmentLines times")
        }
    }
}