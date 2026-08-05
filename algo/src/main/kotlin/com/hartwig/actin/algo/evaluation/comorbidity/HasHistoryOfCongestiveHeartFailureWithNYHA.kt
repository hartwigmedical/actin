package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.trial.input.datamodel.NyhaClass

class HasHistoryOfCongestiveHeartFailureWithNYHA(
    private val minimalClass: NyhaClass,
    private val icdModel: IcdModel,
    private val labels: EvaluationLabels.Comorbidity
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val allExtensionCodes = listOf(
            IcdConstants.NYHA_CLASS_1_CODE,
            IcdConstants.NYHA_CLASS_2_CODE,
            IcdConstants.NYHA_CLASS_3_CODE,
            IcdConstants.NYHA_CLASS_4_CODE
        )

        val codes = allExtensionCodes.drop(minimalClass.ordinal).map { IcdCode(IcdConstants.CONGESTIVE_HEART_FAILURE_CODE, it) }.toSet()

        val matches = icdModel.findInstancesMatchingAnyIcdCode(record.comorbidities, codes)

        return when {
            matches.fullMatches.isNotEmpty() -> {
                EvaluationFactory.pass(labels.hasHistoryOfCongestiveHeartFailureWithNyhaPass(minimalClass.name))
            }

            matches.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> {
                EvaluationFactory.undetermined(labels.hasHistoryOfCongestiveHeartFailureWithNyhaUndetermined(minimalClass.name))
            }

            else -> {
                EvaluationFactory.fail(labels.hasHistoryOfCongestiveHeartFailureWithNyhaFail(minimalClass.name))
            }
        }
    }
}