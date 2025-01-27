package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.icd.IcdModel

class HasIntoleranceRelatedToStudyMedication(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetIcdCodes = IcdConstants.DRUG_ALLERGY_SET.map { IcdCode(it) }
        val allergies = icdModel.findInstancesMatchingAnyIcdCode(record.comorbidities, targetIcdCodes).fullMatches
            .filter { (it as? Intolerance)?.clinicalStatus?.equals(CLINICAL_STATUS_ACTIVE, ignoreCase = true) != false }
            .toSet()

        return if (allergies.isNotEmpty()) {
            EvaluationFactory.undetermined(
                "Has medication-related allergies (${Format.concatItemsWithAnd(allergies)}) - undetermined if allergy to study medication"
            )
        } else EvaluationFactory.fail("Has no intolerances to study medication")
    }

    companion object {
        const val CLINICAL_STATUS_ACTIVE: String = "Active"
    }
}