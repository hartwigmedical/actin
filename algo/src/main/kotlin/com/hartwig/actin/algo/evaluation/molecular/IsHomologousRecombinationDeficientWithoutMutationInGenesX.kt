package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import java.time.LocalDate

class IsHomologousRecombinationDeficientWithoutMutationInGenesX(private val genesToFind: Set<String>, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val isHRD = test.characteristics.homologousRecombination?.isDeficient

        with(HomologousRecombinationDeficiencyGeneSummary.createForDrivers(test.drivers)) {
            val genesToFindWithMutation = genesInGenesToFind(hrdGenesWithBiallelicDriver + hrdGenesWithNonBiallelicDriver)

            return when {
                isHRD == null && hrdGenesWithBiallelicDriver.isNotEmpty() && genesToFindWithMutation.isEmpty() -> {
                    EvaluationFactory.undetermined(
                        "Unknown HRD status but biallelic drivers in HR genes",
                        isMissingMolecularResultForEvaluation = true
                    )
                }

                isHRD == null && hrdGenesWithNonBiallelicDriver.isNotEmpty() && genesToFindWithMutation.isEmpty() -> {
                    EvaluationFactory.undetermined(
                        "Unknown HRD status but non-biallelic drivers in HR genes",
                        isMissingMolecularResultForEvaluation = true
                    )
                }

                isHRD == null -> {
                    EvaluationFactory.fail("Unknown HRD status", isMissingMolecularResultForEvaluation = true)
                }

                isHRD == false -> {
                    EvaluationFactory.fail("Tumor is not HRD")
                }

                genesToFindWithMutation.isNotEmpty() -> {
                    EvaluationFactory.fail("Tumor is HRD with variant in ${Format.concat(genesToFindWithMutation)}")
                }

                hrdGenesWithNonBiallelicDriver.isNotEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                    EvaluationFactory.warn("Tumor is HRD but with only non-biallelic drivers in HR genes")
                }

                hrdGenesWithNonBiallelicDriver.isEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                    EvaluationFactory.warn("Tumor is HRD but without drivers in HR genes")
                }

                else -> {
                    EvaluationFactory.pass("Tumor is HRD without any variants in ${Format.concatWithCommaAndOr(genesToFind)}")
                }
            }
        }
    }

    private fun genesInGenesToFind(genes: Iterable<String>): Set<String> {
        return genes.intersect(genesToFind)
    }

}