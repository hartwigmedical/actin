package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

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

        val ignoreDrugsList = concatItemsWithAnd(ignoreDrugs)

        val matchingDrugTypes = treatmentSummary.specificMatches
            .map { it.treatments.flatMap(Treatment::types).map { t -> t.display() }.toSet() }
            .flatten()
            .joinToString(", ")
        val typeMessage = if (types != null && matchingDrugTypes.isNotEmpty()) " of types $matchingDrugTypes" else ""
        val messageEnding = "received ${category.display()}$typeMessage ignoring $ignoreDrugsList"

        return when {
            treatmentSummary.hasSpecificMatch() -> EvaluationFactory.pass("Patient has $messageEnding", "Has $messageEnding")

            treatmentSummary.hasPossibleTrialMatch() -> EvaluationFactory.undetermined(
                "Patient may have $messageEnding due to trial participation",
                "Undetermined if $messageEnding due to trial participation"
            )

            else -> EvaluationFactory.fail("Patient has not $messageEnding", "Has not $messageEnding")
        }
    }
}