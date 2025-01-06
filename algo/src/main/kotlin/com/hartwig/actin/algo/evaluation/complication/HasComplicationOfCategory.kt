package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasComplicationOfCategory(private val categoryToFind: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        if (record.complications == null) {
            return undetermined()
        }
        val complicationMatches = ComplicationFunctions.findComplicationNamesMatchingAnyCategory(record, listOf(categoryToFind))
        if (complicationMatches.isNotEmpty()) {
            return if (complicationMatches.size == 1 && complicationMatches.iterator().next().equals(categoryToFind, ignoreCase = true)) {
                EvaluationFactory.pass("Present complication(s): " + concatLowercaseWithAnd(complicationMatches))
            } else {
                EvaluationFactory.pass("Present complication(s): " + concatLowercaseWithAnd(complicationMatches) + " of category " + categoryToFind)
            }
        }
        return if (hasComplicationsWithoutCategories(record)) {
            undetermined()
        } else
            EvaluationFactory.fail("Has no complication of category $categoryToFind")
    }

    companion object {
        private fun undetermined(): Evaluation {
            return EvaluationFactory.recoverableUndetermined("Complications of unknown type present")
        }

        private fun hasComplicationsWithoutCategories(record: PatientRecord): Boolean {
            val complications = record.complications
            return record.clinicalStatus.hasComplications == true && complications != null &&
                    complications.any { ComplicationFunctions.isYesInputComplication(it) }
        }
    }
}