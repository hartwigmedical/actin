package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.MedicationFunctions.createTreatmentHistoryEntriesFromMedications
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadTreatmentWithCategoryAndTypeButNotWithDrugs(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>?,
    private val ignoreDrugs: Set<Drug>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory = record.oncologicalHistory + createTreatmentHistoryEntriesFromMedications(record.medications)

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
        val typeMessage = if (types != null && concatenatedMatchingTypes.isNotEmpty()) " of types $concatenatedMatchingTypes" else ""
        val messageEnding = "received ${category.display()}$typeMessage ignoring $ignoreDrugsList"

        return when {
            treatmentSummary.hasSpecificMatch() -> {
                EvaluationFactory.pass("Has $messageEnding")
            }

            treatmentSummary.hasPossibleTrialMatch() -> {
                EvaluationFactory.undetermined("Undetermined if treatment received in previous trial included ${category.display()}$typeMessage ignoring $ignoreDrugsList")
            }

            else -> EvaluationFactory.fail("Has not $messageEnding")
        }
    }
}