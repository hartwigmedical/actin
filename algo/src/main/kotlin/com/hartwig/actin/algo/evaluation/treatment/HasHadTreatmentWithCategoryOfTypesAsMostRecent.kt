package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadTreatmentWithCategoryOfTypesAsMostRecent(
    private val category: TreatmentCategory, private val types: Set<TreatmentType>?,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorAntiCancerDrugs = record.oncologicalHistory
            .filter { it.categories().any { category -> TreatmentCategory.SYSTEMIC_CANCER_TREATMENT_CATEGORIES.contains(category) } }

        val treatmentMatch = if (types != null) {
            priorAntiCancerDrugs
                .filter { it.matchesTypeFromSet(types) == true }
        } else {
            priorAntiCancerDrugs.filter { it.categories().contains(category) }
        }

        val mostRecentAntiCancerDrug = priorAntiCancerDrugs.maxWithOrNull(TreatmentHistoryEntryStartDateComparator())
        val typeString = types?.let { " ${types.joinToString { it.display() }}"}.orEmpty()

        return when {
            priorAntiCancerDrugs.isEmpty() -> {
                EvaluationFactory.fail(labels.hasHadTreatmentWithCategoryOfTypesAsMostRecentFailNoPriorDrugs())
            }

            types != null && mostRecentAntiCancerDrug?.matchesTypeFromSet(types) == true -> {
                EvaluationFactory.pass(
                    labels.hasHadTreatmentWithCategoryOfTypesAsMostRecentPassWithTypes(typeString, category.display())
                )
            }

            types == null && mostRecentAntiCancerDrug?.categories()?.contains(category) == true -> {
                EvaluationFactory.pass(labels.hasHadTreatmentWithCategoryOfTypesAsMostRecentPass(category.display()))
            }

            treatmentMatch.any { it.startYear == null } -> {
                EvaluationFactory.undetermined(
                    labels.hasHadTreatmentWithCategoryOfTypesAsMostRecentUndetermined(typeString, category.display())
                )
            }

            treatmentMatch.isNotEmpty() -> {
                EvaluationFactory.fail(
                    labels.hasHadTreatmentWithCategoryOfTypesAsMostRecentFailNotMostRecent(typeString, category.display())
                )
            }

            else -> {
                EvaluationFactory.fail(
                    labels.hasHadTreatmentWithCategoryOfTypesAsMostRecentFailNotReceived(typeString, category.display())
                )
            }
        }
    }
}