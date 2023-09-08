package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.Medication

class HasRecentlyReceivedCancerTherapyOfCategory(
    private val categories: Map<String, Set<AtcLevel>>,
    private val categoriesToIgnore: Map<String, Set<AtcLevel>>,
    private val interpreter: MedicationStatusInterpreter
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val categoriesToFind: Set<AtcLevel> = categories.values.flatten().toSet() - categoriesToIgnore.values.flatten().toSet()
        val categoryName: Set<String> = categories.keys

        val activeMedicationsMatchingCategories = record.clinical().medications()
            .filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }
            .filter { (allLevels(it) intersect categoriesToFind).isNotEmpty() || it.isTrialMedication }

        val foundCategories = activeMedicationsMatchingCategories.map { medication ->
            if (medication.isTrialMedication) "Trial medication" else medication.atc()!!.pharmacologicalSubGroup().name()!!.lowercase()
        }

        val foundMedicationNames = activeMedicationsMatchingCategories.map { it.name() }.filter { it.isNotEmpty() }

        return if (activeMedicationsMatchingCategories.isNotEmpty()) {
            val foundMedicationString = if (foundMedicationNames.isNotEmpty()) ": ${concatLowercaseWithAnd(foundMedicationNames)}" else ""
            EvaluationFactory.pass(
                "Patient has recently received medication of category ${concatLowercaseWithAnd(foundCategories)}$foundMedicationString",
                "Has recently received medication of category ${concatLowercaseWithAnd(foundCategories)}$foundMedicationString"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received recent treatments of category " + concatLowercaseWithAnd(categoryName),
                "Has not received recent treatments of category " + concatLowercaseWithAnd(categoryName)
            )
        }
    }

    private fun allLevels(it: Medication) = it.atc()?.allLevels() ?: emptySet<AtcLevel>()
}