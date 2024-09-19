package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MEDICATION_NOT_PROVIDED
import com.hartwig.actin.algo.evaluation.treatment.TreatmentAssessment
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
        val categoriesToFind = categories.mapValues { (key, atcLevels) -> atcLevels - (categoriesToIgnore[key] ?: emptySet()) }
            .filterValues { it.isNotEmpty() }

        val activeMedications = medications.filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }

        val foundCategories = mutableSetOf<String>()
        val foundMedicationNames = mutableSetOf<String>()
        activeMedications.forEach { medication ->
            categoriesToFind.forEach { (categoryName, levels) ->
                if (medication.allLevels().intersect(levels).isNotEmpty()) {
                    foundCategories.add(categoryName)
                    foundMedicationNames.add(medication.drug?.name ?: medication.name)
                }
            }
            if (medication.isTrialMedication) {
                foundCategories.add("Trial medication")
            }
        }

        val categoryToDrugTypes = MedicationCategories.MEDICATION_CATEGORIES_TO_DRUG_TYPES.filter { categoryNames.contains(it.key) }
        val drugTypesToFind = categoryNames.flatMap { MedicationCategories.MEDICATION_CATEGORIES_TO_DRUG_TYPES[it] ?: emptySet() }.toSet()

        val treatmentAssessment = record.oncologicalHistory.map { treatmentHistoryEntry ->
            val startedPastMinDate = DateComparison.isAfterDate(minDate, treatmentHistoryEntry.startYear, treatmentHistoryEntry.startMonth)
            val categoryAndTypeMatch = categoryToDrugTypes.any { (categoryName, drugTypes) ->
                drugTypes.any { drugType ->
                    val hasCategory = when (drugType) {
                        is TreatmentCategory -> treatmentHistoryEntry.categories().contains(drugType)
                        is DrugType -> treatmentHistoryEntry.categories()
                            .contains(drugType.category) && treatmentHistoryEntry.matchesTypeFromSet(setOf(drugType)) == true

                        else -> false
                    }
                    if (hasCategory && startedPastMinDate == true) {
                        foundCategories.add(categoryName)
                        treatmentHistoryEntry.treatments.filterIsInstance<DrugTreatment>().forEach { treatment ->
                            treatment.drugs.forEach { drug -> foundMedicationNames.add(drug.name) }
                        }
                    }
                    hasCategory
                }
            }
            TreatmentAssessment(
                hasHadValidTreatment = categoryAndTypeMatch && startedPastMinDate == true,
                hasInconclusiveDate = categoryAndTypeMatch && startedPastMinDate == null,
                hasHadTrialAfterMinDate = drugTypesToFind.any {
                    val category = if (it is TreatmentCategory) it else (it as DrugType).category
                    TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, category) && startedPastMinDate == true
                }
            )
        }.fold(TreatmentAssessment()) { acc, element -> acc.combineWith(element) }

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