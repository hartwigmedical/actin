package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasIntoleranceForPD1OrPDL1Inhibitors(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val targetCodes = IcdConstants.DRUG_ALLERGY_SET.flatMap { mainCode ->
            IcdConstants.PD_L1_PD_1_DRUG_SET.map { extension -> IcdCode(mainCode, extension) }
        }.toSet()

        val monoClonalAntibodyIntolerances = icdModel.findInstancesMatchingAnyIcdCode(
            record.intolerances,
            IcdConstants.DRUG_ALLERGY_SET.map { IcdCode(it, IcdConstants.MONOCLONAL_ANTIBODY_BLOCK) }.toSet()
        ).fullMatches

        val matchingIntolerancesByName =
            record.intolerances.filter { stringCaseInsensitivelyMatchesQueryCollection(it.name, INTOLERANCE_TERMS) }.toSet()

        val (fullMatches, mainCodeMatchesWithUnknownExtension) = icdModel.findInstancesMatchingAnyIcdCode(record.intolerances, targetCodes)
        val matchingIntolerances = (fullMatches + matchingIntolerancesByName).toSet()

        val autoImmuneHistory = icdModel.findInstancesMatchingAnyIcdCode(
            OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions),
            IcdConstants.AUTOIMMUNE_DISEASE_SET.map { IcdCode(it) }.toSet()
        ).fullMatches

        val undeterminedMessage = "intolerance in history - but undetermined if PD-1/PD-L1 intolerance"

        return when {
            matchingIntolerances.isNotEmpty() -> {
                EvaluationFactory.pass("Has PD-1/PD-L1 intolerance(s): " + Format.concatItemsWithAnd(matchingIntolerances))
            }

            mainCodeMatchesWithUnknownExtension.isNotEmpty() -> {
                EvaluationFactory.undetermined("Drug $undeterminedMessage (drug type unknown)")
            }

            monoClonalAntibodyIntolerances.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Monoclonal antibody $undeterminedMessage: " + Format.concatItemsWithAnd(
                        monoClonalAntibodyIntolerances)
                )
            }

            autoImmuneHistory.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Patient may have a contra-indication for PD-1/PD-L1 inhibitors due to autoimmune disease " +
                            "(${Format.concatItemsWithAnd(autoImmuneHistory)})"
                )
            }

            else -> EvaluationFactory.fail("Patient does not have PD-1/PD-L1 intolerance")
        }
    }

    companion object {
        val INTOLERANCE_TERMS =
            listOf("Pembrolizumab", "Nivolumab", "Cemiplimab", "Avelumab", "Atezolizumab", "Durvalumab", "PD-1", "PD-L1")
    }
}