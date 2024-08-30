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

        val foundCategories = mutableSetOf<String>()
        categoriesToFind.filter { categoryToFind ->
            medications
                .filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }
                .any {
                    if ((it.allLevels() intersect categoryToFind.value).isNotEmpty()) {
                        foundCategories.add(categoryToFind.key)
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
                    if (it == TreatmentCategory) {
                        val hasCategory = treatmentHistoryEntry.categories().contains(it)
                        if (hasCategory) foundCategories.add(categoryToDrugType.key)
                        hasCategory
                    } else {
                        val hasCategory = treatmentHistoryEntry.categories().contains((it as DrugType).category)
                                && treatmentHistoryEntry.matchesTypeFromSet(setOf(it)) == true
                        if (hasCategory) foundCategories.add(categoryToDrugType.key)
                        hasCategory
                    }
                }
            }
            TreatmentFunctions.TreatmentAssessment(
                hasHadValidTreatment = categoryAndTypeMatch && startedPastMinDate == true,
                hasInconclusiveDate = categoryAndTypeMatch && startedPastMinDate == null,
                hasHadTrialAfterMinDate = drugTypesToFind.any {
                    val category = if (it == TreatmentCategory) it as TreatmentCategory else (it as DrugType).category
                    val hasTrial = TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, category) && startedPastMinDate == true
                    if (hasTrial) foundCategories.add("Trial medication")
                    hasTrial
                }
            )
        }.fold(TreatmentFunctions.TreatmentAssessment()) { acc, element -> acc.combineWith(element) }

        return when {
            foundCategories.isNotEmpty() || treatmentAssessment.hasHadValidTreatment -> {
                EvaluationFactory.pass(
                    "Patient has recently received medication of category ${concatLowercaseWithAnd(foundCategories)}" +
                            " - pay attention to washout period",
                    "Recent '${concatLowercaseWithAnd(foundCategories)}' medication use" +
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