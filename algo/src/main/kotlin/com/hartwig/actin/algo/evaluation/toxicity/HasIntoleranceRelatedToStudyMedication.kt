package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat

class HasIntoleranceRelatedToStudyMedication internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val allergies = record.clinical().intolerances()
            .filter { it.clinicalStatus().equals(CLINICAL_STATUS_ACTIVE, ignoreCase = true) }
            .filter { it.doids().contains(DoidConstants.DRUG_ALLERGY_DOID) || it.category().equals(MEDICATION_CATEGORY, ignoreCase = true) }
            .map { it.name() }
            .toSet()

        return if (allergies.isNotEmpty()) {
            unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(
                    "Patient has medication-related allergies: " + concat(allergies) + ". "
                            + "Currently not determined if this could be related to potential study medication"
                )
                .build()
        } else unrecoverable()
            .result(EvaluationResult.FAIL)
            .addFailSpecificMessages("Patient has no known allergies with category 'medication'")
            .build()
    }

    companion object {
        const val MEDICATION_CATEGORY: String = "Medication"
        const val CLINICAL_STATUS_ACTIVE: String = "Active"
    }
}