package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter

class HasRecentlyReceivedCancerTherapyOfCategory(
    private val categoriesToFind: Set<String>,
    private val interpreter: MedicationStatusInterpreter
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val activeMedicationsMatchingCategories = record.clinical().medications()
            .filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }
            .filter { medication -> categoriesToFind.any { stringCaseInsensitivelyMatchesQueryCollection(it, medication.categories()) } }

        val foundCategories = activeMedicationsMatchingCategories.flatMap { it.categories() }.distinct()
        val foundMedicationNames = activeMedicationsMatchingCategories.map { it.name() }.filter { it.isNotEmpty() }.distinct()

        return if (activeMedicationsMatchingCategories.isNotEmpty()) {
            val foundMedicationString = if (foundMedicationNames.isNotEmpty()) ": ${concat(foundMedicationNames)}" else ""
            EvaluationFactory.pass(
                "Patient has recently received medication of category ${concat(foundCategories)}$foundMedicationString",
                "Has recently received medication of category ${concat(foundCategories)}$foundMedicationString"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received recent treatments of category " + concat(categoriesToFind),
                "Has not received recent treatments of category " + concat(categoriesToFind)
            )
        }
    }
}