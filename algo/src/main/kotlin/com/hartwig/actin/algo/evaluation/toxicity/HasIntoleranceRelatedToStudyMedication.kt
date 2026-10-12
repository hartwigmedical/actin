package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.icd.IcdModel

class HasIntoleranceRelatedToStudyMedication(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val drugAllergies = DrugAllergyFunctions.findDrugAllergies(
            icdModel,
            record.comorbidities.filterNot { it is Toxicity && it.grade?.let { grade -> grade < 2 } ?: true },
            setOf(IcdConstants.ANTI_NEOPLASTIC_AGENTS)
        )
        val specificDrugAllergies = drugAllergies.fullMatches.toSet()
        val unknownDrugAllergies = drugAllergies.mainCodeMatchesWithUnknownExtension.toSet()
        val undeterminedMessage = "undetermined if allergy to study medication"

        return when {
            specificDrugAllergies.isNotEmpty() -> EvaluationFactory.undetermined(
                "Has medication-related allergies (${Format.concatItemsWithAnd(specificDrugAllergies)}) - $undeterminedMessage"
            )

            unknownDrugAllergies.isNotEmpty() -> EvaluationFactory.undetermined(
                "Has drug allergies (${Format.concatItemsWithAnd(unknownDrugAllergies)}) - $undeterminedMessage (drug type unknown)"
            )

            else -> EvaluationFactory.fail("Has no intolerances to study medication")
        }
    }
}