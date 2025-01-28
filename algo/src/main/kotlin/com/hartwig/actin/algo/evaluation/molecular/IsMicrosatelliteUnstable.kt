package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.molecular.MolecularConstants.MSI_GENES
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
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
                    EvaluationFactory.undetermined(
                        "No MSI test result but biallelic driver(s) in MMR gene(s) (${Format.concat(msiGenesWithBiallelicDriver)}) detected",
                        isMissingMolecularResultForEvaluation = true
                    )
                } else if (msiGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "No MSI test result but non-biallelic driver(s) in MMR gene(s) (${Format.concat(msiGenesWithNonBiallelicDriver)}) detected",
                        isMissingMolecularResultForEvaluation = true
                    )
                } else if (msiGenesWithUnknownBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "No MSI test result but driver(s) in MMR gene(s) (${
                            Format.concat(
                                msiGenesWithUnknownBiallelicDriver
                            )
                        }) detected",
                        isMissingMolecularResultForEvaluation = true
                    )
                } else {
                    EvaluationFactory.undetermined(
                        "No MSI test result", isMissingMolecularResultForEvaluation = true
                    )
                }
            }

            true -> {
                val inclusionMolecularEvents = setOf(MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE)
                if (msiGenesWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.pass(
                        "Tumor is MSI with biallelic driver event(s) in MMR gene(s) (${Format.concat(msiGenesWithBiallelicDriver)})",
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else if (msiGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.warn(
                        "Tumor is MSI but with only non-biallelic driver event(s) in MMR gene(s) (${Format.concat(msiGenesWithNonBiallelicDriver)})",
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
        geneAlterations.asSequence().flatten().map(GeneAlteration::gene).toSet()
}