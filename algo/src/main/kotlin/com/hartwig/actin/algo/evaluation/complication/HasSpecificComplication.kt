package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.datamodel.IcdMatches

class HasSpecificComplication(private val icdModel: IcdModel, private val targetIcdTitles: List<String>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        record.complications ?: return EvaluationFactory.recoverableUndetermined(
            "Undetermined whether patient has cancer-related complications"
        )

        val targetCodes = targetIcdTitles.map { icdModel.resolveCodeForTitle(it)!! }.toSet()
        val icdMatches =
            record.complications?.let { icdModel.findInstancesMatchingAnyIcdCode(it, targetCodes) } ?: IcdMatches(emptyList(), emptyList())

        val icdTitleText = if (targetIcdTitles.size > 1) {
            "belonging to type ${Format.concatLowercaseWithCommaAndOr(targetIcdTitles)}"
        } else targetIcdTitles.takeIf { it.isNotEmpty() }?.first()

        return when {
            icdMatches.fullMatches.isNotEmpty() -> EvaluationFactory.pass(
                "Has complication(s) " + Format.concatItemsWithAnd(icdMatches.fullMatches)
            )

            icdMatches.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> EvaluationFactory.undetermined(
                "Has complication(s) ${Format.concatItemsWithAnd(icdMatches.mainCodeMatchesWithUnknownExtension)} " +
                        "but undetermined if $icdTitleText"
            )

            hasComplicationsWithoutNames(record) -> EvaluationFactory.undetermined("Complication(s) present but unknown if $icdTitleText")

            else -> EvaluationFactory.fail("Complication(s) $icdTitleText not present")
        }
    }

    companion object {
        private fun hasComplicationsWithoutNames(record: PatientRecord): Boolean {
            return record.clinicalStatus.hasComplications == true
                    && record.complications?.any { ComplicationFunctions.isYesInputComplication(it) } ?: false
        }
    }
}