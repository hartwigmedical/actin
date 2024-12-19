package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.complication.ComplicationFunctions
import com.hartwig.actin.algo.evaluation.toxicity.ToxicityFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasPotentialAbsorptionDifficulties(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetIcdCodes = IcdConstants.POSSIBLE_ABSORPTION_DIFFICULTIES_SET.map { IcdCode(it) }.toSet()
        val conditions = icdModel.findInstancesMatchingAnyIcdCode(
            OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions),
            targetIcdCodes
        ).fullMatches

        if (conditions.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Patient has potential absorption difficulties due to " + Format.concatItemsWithAnd(conditions),
                "Potential absorption difficulties: " + Format.concatItemsWithAnd(conditions)
            )
        }

        val complications = ComplicationFunctions.findComplicationsMatchingAnyIcdCode(icdModel, record, targetIcdCodes).fullMatches

        if (complications.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Patient has potential absorption difficulties due to " + Format.concatItemsWithAnd(complications),
                "Potential absorption difficulties: " + Format.concatItemsWithAnd(complications)
            )
        }

        val matchingToxicities = ToxicityFunctions.findToxicitiesMatchingAnyIcdCode(icdModel, record.toxicities, targetIcdCodes).fullMatches

        return if (matchingToxicities.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has potential absorption difficulties due to " + Format.concatItemsWithAnd(matchingToxicities),
                "Potential absorption difficulties: " + Format.concatItemsWithAnd(matchingToxicities)
            )
        } else
            EvaluationFactory.fail(
                "No potential reasons for absorption problems identified",
                "No potential absorption difficulties identified"
            )
    }
}