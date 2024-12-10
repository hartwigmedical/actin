package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.Intolerance.IntoleranceFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.icd.IcdModel

class HasIntoleranceRelatedToStudyMedication(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val allergies = record.intolerances
            .filter {
                it.clinicalStatus.equals(CLINICAL_STATUS_ACTIVE, ignoreCase = true)
                        && IntoleranceFunctions.hasIcdMatch(it, IcdConstants.DRUG_ALLERGY_LIST, icdModel)
            }
            .map { it.name }
            .toSet()

        return if (allergies.isNotEmpty()) {
            EvaluationFactory.undetermined(
                "Has medication-related allergies: ${Format.concatWithCommaAndAnd(allergies)} - undetermined if allergy to study medication."
            )
        } else EvaluationFactory.fail("Has no intolerances to study medication")
    }

    companion object {
        const val CLINICAL_STATUS_ACTIVE: String = "Active"
    }
}