package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.icd.IcdModel

class HasSpecificComplication(private val icdModel: IcdModel, private val targetIcdTitles: List<String>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetCodes = targetIcdTitles.map { icdModel.resolveCodeForTitle(it)!! }.toSet()
        val icdMatches = icdModel.findInstancesMatchingAnyIcdCode(record.complications, targetCodes)

        val icdTitleText = if (targetIcdTitles.size > 1) {
            "belonging to type ${Format.concatLowercaseWithCommaAndOr(targetIcdTitles)}"
        } else targetIcdTitles.takeIf { it.isNotEmpty() }?.first()

        return when {
            icdMatches.fullMatches.isNotEmpty() -> EvaluationFactory.pass(
                "Patient has complication(s) " + Format.concatItemsWithAnd(icdMatches.fullMatches),
                "Present " + Format.concatItemsWithAnd(icdMatches.fullMatches)
            )

            icdMatches.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> EvaluationFactory.undetermined(
                "Has complication(s) ${Format.concatItemsWithAnd(icdMatches.mainCodeMatchesWithUnknownExtension)} " +
                        "but undetermined if $icdTitleText"
            )

            hasComplicationsWithoutNames(record) -> EvaluationFactory.undetermined(
                "Patient has complications but type of complications unknown. Undetermined if $icdTitleText",
                "Complications present, unknown if $icdTitleText"
            )

            record.clinicalStatus.hasComplications == null -> EvaluationFactory.undetermined(
                "Undetermined whether patient has cancer-related complications",
                "Undetermined complication status"
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
                    && record.complications.any { ComplicationFunctions.isYesInputComplication(it) }
        }
    }
}