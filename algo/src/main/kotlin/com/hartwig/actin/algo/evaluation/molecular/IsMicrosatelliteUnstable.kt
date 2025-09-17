package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.molecular.util.GeneConstants
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents
import java.time.LocalDate

class IsMicrosatelliteUnstable(maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val drivers = test.drivers
        val msiVariants = drivers.variants
            .filter { variant -> variant.gene in GeneConstants.MMR_GENES && variant.isReportable }

        val biallelicMsiVariants = msiVariants.filter { it.isBiallelic == true }
        val nonBiallelicMsiVariants = msiVariants.filter { it.isBiallelic == false }
        val unknownBiallelicMsiVariants = msiVariants.filter { it.isBiallelic == null }

        val msiCopyNumbers = drivers.copyNumbers.filter { it.gene in GeneConstants.MMR_GENES && it.canonicalImpact.type in GeneConstants.DELETION }
        val msiHomozygousDisruptions = drivers.homozygousDisruptions.filter { it.gene in GeneConstants.MMR_GENES }
        val msiGenesWithBiallelicDriver = genesFrom(biallelicMsiVariants, msiCopyNumbers, msiHomozygousDisruptions)

        val msiDisruptions = drivers.disruptions.filter { it.gene in GeneConstants.MMR_GENES && it.isReportable }
        val msiGenesWithNonBiallelicDriver = genesFrom(nonBiallelicMsiVariants, msiDisruptions)

        val msiGenesWithUnknownBiallelicDriver = genesFrom(unknownBiallelicMsiVariants)

        return when (test.characteristics.microsatelliteStability?.isUnstable) {
            null -> {
                val message = when {
                    msiGenesWithBiallelicDriver.isNotEmpty() -> {
                        " but biallelic driver event(s) in MMR gene(s) ($msiGenesWithBiallelicDriver) detected"
                    }
                    msiGenesWithNonBiallelicDriver.isNotEmpty() -> {
                        " but non-biallelic driver event(s) in MMR gene(s) ($msiGenesWithNonBiallelicDriver) detected"
                    }
                    msiGenesWithUnknownBiallelicDriver.isNotEmpty() -> {
                        " but driver event(s) in MMR gene(s) ($msiGenesWithUnknownBiallelicDriver) detected"
                    }
                    else -> ""
                }
                EvaluationFactory.undetermined("No MSI test result$message", isMissingMolecularResultForEvaluation = true)
            }

            true -> {
                val inclusionMolecularEvents = setOf(MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE)
                if (msiGenesWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.pass(
                        "Tumor is MSI with biallelic driver event(s) in MMR gene(s) ($msiGenesWithBiallelicDriver)",
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else if (msiGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.warn(
                        "Tumor is MSI but with only non-biallelic driver event(s) in MMR gene(s) ($msiGenesWithNonBiallelicDriver)",
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else {
                    EvaluationFactory.warn(
                        "Tumor is MSI but without driver event(s) in MMR gene(s)",
                        inclusionEvents = inclusionMolecularEvents
                    )
                }
            }

            false -> {
                EvaluationFactory.fail("Tumor is not MSI")
            }
        }
    }

    private fun genesFrom(vararg geneAlterations: Iterable<GeneAlteration>) =
        Format.concat(geneAlterations.asList().flatten().map(GeneAlteration::gene))
}