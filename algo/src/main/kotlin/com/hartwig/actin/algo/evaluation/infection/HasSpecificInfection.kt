package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasSpecificInfection(
    private val icdModel: IcdModel, private val icdCodes: Set<IcdCode>, private val term: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingComorbidities = icdModel.findInstancesMatchingAnyIcdCode(record.comorbidities, icdCodes)
        val infectionStatus = record.clinicalStatus.infectionStatus
        val hasMatchingInfection = if (infectionStatus?.hasActiveInfection == true) {
            infectionStatus.description?.let { description ->
                description.contains(term) || term.contains(description)
            }
        } else {
            false
        }

        return when {
            matchingComorbidities.fullMatches.isNotEmpty() || hasMatchingInfection == true -> {
                EvaluationFactory.pass("${term.replaceFirstChar(Char::uppercase)} infection in history")
            }

            hasMatchingInfection == null || matchingComorbidities.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> {
                EvaluationFactory.undetermined("Infection in history but undetermined if $term")
            }

            else -> EvaluationFactory.fail("No $term infection")
        }
    }
}