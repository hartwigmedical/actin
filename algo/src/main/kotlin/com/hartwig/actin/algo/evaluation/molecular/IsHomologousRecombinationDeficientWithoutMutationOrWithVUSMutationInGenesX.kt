package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristicEvents

class IsHomologousRecombinationDeficientWithoutMutationOrWithVUSMutationInGenesX(
    private val genesToFind: Set<String>,
    labels: EvaluationLabels.Molecular
) : MolecularEvaluationFunction(labels = labels) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val isHRD = test.characteristics.homologousRecombination?.isDeficient

        with(HomologousRecombinationDeficiencyGeneSummary.createForDrivers(test.drivers)) {
            val genesToFindWithDeletionOrPartialDel = genesInGenesToFind(hrdGenesWithDeletionOrPartialDel)
            val genesToFindWithBiallelicCav = genesInGenesToFind(hrdGenesWithBiallelicCav)
            val genesToFindWithNonBiallelicCav = genesInGenesToFind(hrdGenesWithNonBiallelicCav)

            val warnEvaluations = mutableSetOf<String>()
            addToWarnEvaluations(
                warnEvaluations,
                "non-cancer-associated variant biallelic high driver(s)",
                genesInGenesToFind(hrdGenesWithBiallelicNonCavHighDriver)
            )
            addToWarnEvaluations(
                warnEvaluations,
                "non-cancer-associated variant biallelic non-high driver(s)",
                genesInGenesToFind(hrdGenesWithBiallelicNonCavNonHighDriver)
            )
            addToWarnEvaluations(
                warnEvaluations,
                "non-cancer-associated variant non-biallelic high driver(s)",
                genesInGenesToFind(hrdGenesWithNonBiallelicNonCavHighDriver)
            )
            addToWarnEvaluations(
                warnEvaluations,
                "non-cancer-associated variant non-biallelic non-high driver(s)",
                genesInGenesToFind(hrdGenesWithNonBiallelicNonCavNonHighDriver)
            )
            addToWarnEvaluations(warnEvaluations, "homozygous disruption", genesInGenesToFind(hrdGenesWithHomozygousDisruption))
            addToWarnEvaluations(warnEvaluations, "non-homozygous disruption", genesInGenesToFind(hrdGenesWithNonHomozygousDisruption))

            val inclusionEvents = setOf(MolecularCharacteristicEvents.HOMOLOGOUS_RECOMBINATION_DEFICIENT)

            return when {
                isHRD == null && hrdGenesWithBiallelicDriver.isNotEmpty() -> {
                    EvaluationFactory.undetermined(
                        labels.isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXUndeterminedBiallelic(),
                        isMissingMolecularResultForEvaluation = true
                    )
                }

                isHRD == null && hrdGenesWithNonBiallelicDriver.isNotEmpty() -> {
                    EvaluationFactory.undetermined(
                        labels.isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXUndeterminedNonBiallelic(),
                        isMissingMolecularResultForEvaluation = true
                    )
                }

                isHRD == null -> {
                    EvaluationFactory.undetermined(
                        labels.isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXUndetermined(),
                        isMissingMolecularResultForEvaluation = true
                    )
                }

                !isHRD -> EvaluationFactory.fail(labels.isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXFail())

                genesToFindWithBiallelicCav.isNotEmpty() || genesToFindWithNonBiallelicCav.isNotEmpty() -> {
                    EvaluationFactory.fail(
                        labels.isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXFailCav(
                            concat(genesToFindWithNonBiallelicCav + genesToFindWithBiallelicCav)
                        )
                    )
                }

                genesToFindWithDeletionOrPartialDel.isNotEmpty() -> {
                    EvaluationFactory.fail(
                        labels.isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXFailDeletion(
                            concat(genesToFindWithDeletionOrPartialDel)
                        )
                    )
                }

                warnEvaluations.isNotEmpty() -> {
                    warnEvaluation(warnEvaluations, inclusionEvents)
                }

                hrdGenesWithNonBiallelicDriver.isNotEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                    EvaluationFactory.warn(
                        labels.isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXWarnNonBiallelic(),
                        inclusionEvents = inclusionEvents
                    )
                }

                hrdGenesWithNonBiallelicDriver.isEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                    EvaluationFactory.warn(
                        labels.isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXWarnNoDriver(),
                        inclusionEvents = inclusionEvents
                    )
                }

                else -> {
                    EvaluationFactory.pass(
                        labels.isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXPass(concat(genesToFind)),
                        inclusionEvents = inclusionEvents
                    )
                }
            }
        }
    }

    private fun genesInGenesToFind(genes: Iterable<String>): Set<String> {
        return genes.intersect(genesToFind)
    }

    private fun addToWarnEvaluations(warnEvaluations: MutableSet<String>, driverType: String, foundGenes: Set<String>) {
        if (foundGenes.isNotEmpty()) {
            warnEvaluations.add(driverType + " in " + concat(foundGenes))
        }
    }

    private fun warnEvaluation(driverTypeInFoundGenes: Set<String>, inclusionEvents: Set<String>): Evaluation {
        return EvaluationFactory.warn(
            labels.isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXWarnPathogenic(
                concat(driverTypeInFoundGenes)
            ),
            inclusionEvents = inclusionEvents
        )
    }
}