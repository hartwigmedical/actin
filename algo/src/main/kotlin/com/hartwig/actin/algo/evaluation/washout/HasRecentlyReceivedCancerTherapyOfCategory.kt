package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MEDICATION_NOT_PROVIDED
import com.hartwig.actin.algo.evaluation.treatment.TreatmentFunctions
import com.hartwig.actin.algo.evaluation.treatment.TrialFunctions
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.medication.MedicationCategories
import java.time.LocalDate

class HasRecentlyReceivedCancerTherapyOfCategory(
    private val categories: Map<String, Set<AtcLevel>>,
    private val categoriesToIgnore: Map<String, Set<AtcLevel>>,
    private val interpreter: MedicationStatusInterpreter,
    private val minDate: LocalDate
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val atcLevelsToFind: Set<AtcLevel> = categories.values.flatten().toSet() - categoriesToIgnore.values.flatten().toSet()
        val categoryNames: Set<String> = categories.keys

        val activeMedicationsMatchingCategories = medications
            .filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }
            .filter { (it.allLevels() intersect atcLevelsToFind).isNotEmpty() || it.isTrialMedication }

        val categoryWithoutIgnore = categories.keys - categoriesToIgnore.keys
        val category = categoryWithoutIgnore.flatMap { MedicationCategories.MEDICATION_CATEGORIES_TO_DRUG_TYPES[it] ?: emptySet() }.toSet()

        val treatmentAssessment = record.oncologicalHistory.map { treatmentHistoryEntry ->
            val startedPastMinDate = DateComparison.isAfterDate(minDate, treatmentHistoryEntry.startYear, treatmentHistoryEntry.startMonth)
            val categoryAndTypeMatch = category.any {
                if (it == TreatmentCategory) {
                    treatmentHistoryEntry.categories().contains(it)
                } else {
                    treatmentHistoryEntry.categories().contains((it as DrugType).category)
                            && treatmentHistoryEntry.matchesTypeFromSet(setOf(it)) == true
                }
            }
            TreatmentFunctions.TreatmentAssessment(
                hasHadValidTreatment = categoryAndTypeMatch && startedPastMinDate == true,
                hasInconclusiveDate = categoryAndTypeMatch && startedPastMinDate == null,
                hasHadTrialAfterMinDate = category.any { if (it == TreatmentCategory) {TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, it as TreatmentCategory) && startedPastMinDate == true} else {TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, (it as DrugType).category) && startedPastMinDate == true} }
            )
        }.fold(TreatmentFunctions.TreatmentAssessment()) { acc, element -> acc.combineWith(element) }

        val foundCategories = activeMedicationsMatchingCategories.map { medication ->
            if (medication.isTrialMedication) "Trial medication" else medication.atc!!.pharmacologicalSubGroup.name.lowercase()
        }

        val foundMedicationNames = activeMedicationsMatchingCategories.map { it.name }.filter { it.isNotEmpty() }
        val foundMedicationString =
            if (foundMedicationNames.isNotEmpty()) ": ${concatLowercaseWithAnd(foundMedicationNames)}" else ""

        return when {
            activeMedicationsMatchingCategories.isNotEmpty() || treatmentAssessment.hasHadValidTreatment -> {
                EvaluationFactory.pass(
                    "Patient has recently received medication of category '${concatLowercaseWithAnd(foundCategories)}'$foundMedicationString" +
                            " - pay attention to washout period",
                    "Recent '${concatLowercaseWithAnd(foundCategories)}' medication use$foundMedicationString" +
                            " - pay attention to washout period"
                )
            }

            treatmentAssessment.hasInconclusiveDate -> {
                EvaluationFactory.undetermined("Has received ${concatLowercaseWithAnd(categoryNames)} treatment but inconclusive date")
            }

            treatmentAssessment.hasHadTrialAfterMinDate -> {
                EvaluationFactory.undetermined(
                    "Patient has participated in a trial recently, inconclusive ${concatLowercaseWithAnd(categoryNames)} treatment",
                    "Inconclusive ${concatLowercaseWithAnd(categoryNames)} treatment due to trial participation"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not received recent treatments of category '${concatLowercaseWithAnd(categoryNames)}'",
                    "No recent '${concatLowercaseWithAnd(categoryNames)}' medication use"
                )
            }
        }
    }
}