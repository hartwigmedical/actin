package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.warn
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.othercondition.PriorOtherConditionFunctions
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasIntoleranceForPD1OrPDL1Inhibitors(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val intolerances = record.intolerances.map { it.name }
            .filter { stringCaseInsensitivelyMatchesQueryCollection(it, INTOLERANCE_TERMS) }
            .toSet()

        return if (intolerances.isNotEmpty()) {
            pass(
                "Patient has PD-1/PD-L1 intolerance(s) " + concat(intolerances),
                "Patient has PD-1/PD-L1 intolerance(s): " + concat(intolerances)
            )
        } else {
            val autoImmuneHistory = OtherConditionSelector.selectClinicallyRelevant(
                PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(
                    icdModel,
                    record,
                    IcdConstants.AUTOIMMUNE_DISEASE_SET.map { IcdCode(it) }.toSet()
                ).fullMatches
            ).map { it.name }

            if (autoImmuneHistory.isNotEmpty()) {
                warn(
                    "Patient has autoimmune disease condition(s) " + concat(autoImmuneHistory)
                            + " which may indicate intolerance for immunotherapy",
                    "Patient may have PD-1/PD-L1 intolerance due to autoimmune disease"
                )
            } else {
                fail(
                    "Patient does not have PD-1/PD-L1 intolerance", "Patient does not have PD-1/PD-L1 intolerance"
                )
            }
        }
    }

    companion object {
        val INTOLERANCE_TERMS =
            listOf("Pembrolizumab", "Nivolumab", "Cemiplimab", "Avelumab", "Atezolizumab", "Durvalumab", "PD-1", "PD-L1")
    }
}