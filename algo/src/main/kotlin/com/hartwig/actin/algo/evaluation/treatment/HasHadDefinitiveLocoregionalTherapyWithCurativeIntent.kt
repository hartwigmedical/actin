package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

class HasHadDefinitiveLocoregionalTherapyWithCurativeIntent : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentHistory = record.oncologicalHistory
        if (treatmentHistory.isEmpty()) {
            return EvaluationFactory.warn("Patient has no treatment history (we do not always receive the original surgery)")
        }

        val locoregionalTherapyHistory = treatmentHistory.filter { entry ->
            entry.categories().any { it == TreatmentCategory.RADIOTHERAPY || it == TreatmentCategory.SURGERY }
        }
        if (locoregionalTherapyHistory.isEmpty()) {
            return EvaluationFactory.fail("Patient has not had locoregional surgery")
        }

        val locoregionalTherapyIntents = locoregionalTherapyHistory.flatMap { it.intents.orEmpty() }
        if (Intent.CURATIVE in locoregionalTherapyIntents) {
            return EvaluationFactory.pass("Patient has received locoregional therapy with curative intent")
        }

        val locoregionalTherapyIntentsSets = locoregionalTherapyHistory.map { it.intents }
        if (locoregionalTherapyIntentsSets.any{ it.isNullOrEmpty() }) {
            return EvaluationFactory.undetermined("Patient has received locoregional therapy with unknown intent")
        }

        return EvaluationFactory.fail("Patient has received locoregional therapy without curative intent")
    }
}