package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MEDICATION_NOT_PROVIDED
import com.hartwig.actin.algo.evaluation.treatment.TrialFunctions
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseUnlessNumericWithAnd
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.medication.MedicationCategories
import java.time.LocalDate

data class TreatmentAssessmentExtended(
    val hasHadValidTreatment: Boolean = false,
    val hasInconclusiveDate: Boolean = false,
    val hasHadTrialAfterMinDate: Boolean = false,
    val matchingMedicationCategories: Set<String> = emptySet(),
    val matchingDrugs: Set<String> = emptySet()
) {

    fun combineWith(other: TreatmentAssessmentExtended): TreatmentAssessmentExtended {
        return TreatmentAssessmentExtended(
            hasHadValidTreatment || other.hasHadValidTreatment,
            hasInconclusiveDate || other.hasInconclusiveDate,
            hasHadTrialAfterMinDate || other.hasHadTrialAfterMinDate,
            matchingMedicationCategories + other.matchingMedicationCategories,
            matchingDrugs + other.matchingDrugs
        )
    }
}

class HasRecentlyReceivedCancerTherapyOfCategory(
    private val categories: Map<String, Set<AtcLevel>>,
    private val categoriesToIgnore: Map<String, Set<AtcLevel>>,
    private val interpreter: MedicationStatusInterpreter,
    private val minDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val categoryNames: Set<String> = categories.keys - categoriesToIgnore.keys
        val treatmentAssessment = assessTreatmentHistory(record, categoryNames)
        val matchingTreatmentFound =
            treatmentAssessment.hasHadValidTreatment || treatmentAssessment.hasInconclusiveDate || treatmentAssessment.hasHadTrialAfterMinDate

        if (!matchingTreatmentFound && record.medications == null) {
            return MEDICATION_NOT_PROVIDED
        }

        val activeMedications =
            record.medications?.filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE } ?: emptyList()
        val (foundMedicationCategories, foundMedicationNames) = findMatchingMedications(activeMedications)
        val foundTrialMedication = activeMedications.any(Medication::isTrialMedication)

        val foundDrugNames = foundMedicationNames + treatmentAssessment.matchingDrugs
        val foundMedicationString = if (foundDrugNames.isNotEmpty()) ": ${concatLowercaseUnlessNumericWithAnd(foundDrugNames)}" else ""
        val foundCategories = foundMedicationCategories.toSet() + treatmentAssessment.matchingMedicationCategories

        return when {
            foundCategories.isNotEmpty() || treatmentAssessment.hasHadValidTreatment -> {
                EvaluationFactory.pass(
                    "Recent '${concatLowercaseWithAnd(foundCategories)}' drug use$foundMedicationString" +
                            " - pay attention to washout period"
                )
            }

            treatmentAssessment.hasInconclusiveDate -> {
                EvaluationFactory.undetermined("Has received ${concatLowercaseWithAnd(categoryNames)} treatment but inconclusive date")
            }

            treatmentAssessment.hasHadTrialAfterMinDate || foundTrialMedication -> {
                EvaluationFactory.undetermined("Undetermined ${concatLowercaseWithAnd(categoryNames)} treatment due to trial participation")
            }

            else -> {
                EvaluationFactory.fail("No recent '${concatLowercaseWithAnd(categoryNames)}' drug use")
            }
        }
    }

    private fun findMatchingMedications(activeMedications: List<Medication>): Pair<List<String>, List<String>> {
        val atcLevelsToIgnore = categoriesToIgnore.values.fold(emptySet<AtcLevel>()) { acc, curr -> acc + curr }
        val categoriesToFind = categories.mapValues { (_, atcLevels) -> atcLevels - atcLevelsToIgnore }
        val categoriesByLevel = categoriesToFind.flatMap { (name, levels) -> levels.map { it to name } }
            .groupBy({ it.first }, { it.second })
        val (foundMedicationCategories, foundMedicationNames) = activeMedications.flatMap { medication ->
            medication.allLevels().flatMap {
                categoriesByLevel[it]?.map { category -> category to (medication.drug?.name ?: medication.name) } ?: emptyList()
            }
        }.unzip()
        return Pair(foundMedicationCategories, foundMedicationNames)
    }

    private fun assessTreatmentHistory(record: PatientRecord, categoryNames: Set<String>): TreatmentAssessmentExtended {
        val categoryToDrugTypes = MedicationCategories.MEDICATION_CATEGORIES_TO_DRUG_TYPES.filter { categoryNames.contains(it.key) }
        val drugTypesToFind = categoryToDrugTypes.flatMap { it.value }.toSet()

        val categoryToTreatmentCategories =
            MedicationCategories.MEDICATION_CATEGORIES_TO_TREATMENT_CATEGORY.filter { categoryNames.contains(it.key) }
        val treatmentCategoriesToFind = categoryToTreatmentCategories.flatMap { it.value }.toSet()

        return record.oncologicalHistory.map { treatmentHistoryEntry ->
            val startedPastMinDate = DateComparison.isAfterDate(minDate, treatmentHistoryEntry.startYear, treatmentHistoryEntry.startMonth)

            val matchingCategories = treatmentHistoryEntry.categories().intersect(treatmentCategoriesToFind)
            val matchingTypes = treatmentHistoryEntry.allTreatments().flatMap(Treatment::types).toSet().intersect(drugTypesToFind)
            val isMatch = matchingCategories.isNotEmpty() || matchingTypes.isNotEmpty()

            TreatmentAssessmentExtended(
                hasHadValidTreatment = isMatch && startedPastMinDate == true,
                hasInconclusiveDate = isMatch && startedPastMinDate == null,
                hasHadTrialAfterMinDate = startedPastMinDate == true &&
                        (drugTypesToFind.map(DrugType::category) + treatmentCategoriesToFind).any {
                            TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, it)
                        },
                matchingMedicationCategories = if (!isMatch || startedPastMinDate != true) emptySet() else {
                    val matchingBasedOnDrugType =
                        categoryToDrugTypes.filter { (_, drugTypes) -> matchingTypes.intersect(drugTypes).isNotEmpty() }.keys
                    val matchingBasedOnTreatmentCategory = categoryToTreatmentCategories.filter { (_, treatmentCategories) ->
                        matchingCategories.intersect(treatmentCategories).isNotEmpty()
                    }.keys
                    matchingBasedOnDrugType + matchingBasedOnTreatmentCategory
                },
                matchingDrugs = if (!isMatch || startedPastMinDate != true) emptySet() else {
                    treatmentHistoryEntry.allTreatments().filterIsInstance<DrugTreatment>()
                        .flatMap { treatment ->
                            treatment.drugs.filter {
                                it.category in treatmentCategoriesToFind || it.drugTypes.any(
                                    drugTypesToFind::contains
                                )
                            }
                        }
                        .map(Drug::name)
                        .toSet()
                }
            )
        }.fold(TreatmentAssessmentExtended()) { acc, element -> acc.combineWith(element) }
    }
}