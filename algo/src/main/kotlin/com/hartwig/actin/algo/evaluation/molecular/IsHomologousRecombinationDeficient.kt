package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristicEvents
import com.hartwig.actin.molecular.util.GeneConstants

class IsHomologousRecombinationDeficient(labels: EvaluationLabels.Molecular) : MolecularEvaluationFunction(labels = labels) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val hrdGenesWithBiallelicDriver: MutableSet<String> = mutableSetOf()
        val hrdGenesWithNonBiallelicDriver: MutableSet<String> = mutableSetOf()
        val hrdGenesWithUnknownAllelicDriver: MutableSet<String> = mutableSetOf()
        for (gene in GeneConstants.HR_GENES) {
            for (variant in test.drivers.variants) {
                if (variant.gene == gene && variant.isReportable) {
                    when (variant.isBiallelic) {
                        true -> {
                            hrdGenesWithBiallelicDriver.add(gene)
                        }

                        false -> {
                            hrdGenesWithNonBiallelicDriver.add(gene)
                        }

                        null -> {
                            hrdGenesWithUnknownAllelicDriver.add(gene)
                        }
                    }
                }
            }
            for (copyNumber in test.drivers.copyNumbers) {
                if (copyNumber.canonicalImpact.type.isDeletion && copyNumber.gene == gene) {
                    hrdGenesWithBiallelicDriver.add(gene)
                }
            }
            for (homozygousDisruption in test.drivers.homozygousDisruptions) {
                if (homozygousDisruption.gene == gene) {
                    hrdGenesWithBiallelicDriver.add(gene)
                }
            }
            for (disruption in test.drivers.disruptions) {
                if (disruption.gene == gene && disruption.isReportable) {
                    hrdGenesWithNonBiallelicDriver.add(gene)
                }
            }
        }
        return when (test.characteristics.homologousRecombination?.isDeficient) {
            null -> {
                if (hrdGenesWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        labels.isHomologousRecombinationDeficientUndeterminedBiallelic(concat(hrdGenesWithBiallelicDriver)),
                        isMissingMolecularResultForEvaluation = true
                    )
                } else if (hrdGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        labels.isHomologousRecombinationDeficientUndeterminedNonBiallelic(concat(hrdGenesWithNonBiallelicDriver)),
                        isMissingMolecularResultForEvaluation = true
                    )
                } else if (hrdGenesWithUnknownAllelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        labels.isHomologousRecombinationDeficientUndeterminedUnknownAllelic(concat(hrdGenesWithUnknownAllelicDriver)),
                        isMissingMolecularResultForEvaluation = true
                    )
                } else {
                    EvaluationFactory.undetermined(
                        labels.isHomologousRecombinationDeficientUndetermined(),
                        isMissingMolecularResultForEvaluation = true
                    )
                }
            }

            true -> {
                val inclusionMolecularEvents = setOf(MolecularCharacteristicEvents.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
                if (hrdGenesWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.pass(
                        labels.isHomologousRecombinationDeficientPass(concat(hrdGenesWithBiallelicDriver)),
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else if (hrdGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.warn(
                        labels.isHomologousRecombinationDeficientWarnNonBiallelic(concat(hrdGenesWithNonBiallelicDriver)),
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else {
                    EvaluationFactory.warn(
                        labels.isHomologousRecombinationDeficientWarnNoDriver(),
                        inclusionEvents = inclusionMolecularEvents
                    )
                }
            }

            else -> {
                EvaluationFactory.fail(labels.isHomologousRecombinationDeficientFail())
            }
        }
    }
}