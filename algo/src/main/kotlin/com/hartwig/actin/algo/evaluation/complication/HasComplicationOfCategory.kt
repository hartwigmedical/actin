package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat

class HasComplicationOfCategory internal constructor(private val categoryToFind: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        if (record.clinical().complications() == null) {
            return undetermined()
        }
        val complicationMatches = ComplicationFunctions.findComplicationNamesMatchingAnyCategory(record, listOf(categoryToFind))
        if (complicationMatches.isNotEmpty()) {
            return if (complicationMatches.size == 1 && complicationMatches.iterator().next().equals(categoryToFind, ignoreCase = true)) {
                EvaluationFactory.pass(
                    "Patient has complication " + concat(complicationMatches),
                    "Present complication(s): " + concat(complicationMatches)
                )
            } else {
                EvaluationFactory.pass(
                    "Patient has complication " + concat(complicationMatches) + " of category " + categoryToFind,
                    "Present complication(s): " + concat(complicationMatches)
                )
            }
        }
        return if (hasComplicationsWithoutCategories(record)) {
            undetermined()
        } else
            EvaluationFactory.fail(
                "Patient does not have complication of category $categoryToFind",
                "Has no complication of category $categoryToFind"
            )
    }

    companion object {
        private fun undetermined(): Evaluation {
            return EvaluationFactory.recoverableUndetermined(
                "Patient has complications but undetermined which category of complications",
                "Complications present, but unknown type"
            )
        }

        private fun hasComplicationsWithoutCategories(record: PatientRecord): Boolean {
            val complications = record.clinical().complications()
            return record.clinical().clinicalStatus().hasComplications() == true && complications != null &&
                    complications.any { ComplicationFunctions.isYesInputComplication(it) }
        }
    }
}