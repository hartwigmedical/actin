package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.warn
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndAnd
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasIntoleranceForPD1OrPDL1Inhibitors(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val intolerances = record.intolerances.map { it.name }
            .filter { stringCaseInsensitivelyMatchesQueryCollection(it, INTOLERANCE_TERMS) }
            .toSet()

        return if (intolerances.isNotEmpty()) {
            pass("Has PD-1/PD-L1 intolerance(s) (${concatLowercaseWithCommaAndAnd(intolerances)}")
        } else {
            val autoImmuneDiseaseTerms =
                OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions).flatMap { it.doids }
                    .filter { doidModel.doidWithParents(it).contains(DoidConstants.AUTOIMMUNE_DISEASE_DOID) }
                    .map { doidModel.resolveTermForDoid(it) }.toSet()

            if (autoImmuneDiseaseTerms.isNotEmpty()) {
                warn(
                    "Possible PD-1/PD-L1 intolerance due to autoimmune disease " +
                            "(${concatLowercaseWithCommaAndAnd(autoImmuneDiseaseTerms.filterNotNull())})"
                )
            } else {
                fail("No PD-1/PD-L1 intolerance")
            }
        }
    }

    companion object {
        val INTOLERANCE_TERMS =
            listOf("Pembrolizumab", "Nivolumab", "Cemiplimab", "Avelumab", "Atezolizumab", "Durvalumab", "PD-1", "PD-L1")
    }
}