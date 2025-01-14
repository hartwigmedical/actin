package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy

class HasHadNonInternalRadiotherapy : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingTreatments = record.oncologicalHistory
            .filter { entry -> entry.treatments.any { it is Radiotherapy && it.isInternal != true } }

        return if (matchingTreatments.isNotEmpty()) {
            EvaluationFactory.pass(
                "Has received non-internal radiotherapy (" +
                        Format.concatLowercaseWithCommaAndAnd(matchingTreatments.map(TreatmentHistoryEntryFunctions::fullTreatmentDisplay))
                        + ")"
            )
        } else {
            EvaluationFactory.fail("Has not received any non-internal radiotherapy")
        }
    }
}