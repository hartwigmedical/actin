package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.molecular.MolecularConstants.MSI_GENES
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.molecular.datamodel.GeneAlteration
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.hmf.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.hmf.driver.ExtendedVariant
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents

class IsMicrosatelliteUnstable : MolecularEvaluationFunction {

    override fun noMolecularRecordEvaluation() = EvaluationFactory.undetermined("Undetermined if tumor is MSI", "Undetermined MSI status")

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val drivers = molecular.drivers
        val (biallelicMsiVariants, nonBiallelicMsiVariants) = drivers.variants
            .filter { variant -> variant.gene in MSI_GENES && variant.isReportable }
            .partition(ExtendedVariant::isBiallelic)

        val msiCopyNumbers = drivers.copyNumbers.filter { it.gene in MSI_GENES && it.type == CopyNumberType.LOSS }
        val msiHomozygousDisruptions = drivers.homozygousDisruptions.filter { it.gene in MSI_GENES }
        val msiGenesWithBiallelicDriver = genesFrom(biallelicMsiVariants, msiCopyNumbers, msiHomozygousDisruptions)

        val msiDisruptions = drivers.disruptions.filter { it.gene in MSI_GENES && it.isReportable }
        val msiGenesWithNonBiallelicDriver = genesFrom(nonBiallelicMsiVariants, msiDisruptions)

        return when (molecular.characteristics.isMicrosatelliteUnstable) {
            null -> {
                if (msiGenesWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown microsatellite instability (MSI) status but biallelic drivers in MSI genes: "
                                + Format.concat(msiGenesWithBiallelicDriver) + " are detected - an MSI test may be recommended",
                        "Unknown MSI status but biallelic drivers in MSI genes"
                    )
                } else if (msiGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown microsatellite instability (MSI) status but non-biallelic drivers in MSI genes: "
                                + Format.concat(msiGenesWithNonBiallelicDriver) + " are detected - an MSI test may be recommended",
                        "Unknown MSI status but non-biallelic drivers in MSI genes"
                    )
                } else {
                    EvaluationFactory.fail("Unknown microsatellite instability (MSI) status", "Unknown MSI status")
                }
            }

            true -> {
                val inclusionMolecularEvents = setOf(MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE)
                if (msiGenesWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.pass(
                        "Microsatellite instability (MSI) status detected with biallelic drivers in MSI genes: "
                                + Format.concat(msiGenesWithBiallelicDriver),
                        "Tumor is MSI and biallelic drivers in MSI genes detected",
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else if (msiGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.warn(
                        "Microsatellite instability (MSI) detected with non-biallelic drivers in MSI genes ("
                                + Format.concat(msiGenesWithNonBiallelicDriver) + ")",
                        "Tumor is MSI (but with only non-biallelic drivers in MSI genes)",
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else {
                    EvaluationFactory.warn(
                        "Microsatellite instability (MSI) detected without drivers in MSI genes ("
                                + Format.concat(MSI_GENES) + ")",
                        "Tumor is MSI (but without detected drivers in MSI genes)",
                        inclusionEvents = inclusionMolecularEvents
                    )
                }
            }

            false -> {
                EvaluationFactory.fail("Tumor is microsatellite stable (MSS)", "Tumor is MSS")
            }
        }
    }

    private fun genesFrom(vararg geneAlterations: Iterable<GeneAlteration>) =
        geneAlterations.asSequence().flatten().map(GeneAlteration::gene).toSet()
}