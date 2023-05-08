package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter
import com.hartwig.actin.util.ApplicationConfig

class HasRecentlyReceivedCancerTherapyOfCategory internal constructor(
    private val categoriesToFind: Set<String>,
    private val interpreter: MedicationStatusInterpreter
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val activeMedicationCategories = record.clinical().medications()
            .filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }
            .flatMap { it.categories() }
            .map { it.lowercase(ApplicationConfig.LOCALE) }

        val foundCategory = categoriesToFind.find { categoryToFind ->
            val lowercaseCategory = categoryToFind.lowercase(ApplicationConfig.LOCALE)
            activeMedicationCategories.any { it.contains(lowercaseCategory) }
        }

        return if (foundCategory != null) {
            EvaluationFactory.pass(
                "Patient has recently received treatment with medication $foundCategory",
                "Washout period requirements " + concat(categoriesToFind)
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received recent treatments of category " + concat(categoriesToFind),
                "Washout period requirements " + concat(categoriesToFind)
            )
        }
    }
}