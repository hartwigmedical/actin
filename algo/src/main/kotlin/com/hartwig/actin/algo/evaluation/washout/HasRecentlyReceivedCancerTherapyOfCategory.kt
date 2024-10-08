package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MEDICATION_NOT_PROVIDED
import com.hartwig.actin.algo.evaluation.treatment.TrialFunctions
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
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

        val categoriesByLevel = categoriesToFind.flatMap { (name, levels) -> levels.map { it to name } }
            .groupBy({ it.first }, { it.second })
        val (foundMedicationCategories, foundMedicationNames) = activeMedications.flatMap { medication ->
            medication.allLevels().mapNotNull {
                categoriesByLevel[it]?.let { category -> category to (medication.drug?.name ?: medication.name) }
            }
        }.unzip()
        val foundTrialMedication = activeMedications.any(Medication::isTrialMedication)

        val categoryToDrugTypes = MedicationCategories.MEDICATION_CATEGORIES_TO_DRUG_TYPES.filter { categoryNames.contains(it.key) }
        val drugTypesToFind = categoryNames.flatMap { MedicationCategories.MEDICATION_CATEGORIES_TO_DRUG_TYPES[it] ?: emptySet() }.toSet()

        val categoryToTreatmentCategories =
            MedicationCategories.MEDICATION_CATEGORIES_TO_TREATMENT_CATEGORY.filter { categoryNames.contains(it.key) }
        val treatmentCategoriesToFind =
            categoryNames.flatMap { MedicationCategories.MEDICATION_CATEGORIES_TO_TREATMENT_CATEGORY[it] ?: emptySet() }.toSet()

        val treatmentAssessment = record.oncologicalHistory.map { treatmentHistoryEntry ->
            val startedPastMinDate = DateComparison.isAfterDate(minDate, treatmentHistoryEntry.startYear, treatmentHistoryEntry.startMonth)

            val matchingCategories = treatmentHistoryEntry.categories().intersect(treatmentCategoriesToFind)
            val matchingTypes = treatmentHistoryEntry.allTreatments().flatMap(Treatment::types).toSet().intersect(drugTypesToFind)
            val isMatch = matchingCategories.isNotEmpty() || matchingTypes.isNotEmpty()

            TreatmentAssessmentExtended(
                hasHadValidTreatment = isMatch && startedPastMinDate == true,
                hasInconclusiveDate = isMatch && startedPastMinDate == null,
                hasHadTrialAfterMinDate = drugTypesToFind.any {
                    TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, it.category) && startedPastMinDate == true
                } ||
                        treatmentCategoriesToFind.any {
                            TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, it) && startedPastMinDate == true
                        },
                matchingCategories = if (!isMatch && startedPastMinDate != true) emptySet() else {
                    categoryToDrugTypes.filter { (_, drugTypes) ->
                        treatmentHistoryEntry.allTreatments().flatMap(Treatment::types).toSet().intersect(drugTypes).isNotEmpty()
                    }.keys + categoryToTreatmentCategories.filter { (_, treatmentCategories) ->
                        treatmentHistoryEntry.categories().intersect(treatmentCategories).isNotEmpty()
                    }.keys
                },
                matchingDrugs = if (!isMatch || startedPastMinDate != true) emptySet() else {
                    treatmentHistoryEntry.treatments.filterIsInstance<DrugTreatment>()
                        .flatMap { treatment -> treatment.drugs.filter { it.category in treatmentCategoriesToFind || it.drugTypes.any { drugType -> drugType in drugTypesToFind } } }
                        .map(Drug::name)
                        .toSet()
                }
            )
        }.fold(TreatmentAssessmentExtended()) { acc, element -> acc.combineWith(element) }

        val foundDrugNames = foundMedicationNames + treatmentAssessment.matchingDrugs
        val foundMedicationString = if (foundDrugNames.isNotEmpty()) ": ${concatLowercaseWithAnd(foundDrugNames)}" else ""

        val foundCategories = foundMedicationCategories.flatten().toSet() + treatmentAssessment.matchingCategories

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

            treatmentAssessment.hasHadTrialAfterMinDate || foundTrialMedication -> {
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

    data class TreatmentAssessmentExtended(
        val hasHadValidTreatment: Boolean = false,
        val hasInconclusiveDate: Boolean = false,
        val hasHadTrialAfterMinDate: Boolean = false,
        val matchingCategories: Set<String> = emptySet(),
        val matchingDrugs: Set<String> = emptySet()
    ) {

        fun combineWith(other: TreatmentAssessmentExtended): TreatmentAssessmentExtended {
            return TreatmentAssessmentExtended(
                hasHadValidTreatment || other.hasHadValidTreatment,
                hasInconclusiveDate || other.hasInconclusiveDate,
                hasHadTrialAfterMinDate || other.hasHadTrialAfterMinDate,
                matchingDrugs + other.matchingDrugs,
                matchingCategories + other.matchingCategories
            )
        }
    }
}