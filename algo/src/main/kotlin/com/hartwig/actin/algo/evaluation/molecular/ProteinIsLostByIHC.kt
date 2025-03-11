package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class ProteinIsLostByIHC(private val protein: String, private val maxTestAge: LocalDate? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTests = IhcTestFilter.allIHCTestsForProtein(record.priorIHCTests, protein)

        val geneIsWildType = MolecularRuleEvaluator.geneIsWildTypeForPatient(protein, record, maxTestAge)
        val additionalMessage = if (geneIsWildType) " though $protein is wild-type in recent molecular test" else ""

        return when {
            ihcTests.any { ihcTest -> ihcTest.scoreText?.lowercase() == "loss" } -> {
                EvaluationFactory.pass("$protein is lost by IHC")
            }

            ihcTests.any { ihcTest -> ihcTest.scoreText?.lowercase() != "no loss" } -> {
                EvaluationFactory.undetermined("$protein IHC test(s) available but undetermined if $protein is lost")
            }

            ihcTests.isNotEmpty() -> {
                EvaluationFactory.fail("$protein is not lost by IHC")
            }

            else -> {
                EvaluationFactory.undetermined("No $protein IHC test result$additionalMessage", isMissingMolecularResultForEvaluation = true)
            }
        }
    }
}
