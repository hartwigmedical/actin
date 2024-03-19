package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasUnresectablePeritonealMetastases : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {

        val targetTerms = listOf("peritoneum", "peritoneal", "intraperitoneum", "intraperitoneal")
        val hasPeritonealMetastases = record.tumor.otherLesions?.any { lesion ->
            val lowercaseLesion = lesion.lowercase()
            targetTerms.any(lowercaseLesion::startsWith) || targetTerms.any { lowercaseLesion.contains(" $it") }
        }

        return when (hasPeritonealMetastases) {
            null -> {
                EvaluationFactory.undetermined("Missing metastases data")
            }

            true -> {
                EvaluationFactory.warn("Undetermined if peritoneal metastases are unresectable")
            }

            else -> {
                EvaluationFactory.fail("Patient has no unresectable peritoneal metastases", "No unresectable peritoneal metastases")
            }
        }
    }
}