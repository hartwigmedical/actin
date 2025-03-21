package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class ProteinIsWildTypeByIHC(private val protein: String, private val gene: String, private val maxTestAge: LocalDate? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val allIHCTestsForProtein =
            IhcTestFilter.allIHCTestsForProtein(record.priorIHCTests, protein)
        val hasOnlyWildTypeResults = allIHCTestsForProtein.isNotEmpty() && allIHCTestsForProtein.all { test ->
            WILD_TYPE_QUERY_STRINGS.any { it.equals(test.scoreText, ignoreCase = true) }
        }

        return if (hasOnlyWildTypeResults) {
            EvaluationFactory.pass("$protein is wild type by IHC")
        } else {
            val additionalMessage = IHCMessagesFunctions.additionalMessageWhenGeneIsWildType(gene, record, maxTestAge)
            EvaluationFactory.undetermined("$protein wild type status by IHC unknown$additionalMessage")
        }
    }

    companion object {
        private val WILD_TYPE_QUERY_STRINGS = setOf("wildtype", "wild-type", "wild type")
    }
}