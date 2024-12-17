package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.intolerance.IntoleranceFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasDrugIntoleranceWithAnyIcdCodeOrName(
    private val icdModel: IcdModel,
    private val extensionCode: String,
    private val names: Set<String>,
    private val description: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetCodes = IcdConstants.DRUG_ALLERGY_SET.map { IcdCode(it, extensionCode) }.toSet()
        val platinumAllergiesByName =
            record.intolerances.filter { stringCaseInsensitivelyMatchesQueryCollection(it.name, names) }
        val matchingAllergiesByMainCode = IntoleranceFunctions.findIntoleranceMatchingAnyIcdCode(icdModel, record, targetCodes)
        val matchingAllergies = (matchingAllergiesByMainCode.fullMatches + platinumAllergiesByName).toSet()
        val undeterminedDrugAllergies = matchingAllergiesByMainCode.mainCodeMatchesWithUnknownExtension.toSet()

        return when {
            matchingAllergies.isNotEmpty() -> {
                EvaluationFactory.pass("Patient has allergy to a $description: " + Format.concatItemsWithAnd(matchingAllergies))
            }

            undeterminedDrugAllergies.isNotEmpty() -> {
                EvaluationFactory.undetermined("Drug allergy in history - but undetermined if $description allergy (drug type unknown)")
            }

            else -> EvaluationFactory.fail("No known allergy to $description")
        }
    }
}