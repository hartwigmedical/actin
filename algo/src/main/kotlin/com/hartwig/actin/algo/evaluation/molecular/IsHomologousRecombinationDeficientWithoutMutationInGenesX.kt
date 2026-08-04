package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristicEvents
import com.hartwig.actin.molecular.util.GeneConstants

class IsHomologousRecombinationDeficientWithoutMutationInGenesX(
    private val genesToFind: Set<String>,
    labels: EvaluationLabels.Molecular
) : MolecularEvaluationFunction(labels = labels) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val isHRD = test.characteristics.homologousRecombination?.isDeficient

        with(HomologousRecombinationDeficiencyGeneSummary.createForDrivers(test.drivers)) {
            val genesToFindWithMutation = genesInGenesToFind(hrdGenesWithBiallelicDriver + hrdGenesWithNonBiallelicDriver)
            val inclusionEvents = setOf(MolecularCharacteristicEvents.HOMOLOGOUS_RECOMBINATION_DEFICIENT)

            return when {
                isHRD == null && hrdGenesWithBiallelicDriver.isNotEmpty() && genesToFindWithMutation.isEmpty() -> {
                    EvaluationFactory.undetermined(
                        labels.isHomologousRecombinationDeficientWithoutMutationInGenesXUndeterminedBiallelic(),
                        isMissingMolecularResultForEvaluation = true
                    )
                }

                isHRD == null && hrdGenesWithNonBiallelicDriver.isNotEmpty() && genesToFindWithMutation.isEmpty() -> {
                    EvaluationFactory.undetermined(
                        labels.isHomologousRecombinationDeficientWithoutMutationInGenesXUndeterminedNonBiallelic(),
                        isMissingMolecularResultForEvaluation = true
                    )
                }

                isHRD == null -> {
                    EvaluationFactory.undetermined(
                        labels.isHomologousRecombinationDeficientWithoutMutationInGenesXUndetermined(),
                        isMissingMolecularResultForEvaluation = true
                    )
                }

                !isHRD -> EvaluationFactory.fail(labels.isHomologousRecombinationDeficientWithoutMutationInGenesXFail())

                genesToFindWithMutation.isNotEmpty() -> {
                    EvaluationFactory.fail(
                        labels.isHomologousRecombinationDeficientWithoutMutationInGenesXFailVariant(Format.concat(genesToFindWithMutation))
                    )
                }

                hrdGenesWithNonBiallelicDriver.isNotEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                    EvaluationFactory.warn(
                        labels.isHomologousRecombinationDeficientWithoutMutationInGenesXWarnNonBiallelic(),
                        inclusionEvents = inclusionEvents
                    )
                }

                hrdGenesWithNonBiallelicDriver.isEmpty() && hrdGenesWithBiallelicDriver.isEmpty() && !genesToFind.containsAll(GeneConstants.HR_GENES) -> {
                    EvaluationFactory.warn(
                        labels.isHomologousRecombinationDeficientWithoutMutationInGenesXWarnNoDriver(),
                        inclusionEvents = inclusionEvents
                    )
                }

                else -> {
                    EvaluationFactory.pass(
                        labels.isHomologousRecombinationDeficientWithoutMutationInGenesXPass(Format.concatWithCommaAndOr(genesToFind)),
                        inclusionEvents = inclusionEvents
                    )
                }
            }
        }
    }

    private fun genesInGenesToFind(genes: Iterable<String>): Set<String> {
        return genes.intersect(genesToFind)
    }

}