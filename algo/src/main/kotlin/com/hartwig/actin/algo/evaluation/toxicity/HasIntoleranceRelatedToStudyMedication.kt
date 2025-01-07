package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasIntoleranceRelatedToStudyMedication : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val allergies = record.intolerances
            .filter { it.clinicalStatus.equals(CLINICAL_STATUS_ACTIVE, ignoreCase = true) }
            .filter { it.doids.contains(DoidConstants.DRUG_ALLERGY_DOID) || it.category.equals(MEDICATION_CATEGORY, ignoreCase = true) }
            .map { it.name }
            .toSet()

        return if (allergies.isNotEmpty()) {
            EvaluationFactory.undetermined(
                "Medication-related allergies (${Format.concatLowercaseWithCommaAndAnd(allergies)})"
                        + " - undetermined if this could be related to potential study medication"
            )
        } else EvaluationFactory.fail("Patient has no known allergies with category 'medication'")
    }

    companion object {
        const val MEDICATION_CATEGORY: String = "Medication"
        const val CLINICAL_STATUS_ACTIVE: String = "Active"
    }
}