package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadTreatmentWithCategoryOfTypesAsMostRecent(
    private val category: TreatmentCategory, private val types: Set<TreatmentType>?
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
                EvaluationFactory.fail("Has not received prior anti cancer drugs")
            }

            types != null && mostRecentAntiCancerDrug?.matchesTypeFromSet(types) == true -> {
                EvaluationFactory.pass("Has received$typeString ${category.display()} as most recent treatment line")
            }

            types == null && mostRecentAntiCancerDrug?.categories()?.contains(category) == true -> {
                EvaluationFactory.pass("Has received ${category.display()} as most recent treatment line")
            }

            treatmentMatch.any { it.startYear == null } -> {
                EvaluationFactory.undetermined("Has received$typeString ${category.display()} but undetermined if most recent (date unknown)")
            }

            treatmentMatch.isNotEmpty() -> {
                EvaluationFactory.fail("Has received$typeString ${category.display()} but not as the most recent treatment line")
            }

            else -> {
                EvaluationFactory.fail("Has not received$typeString ${category.display()} as prior therapy")
            }
        }
    }
}