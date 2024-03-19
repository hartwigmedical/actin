package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter

class HasRecentlyReceivedCancerTherapyOfCategory(
    private val categories: Map<String, Set<AtcLevel>>,
    private val categoriesToIgnore: Map<String, Set<AtcLevel>>,
    private val interpreter: MedicationStatusInterpreter
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val atcLevelsToFind: Set<AtcLevel> = categories.values.flatten().toSet() - categoriesToIgnore.values.flatten().toSet()
        val categoryNames: Set<String> = categories.keys

        val activeMedicationsMatchingCategories = record.medications
            .filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }
            .filter { (it.allLevels() intersect atcLevelsToFind).isNotEmpty() || it.isTrialMedication }

        val foundCategories = activeMedicationsMatchingCategories.map { medication ->
            if (medication.isTrialMedication) "Trial medication" else medication.atc!!.pharmacologicalSubGroup.name.lowercase()
        }

        val foundMedicationNames = activeMedicationsMatchingCategories.map { it.name }.filter { it.isNotEmpty() }

        return if (activeMedicationsMatchingCategories.isNotEmpty()) {
            val foundMedicationString = if (foundMedicationNames.isNotEmpty()) ": ${concatLowercaseWithAnd(foundMedicationNames)}" else ""
            EvaluationFactory.pass(
                "Patient has recently received medication of category '${concatLowercaseWithAnd(foundCategories)}'$foundMedicationString" +
                        " - pay attention to washout period",
                "Recent '${concatLowercaseWithAnd(foundCategories)}' medication use$foundMedicationString" +
                        " - pay attention to washout period"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received recent treatments of category '${concatLowercaseWithAnd(categoryNames)}'",
                "No recent '${concatLowercaseWithAnd(categoryNames)}' medication use"
            )
        }
    }
}