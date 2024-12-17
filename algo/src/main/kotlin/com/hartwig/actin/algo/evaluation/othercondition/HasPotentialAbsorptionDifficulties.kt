package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.complication.ComplicationFunctions
import com.hartwig.actin.algo.evaluation.toxicity.ToxicityFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.IcdModel

class HasPotentialAbsorptionDifficulties(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetIcdCodes = IcdConstants.POSSIBLE_ABSORPTION_DIFFICULTIES_SET.map { IcdCode(it) }.toSet()
        val conditions =
            PriorOtherConditionFunctions.findRelevantPriorConditionsMatchingAnyIcdCode(icdModel, record, targetIcdCodes).fullMatches

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

        val toxicities = record.toxicities
            .filter { it.source == ToxicitySource.QUESTIONNAIRE || (it.grade ?: 0) >= 2 }
            .filter { ToxicityFunctions.findToxicityMatchingAnyIcdCode(icdModel, record, targetIcdCodes).fullMatches.contains(it) }

        return if (toxicities.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has potential absorption difficulties due to " + Format.concatItemsWithAnd(toxicities),
                "Potential absorption difficulties: " + Format.concatItemsWithAnd(toxicities)
            )
        } else
            EvaluationFactory.fail(
                "No potential reasons for absorption problems identified",
                "No potential absorption difficulties identified"
            )
    }
}