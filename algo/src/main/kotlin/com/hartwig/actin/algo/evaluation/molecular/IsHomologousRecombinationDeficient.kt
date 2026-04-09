package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristicEvents
import com.hartwig.actin.molecular.util.GeneConstants

class IsHomologousRecombinationDeficient: MolecularEvaluationFunction() {

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
                        "Unknown HRD status but biallelic driver event(s) in HR gene(s) (${concat(hrdGenesWithBiallelicDriver)}) detected",
                        isMissingMolecularResultForEvaluation = true
                    )
                } else if (hrdGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown HRD status but non-biallelic driver event(s) in HR gene(s) (${concat(hrdGenesWithNonBiallelicDriver)}) detected",
                        isMissingMolecularResultForEvaluation = true
                    )
                } else if (hrdGenesWithUnknownAllelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown HRD status but driver event(s) in HR gene(s) (${concat(hrdGenesWithUnknownAllelicDriver)}) detected",
                        isMissingMolecularResultForEvaluation = true
                    )
                } else {
                    EvaluationFactory.undetermined("Unknown HRD status", isMissingMolecularResultForEvaluation = true)
                }
            }

            true -> {
                val inclusionMolecularEvents = setOf(MolecularCharacteristicEvents.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
                if (hrdGenesWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.pass(
                        "Tumor is HRD with biallelic driver event(s) in HR gene(s) (${concat(hrdGenesWithBiallelicDriver)})",
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else if (hrdGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.warn(
                        "Tumor is HRD but with only non-biallelic driver event(s) in HR gene(s) (${concat(hrdGenesWithNonBiallelicDriver)})",
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else {
                    EvaluationFactory.warn(
                        "Tumor is HRD but without driver event(s) in HR gene(s)",
                        inclusionEvents = inclusionMolecularEvents
                    )
                }
            }

            else -> {
                EvaluationFactory.fail("Tumor is not HRD")
            }
        }
    }
}