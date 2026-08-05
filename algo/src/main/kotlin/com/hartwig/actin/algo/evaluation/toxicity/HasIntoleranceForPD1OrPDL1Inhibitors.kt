package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasIntoleranceForPD1OrPDL1Inhibitors(private val icdModel: IcdModel, private val labels: EvaluationLabels.Toxicity) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetCodes = IcdConstants.DRUG_ALLERGY_SET.flatMap { mainCode ->
            IcdConstants.PD_L1_PD_1_DRUG_SET.map { extension -> IcdCode(mainCode, extension) }
        }.toSet()

        val icdMatches = icdModel.findInstancesMatchingAnyIcdCode(record.comorbidities, targetCodes)

        val matchingIntolerancesByName = record.intolerances.filter { intolerance ->
            intolerance.name?.let { stringCaseInsensitivelyMatchesQueryCollection(it, INTOLERANCE_TERMS) } == true
        }

        val matchingIntolerances = (icdMatches.fullMatches + matchingIntolerancesByName).toSet()

        val monoClonalAntibodyIntolerances = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities,
            IcdConstants.DRUG_ALLERGY_SET.map { IcdCode(it, IcdConstants.MONOCLONAL_ANTIBODY_BLOCK) }
        ).fullMatches

        val autoImmuneHistory = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities,
            IcdConstants.AUTOIMMUNE_DISEASE_SET.map { IcdCode(it) }
        ).fullMatches

        return when {
            matchingIntolerances.isNotEmpty() -> {
                EvaluationFactory.pass(labels.hasIntoleranceForPd1OrPdl1InhibitorsPass(Format.concatItemsWithAnd(matchingIntolerances)))
            }

            icdMatches.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> {
                EvaluationFactory.undetermined(labels.hasIntoleranceForPd1OrPdl1InhibitorsUndeterminedDrug())
            }

            monoClonalAntibodyIntolerances.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    labels.hasIntoleranceForPd1OrPdl1InhibitorsUndeterminedMonoclonal(
                        Format.concatItemsWithAnd(monoClonalAntibodyIntolerances)
                    )
                )
            }

            autoImmuneHistory.isNotEmpty() -> {
                EvaluationFactory.warn(labels.hasIntoleranceForPd1OrPdl1InhibitorsWarn(Format.concatItemsWithAnd(autoImmuneHistory)))
            }

            else -> EvaluationFactory.fail(labels.hasIntoleranceForPd1OrPdl1InhibitorsFail())
        }
    }

    companion object {
        val INTOLERANCE_TERMS =
            listOf("Pembrolizumab", "Nivolumab", "Cemiplimab", "Avelumab", "Atezolizumab", "Durvalumab", "PD-1", "PD-L1")
    }
}