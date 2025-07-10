package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasSpecificComplication(private val icdModel: IcdModel, private val targetIcdCodes: Set<IcdCode>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetIcdTitles = targetIcdCodes.map { icdModel.resolveTitleForCode(it) }.toSet()
        val icdTitleText = if (targetIcdTitles.size > 1) {
            "belonging to type ${Format.concatLowercaseWithCommaAndOr(targetIcdTitles)}"
        } else targetIcdTitles.takeIf { it.isNotEmpty() }?.first()

        val icdMatches = icdModel.findInstancesMatchingAnyIcdCode(record.complications, targetIcdCodes)

        return when {
            icdMatches.fullMatches.isNotEmpty() -> EvaluationFactory.pass(
                "Has complication(s) " + Format.concatItemsWithAnd(icdMatches.fullMatches, true)
            )

            icdMatches.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> EvaluationFactory.undetermined(
                "Has complication(s) ${Format.concatItemsWithAnd(icdMatches.mainCodeMatchesWithUnknownExtension, true)} " +
                        "but undetermined if $icdTitleText"
            )

            hasComplicationsWithoutNames(record) -> EvaluationFactory.undetermined("Complication(s) present but unknown if $icdTitleText")

            record.clinicalStatus.hasComplications == null -> EvaluationFactory.recoverableUndetermined(
                "Undetermined presence of complication $icdTitleText (complication data missing)"
            )

            else -> EvaluationFactory.fail("Complication(s) $icdTitleText not present")
        }
    }

    companion object {
        private fun hasComplicationsWithoutNames(record: PatientRecord): Boolean {
            return record.clinicalStatus.hasComplications == true
                    && record.complications.any { ComplicationFunctions.isYesInputComplication(it) }
        }
    }
}