package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter


class HasRecentlyReceivedCancerTherapyOfCategory(
    private val categories: Map<String, Set<String>>,
    private val interpreter: MedicationStatusInterpreter
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val categoriesToFind: Set<String> = categories.values.flatten().toSet()
        val categoryName: Set<String> = categories.keys
        val activeMedicationsMatchingCategories = record.clinical().medications()
            .filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }
            .filter { medication ->
                MedicationSelector(interpreter).hasATCLevelName(medication, categoriesToFind) || medication.isTrialMedication
            }

        val foundCategories = ArrayList<String>()
        for (medication in activeMedicationsMatchingCategories) {
            if (medication.isTrialMedication) {
                foundCategories.add("Trial medication")
            } else {
                foundCategories.add(medication.atc()!!.pharmacologicalSubGroup().name()!!.lowercase())
            }
        }
        val foundDistinctCategories = foundCategories.distinct()

        val foundMedicationNames = activeMedicationsMatchingCategories.map { it.name() }.filter { it.isNotEmpty() }.distinct()

        return if (activeMedicationsMatchingCategories.isNotEmpty()) {
            val foundMedicationString = if (foundMedicationNames.isNotEmpty()) ": ${concatLowercaseWithAnd(foundMedicationNames)}" else ""
            EvaluationFactory.pass(
                "Patient has recently received medication of category ${concatLowercaseWithAnd(foundDistinctCategories)}$foundMedicationString",
                "Has recently received medication of category ${concatLowercaseWithAnd(foundDistinctCategories)}$foundMedicationString"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received recent treatments of category " + concatLowercaseWithAnd(categoryName),
                "Has not received recent treatments of category " + concatLowercaseWithAnd(categoryName)
            )
        }
    }
}