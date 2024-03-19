package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasExperiencedImmuneRelatedAdverseEvents internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasHadImmuneTherapy = record.oncologicalHistory.any { it.categories().contains(TreatmentCategory.IMMUNOTHERAPY) }
        //TODO: Update according to README
        return if (hasHadImmuneTherapy) {
            EvaluationFactory.warn(
                "Patient may have experienced immune related adverse events by immunotherapy treatment",
                "Undetermined previous occurrence of immunotherapy related adverse events"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not experienced immune related adverse events",
                "No experience of immune related adverse events"
            )
        }
    }
}