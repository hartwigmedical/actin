package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.Intolerance.IntoleranceFunctions
import com.hartwig.actin.algo.evaluation.othercondition.PriorOtherConditionFunctions
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

        val monoClonalAntibodyIntolerances = IntoleranceFunctions.findIntoleranceMatchingAnyIcdCode(
            icdModel,
            record,
            IcdConstants.DRUG_ALLERGY_SET.map { IcdCode(it, IcdConstants.MONOCLONAL_ANTIBODY_BLOCK) }.toSet()
        ).fullMatches

        val matchingIntolerancesByName =
            record.intolerances.filter { stringCaseInsensitivelyMatchesQueryCollection(it.name, INTOLERANCE_TERMS) }.toSet()

        val matchingIntolerancesByMainCode = IntoleranceFunctions.findIntoleranceMatchingAnyIcdCode(icdModel, record, targetCodes)
        val matchingIntolerances = (matchingIntolerancesByMainCode.fullMatches + matchingIntolerancesByName).map { it.name }.toSet()
        val undeterminedDrugIntolerances = matchingIntolerancesByMainCode.mainCodeMatchesWithUnknownExtension.map { it.name }.toSet()

        val autoImmuneHistory = OtherConditionSelector.selectClinicallyRelevant(
            PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(
                icdModel,
                record,
                IcdConstants.AUTOIMMUNE_DISEASE_SET.map { IcdCode(it) }.toSet()
            ).fullMatches
        ).map { it.name }

        val undeterminedMessage = "intolerance in history - but undetermined if PD-1/PD-L1 intolerance"

        return when {
            matchingIntolerances.isNotEmpty() -> {
                EvaluationFactory.pass("Has PD-1/PD-L1 intolerance(s): " + Format.concatWithCommaAndAnd(matchingIntolerances))
            }

            undeterminedDrugIntolerances.isNotEmpty() -> EvaluationFactory.undetermined("Drug $undeterminedMessage (drug type unknown)")

            monoClonalAntibodyIntolerances.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Monoclonal antibody $undeterminedMessage: " + Format.concatWithCommaAndAnd(
                        monoClonalAntibodyIntolerances.map { it.name })
                )
            }

            autoImmuneHistory.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Patient may have a contra-indication for PD-1/PD-L1 inhibitors due to autoimmune disease " +
                            "(${Format.concatWithCommaAndAnd(autoImmuneHistory)})"
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