package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

class HasHadNonInternalRadiotherapy : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingTreatments = record.clinical().treatmentHistory()
            .filter { entry -> entry.treatments().any { it is Radiotherapy && it.isInternal != true } }

        return if (matchingTreatments.isNotEmpty()) {
            EvaluationFactory.pass("Has received non-internal radiotherapy in treatment(s) "
                    + concatLowercaseWithAnd(matchingTreatments.map(TreatmentHistoryEntry::treatmentName)))
        } else {
            EvaluationFactory.fail("Has not received any non-internal radiotherapy")
        }
    }
}