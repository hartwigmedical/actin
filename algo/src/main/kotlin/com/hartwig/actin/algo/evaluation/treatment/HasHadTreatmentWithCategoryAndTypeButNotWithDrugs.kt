package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.medication.MedicationToTreatmentConverter

class HasHadTreatmentWithCategoryAndTypeButNotWithDrugs(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>?,
    private val ignoreDrugs: Set<Drug>,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory = MedicationToTreatmentConverter.convertAndCombine(record.medications, record.oncologicalHistory)

        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            effectiveTreatmentHistory,
            category,
            { historyEntry ->
                historyEntry.allTreatments().any { treatment ->
                    val typesMatch = types?.let { treatment.types().intersect(types).isNotEmpty() } ?: true
                    val drugsNotIgnored = (treatment as? DrugTreatment)?.drugs?.intersect(ignoreDrugs)?.isEmpty() == true
                    typesMatch && drugsNotIgnored
                }
            },
            { treatment -> (treatment as? DrugTreatment)?.drugs.isNullOrEmpty() || treatment.types().isEmpty() }
        )

        val matchingTreatmentTypes = treatmentSummary.specificMatches.flatMap { it.treatments.flatMap(Treatment::types) }
        val concatenatedMatchingTypes = concatItemsWithAnd(matchingTreatmentTypes)

        val ignoreDrugsList = concatItemsWithAnd(ignoreDrugs)
        val typeMessage = if (types != null && concatenatedMatchingTypes.isNotEmpty()) {
            labels.hasHadTreatmentWithCategoryAndTypeButNotWithDrugsTypeSuffix(concatenatedMatchingTypes)
        } else ""

        return when {
            treatmentSummary.hasSpecificMatch() -> {
                EvaluationFactory.pass(
                    labels.hasHadTreatmentWithCategoryAndTypeButNotWithDrugsPass(category.display(), typeMessage, ignoreDrugsList)
                )
            }

            treatmentSummary.hasPossibleTrialMatch() -> {
                EvaluationFactory.undetermined(
                    labels.hasHadTreatmentWithCategoryAndTypeButNotWithDrugsUndetermined(category.display(), typeMessage, ignoreDrugsList)
                )
            }

            else -> EvaluationFactory.fail(
                labels.hasHadTreatmentWithCategoryAndTypeButNotWithDrugsFail(category.display(), typeMessage, ignoreDrugsList)
            )
        }
    }
}