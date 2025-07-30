package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class ProteinIsWildTypeByIhc(private val protein: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val allIhcTestsForProtein =
            IhcTestFilter.allIhcTestsForProtein(record.ihcTests, protein)
        val hasOnlyWildTypeResults = allIhcTestsForProtein.isNotEmpty() && allIhcTestsForProtein.all { test ->
            WILD_TYPE_QUERY_STRINGS.any { it.equals(test.scoreText, ignoreCase = true) }
        }

        return if (hasOnlyWildTypeResults) {
            EvaluationFactory.pass("$protein is wild type by IHC", inclusionEvents = setOf("IHC $protein wildtype"))
        } else {
            EvaluationFactory.undetermined("$protein wild type status by IHC unknown", isMissingMolecularResultForEvaluation = true)
        }
    }

    companion object {
        private val WILD_TYPE_QUERY_STRINGS = setOf("wildtype", "wild-type", "wild type")
    }
}