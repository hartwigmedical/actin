package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.Intolerance.IntoleranceFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasIntoleranceToPlatinumCompounds(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetCodes = IcdConstants.DRUG_ALLERGY_SET.map { IcdCode(it, IcdConstants.PLATINUM_COMPOUND_CODE) }.toSet()
        val platinumAllergiesByName =
            record.intolerances.filter { stringCaseInsensitivelyMatchesQueryCollection(it.name, PLATINUM_COMPOUNDS) }
        val matchingAllergiesByMainCode = IntoleranceFunctions.findIntoleranceMatchingAnyIcdCode(icdModel, record, targetCodes)
        val platinumAllergies = (matchingAllergiesByMainCode.fullMatches + platinumAllergiesByName).map { it.name }.toSet()
        val undeterminedDrugAllergies = matchingAllergiesByMainCode.mainCodeMatchesWithUnknownExtension.map { it.name }.toSet()

        return when {
            platinumAllergies.isNotEmpty() -> {
                EvaluationFactory.pass("Patient has allergy to a platinum compounds: " + Format.concatWithCommaAndAnd(platinumAllergies))
            }

            undeterminedDrugAllergies.isNotEmpty() -> {
                EvaluationFactory.undetermined("Drug allergy in history - but undetermined if platinum compound allergy (drug type unknown)")
            }

            else -> EvaluationFactory.fail("No known allergy to platinum compounds")
        }
    }

        companion object {
            val PLATINUM_COMPOUNDS =
                setOf(
                    "oxaliplatin",
                    "eloxatin",
                    "carboplatin",
                    "paraplatin",
                    "cisplatin",
                    "platinol",
                    "imifolatin",
                    "nedaplatin",
                    "NC-6004"
                )
        }
    }