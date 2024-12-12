package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.icd.IcdModel

class HasSpecificComplication(private val icdModel: IcdModel, private val targetIcdTitles: List<String>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        record.complications ?: return EvaluationFactory.recoverableUndetermined(
            "Undetermined whether patient has cancer-related complications",
            "Undetermined complication status"
        )

        val targetCodes = targetIcdTitles.mapNotNull { icdModel.resolveCodeForTitle(it) }.toSet()
        val matchingComplications =
            ComplicationFunctions.findComplicationsMatchingAnyIcdCode(icdModel, record, targetCodes).fullMatches.map { it.name }

        val icdTitleText = if (targetIcdTitles.size > 1) {
            "belonging to type ${Format.concatLowercaseWithCommaAndOr(targetIcdTitles)}"
        } else targetIcdTitles.takeIf { it.isNotEmpty() }?.first()

        return when {
            matchingComplications.isNotEmpty() -> EvaluationFactory.pass(
                "Patient has complication(s) " + Format.concatWithCommaAndAnd(matchingComplications),
                "Present " + Format.concatWithCommaAndAnd(matchingComplications)
            )

            hasComplicationsWithoutNames(record) -> EvaluationFactory.undetermined(
                "Patient has complications but type of complications unknown. Undetermined if $icdTitleText",
                "Complications present, unknown if $icdTitleText"
            )

            else -> EvaluationFactory.fail(
                "Patient does not have complication $icdTitleText",
                "Complication $icdTitleText not present"
            )
        }
    }

    companion object {
        private fun hasComplicationsWithoutNames(record: PatientRecord): Boolean {
            return record.clinicalStatus.hasComplications == true
                    && record.complications?.any { ComplicationFunctions.isYesInputComplication(it) } ?: false
        }
    }
}