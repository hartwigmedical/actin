package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents
import java.time.LocalDate

class IsHomologousRepairDeficient(maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val hrdGenesWithBiallelicDriver: MutableSet<String> = mutableSetOf()
        val hrdGenesWithNonBiallelicDriver: MutableSet<String> = mutableSetOf()
        val hrdGenesWithUnknownAllelicDriver: MutableSet<String> = mutableSetOf()
        for (gene in MolecularConstants.HRD_GENES) {
            for (variant in test.drivers.variants) {
                if (variant.gene == gene && variant.isReportable) {
                    when (variant.extendedVariantDetails?.isBiallelic) {
                        true -> {
                            hrdGenesWithBiallelicDriver.add(gene)
                        }

                        false -> {
                            hrdGenesWithNonBiallelicDriver.add(gene)
                        }

                        else -> {
                            hrdGenesWithUnknownAllelicDriver.add(gene)
                        }
                    }
                }
            }
            for (copyNumber in test.drivers.copyNumbers) {
                if (copyNumber.canonicalImpact.type == CopyNumberType.LOSS && copyNumber.gene == gene) {
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
        return when (test.characteristics.isHomologousRepairDeficient) {
            null -> {
                if (hrdGenesWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown HRD status but biallelic drivers in HR genes (${concat(hrdGenesWithBiallelicDriver)})",
                        missingGenesForEvaluation = true
                    )
                } else if (hrdGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown HRD status but non-biallelic drivers in HR genes (${concat(hrdGenesWithNonBiallelicDriver)})",
                        missingGenesForEvaluation = true
                    )
                } else if (hrdGenesWithUnknownAllelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown HRD status but drivers with unknown allelic status in HR genes (${
                            concat(
                                hrdGenesWithNonBiallelicDriver
                            )
                        })"
                    )
                } else {
                    EvaluationFactory.undetermined("Unknown HRD status")
                }
            }

            true -> {
                val inclusionMolecularEvents = setOf(MolecularCharacteristicEvents.HOMOLOGOUS_REPAIR_DEFICIENT)
                if (hrdGenesWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.pass(
                        "Tumor is HRD with biallelic drivers in HR genes (${concat(hrdGenesWithBiallelicDriver)})",
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else if (hrdGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.warn(
                        "Tumor is HRD but with only non-biallelic drivers in HR genes",
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else {
                    EvaluationFactory.warn(
                        "Tumor is HRD but without drivers in HR genes",
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