package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.molecular.MolecularConstants.MSI_GENES
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.GeneAlteration
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents
import java.time.LocalDate

class IsMicrosatelliteUnstable(maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge) {

    override fun noMolecularRecordEvaluation() = EvaluationFactory.undetermined("MSI status undetermined")

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val drivers = molecular.drivers
        val msiVariants = drivers.variants
            .filter { variant -> variant.gene in MSI_GENES && variant.isReportable }

        val biallelicMsiVariants = msiVariants.filter { it.extendedVariantDetails?.isBiallelic == true }
        val nonBiallelicMsiVariants = msiVariants.filter { it.extendedVariantDetails?.isBiallelic == false }
        val unknownBiallelicMsiVariants = msiVariants.filter { it.extendedVariantDetails?.isBiallelic == null }

        val msiCopyNumbers = drivers.copyNumbers.filter { it.gene in MSI_GENES && it.canonicalImpact.type == CopyNumberType.LOSS }
        val msiHomozygousDisruptions = drivers.homozygousDisruptions.filter { it.gene in MSI_GENES }
        val msiGenesWithBiallelicDriver = genesFrom(biallelicMsiVariants, msiCopyNumbers, msiHomozygousDisruptions)

        val msiDisruptions = drivers.disruptions.filter { it.gene in MSI_GENES && it.isReportable }
        val msiGenesWithNonBiallelicDriver = genesFrom(nonBiallelicMsiVariants, msiDisruptions)

        val msiGenesWithUnknownBiallelicDriver = genesFrom(unknownBiallelicMsiVariants)

        return when (molecular.characteristics.isMicrosatelliteUnstable) {
            null -> {
                if (msiGenesWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined("Unknown MSI status but biallelic drivers in MSI genes")
                } else if (msiGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined("Unknown MSI status but non-biallelic drivers in MSI genes")
                } else if (msiGenesWithUnknownBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown MSI status but drivers with unknown allelic status in MSI genes"
                    )
                } else {
                    EvaluationFactory.fail("Unknown MSI status")
                }
            }

            true -> {
                val inclusionMolecularEvents = setOf(MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE)
                if (msiGenesWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.pass(
                        "Tumor is MSI and biallelic drivers in MSI genes detected",
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else if (msiGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.warn(
                        "Tumor is MSI (but with only non-biallelic drivers in MSI genes)",
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else {
                    EvaluationFactory.warn(
                        "Tumor is MSI (but without drivers in MSI genes)",
                        inclusionEvents = inclusionMolecularEvents
                    )
                }
            }

            false -> {
                EvaluationFactory.fail("Tumor is MSS")
            }
        }
    }

    private fun genesFrom(vararg geneAlterations: Iterable<GeneAlteration>) =
        geneAlterations.asSequence().flatten().map(GeneAlteration::gene).toSet()
}