package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MEDICATION_NOT_PROVIDED
import com.hartwig.actin.algo.evaluation.treatment.TreatmentFunctions
import com.hartwig.actin.algo.evaluation.treatment.TrialFunctions
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
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
        val categoryNames: Set<String> = categories.keys - categoriesToIgnore.keys
        val categoriesToFind = categories.mapValues { (key, value) -> value - (categoriesToIgnore[key] ?: emptySet()).toSet() }
            .filterValues { it.isNotEmpty() }

        val activeMedications = medications.filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }

        val foundCategories = mutableSetOf<String>()
        val foundMedicationNames = mutableSetOf<String>()
        categoriesToFind.filter { categoryToFind ->
            activeMedications
                .any {
                    if ((it.allLevels() intersect categoryToFind.value).isNotEmpty()) {
                        foundCategories.add(categoryToFind.key)
                        foundMedicationNames.add(it.drug?.name ?: it.name)
                    }
                    if (it.isTrialMedication) {
                        foundCategories.add("Trial medication")
                    }
                    (it.allLevels() intersect categoryToFind.value).isNotEmpty() || it.isTrialMedication
                }
        }.map { it.key }.toSet()

        val categoryToDrugTypes = MedicationCategories.MEDICATION_CATEGORIES_TO_DRUG_TYPES.filter { categoryNames.contains(it.key) }
        val drugTypesToFind = categoryNames.flatMap { MedicationCategories.MEDICATION_CATEGORIES_TO_DRUG_TYPES[it] ?: emptySet() }.toSet()

        val treatmentAssessment = record.oncologicalHistory.map { treatmentHistoryEntry ->
            val startedPastMinDate = DateComparison.isAfterDate(minDate, treatmentHistoryEntry.startYear, treatmentHistoryEntry.startMonth)
            val categoryAndTypeMatch = categoryToDrugTypes.any { categoryToDrugType ->
                categoryToDrugType.value.any {
                    if (it is TreatmentCategory) {
                        val hasCategory = treatmentHistoryEntry.categories().contains(it)
                        if (hasCategory && startedPastMinDate == true) {
                            foundCategories.add(categoryToDrugType.key)
                            treatmentHistoryEntry.treatments.forEach { treatment ->
                                if (treatment is DrugTreatment) {
                                    treatment.drugs.forEach { drug -> foundMedicationNames.add(drug.name) }
                                }
                            }
                        }
                        hasCategory
                    } else {
                        val hasCategory = treatmentHistoryEntry.categories().contains((it as DrugType).category)
                                && treatmentHistoryEntry.matchesTypeFromSet(setOf(it)) == true
                        if (hasCategory && startedPastMinDate == true) {
                            foundCategories.add(categoryToDrugType.key)
                            treatmentHistoryEntry.treatments.forEach { treatment ->
                                if (treatment is DrugTreatment) {
                                    treatment.drugs.forEach { drug -> foundMedicationNames.add(drug.name) }
                                }
                            }
                        }
                        hasCategory
                    }
                }
            }
            TreatmentFunctions.TreatmentAssessment(
                hasHadValidTreatment = categoryAndTypeMatch && startedPastMinDate == true,
                hasInconclusiveDate = categoryAndTypeMatch && startedPastMinDate == null,
                hasHadTrialAfterMinDate = drugTypesToFind.any {
                    val category = if (it is TreatmentCategory) it else (it as DrugType).category
                    TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, category)
                }
            )
        }.fold(TreatmentFunctions.TreatmentAssessment()) { acc, element -> acc.combineWith(element) }

        val foundMedicationString =
            if (foundMedicationNames.isNotEmpty()) ": ${concatLowercaseWithAnd(foundMedicationNames)}" else ""
        return when {
            foundCategories.isNotEmpty() || treatmentAssessment.hasHadValidTreatment -> {
                EvaluationFactory.pass(
                    "Patient has recently received drug of category '${concatLowercaseWithAnd(foundCategories)}'$foundMedicationString" +
                            " - pay attention to washout period",
                    "Recent '${concatLowercaseWithAnd(foundCategories)}' drug use$foundMedicationString" +
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