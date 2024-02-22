package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import java.time.LocalDate

class HasHadSpecificTreatmentSinceDate(private val treatment: Treatment, private val minDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return TreatmentSinceDateFunctions.evaluateTreatmentMatchingPredicateSinceDate(
            record, minDate, "matching '${treatment.display()}'"
        ) { it.name == treatment.name }
    }
}