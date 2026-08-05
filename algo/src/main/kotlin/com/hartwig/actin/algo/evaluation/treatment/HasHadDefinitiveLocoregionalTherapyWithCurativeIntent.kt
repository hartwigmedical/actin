package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

class HasHadDefinitiveLocoregionalTherapyWithCurativeIntent(private val labels: EvaluationLabels.Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val locoregionalTherapyHasCurativeIntent: Boolean = record.oncologicalHistory.filter { entry ->
            entry.categories().any { it == TreatmentCategory.RADIOTHERAPY || it == TreatmentCategory.SURGERY }
        }.any { it.intents?.contains(Intent.CURATIVE) == true }

        return if (locoregionalTherapyHasCurativeIntent) {
            EvaluationFactory.pass(labels.hasHadDefinitiveLocoregionalTherapyWithCurativeIntentPass())
        }
        else {
            EvaluationFactory.undetermined(labels.hasHadDefinitiveLocoregionalTherapyWithCurativeIntentUndetermined())
        }

    }
}