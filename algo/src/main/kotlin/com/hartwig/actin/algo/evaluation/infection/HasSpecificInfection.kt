package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasSpecificInfection(
    private val icdModel: IcdModel, private val icdCodes: Set<IcdCode>, private val term: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingConditions =
            icdModel.findInstancesMatchingAnyIcdCode(OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions), icdCodes)
        val infectionStatus = record.clinicalStatus.infectionStatus
        val hasMatchingInfection = infectionStatus?.takeIf { it.hasActiveInfection }?.description?.let { description ->
            description.contains(term) || term.contains(description)
        }

        return when {
            matchingConditions.fullMatches.isNotEmpty() || hasMatchingInfection == true -> {
                EvaluationFactory.pass("$term infection in history")
            }

            hasMatchingInfection == null || matchingConditions.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> {
                EvaluationFactory.undetermined("Infection in history but undetermined if $term")
            }

            else -> EvaluationFactory.fail("No $term infection")
        }
    }
}