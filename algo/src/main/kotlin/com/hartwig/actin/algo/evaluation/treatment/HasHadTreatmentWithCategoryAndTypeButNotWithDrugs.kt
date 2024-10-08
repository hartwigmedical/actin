package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithComma
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
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory,
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

        val priorCancerMedication = record.medications
            ?.filter { medication ->
                MedicationFunctions.hasCategory(medication, category) && (types?.let {
                    medication.drug?.drugTypes?.intersect(types)?.isNotEmpty()
                } ?: true) && (!ignoreDrugs.contains(medication.drug))
            } ?: emptyList()

        val matchingTreatmentTypes = treatmentSummary.specificMatches.map { it.treatments.flatMap(Treatment::types) }.flatten().toSet()
        val matchingMedicationTypes = priorCancerMedication.flatMap { it.drug?.drugTypes ?: emptyList() }.toSet()
        val totalMatchingTypes = concatItemsWithComma(matchingTreatmentTypes + matchingMedicationTypes)

        val ignoreDrugsList = concatItemsWithAnd(ignoreDrugs)
        val typeMessage = if (types != null && totalMatchingTypes.isNotEmpty()) " of types $totalMatchingTypes" else ""
        val messageEnding = "received ${category.display()}$typeMessage ignoring $ignoreDrugsList"

        return when {
            treatmentSummary.hasSpecificMatch() || priorCancerMedication.isNotEmpty() -> {
                EvaluationFactory.pass("Patient has $messageEnding", "Has $messageEnding")
            }

            treatmentSummary.hasPossibleTrialMatch() || record.medications?.any { it.isTrialMedication } == true -> {
                EvaluationFactory.undetermined(
                    "Patient may have $messageEnding due to trial participation",
                    "Undetermined if $messageEnding due to trial participation"
                )
            }

            else -> EvaluationFactory.fail("Patient has not $messageEnding", "Has not $messageEnding")
        }
    }
}