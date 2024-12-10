package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.complication.ComplicationFunctions
import com.hartwig.actin.algo.evaluation.toxicity.ToxicityFunctions
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.IcdModel

class HasPotentialAbsorptionDifficulties(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetIcdCodes = IcdConstants.POSSIBLE_ABSORPTION_DIFFICULTIES_LIST
        val conditions = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions).flatMap {
            PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(icdModel, record, targetIcdCodes).fullMatches }
            .map { it.name }

        if (conditions.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Patient has potential absorption difficulties due to " + concat(conditions),
                "Potential absorption difficulties: " + concat(conditions)
            )
        }

        val complications = ComplicationFunctions.findComplicationsMatchingAnyIcdCode(record, targetIcdCodes, icdModel).map { it.name }
        if (complications.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Patient has potential absorption difficulties due to " + concat(complications),
                "Potential absorption difficulties: " + concat(complications)
            )
        }

        val toxicities = record.toxicities
            .filter { it.source == ToxicitySource.QUESTIONNAIRE || (it.grade ?: 0) >= 2 }
            .filter { ToxicityFunctions.hasIcdMatch(it, targetIcdCodes , icdModel) }
            .map { it.name }

        return if (toxicities.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has potential absorption difficulties due to " + concat(toxicities),
                "Potential absorption difficulties: " + concat(toxicities)
            )
        } else
            EvaluationFactory.fail(
                "No potential reasons for absorption problems identified",
                "No potential absorption difficulties identified"
            )
    }
}