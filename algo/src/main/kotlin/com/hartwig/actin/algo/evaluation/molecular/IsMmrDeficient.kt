package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.molecular.filter.MolecularTestFilter
import com.hartwig.actin.molecular.util.GeneConstants
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents
import java.time.LocalDate

class IsMmrDeficient(private val maxTestAge: LocalDate? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTestEvaluation = IhcTestEvaluation.create("MSI", record.ihcTests)
        val certainPositiveIhcResult = ihcTestEvaluation.hasCertainPositiveResultsForItem()
        val certainNegativeIhcResult = ihcTestEvaluation.hasCertainNegativeResultsForItem()

        val molecularTestFilter = MolecularTestFilter(maxTestAge, false)
        val molecularHistory = MolecularHistory(molecularTestFilter.apply(record.molecularTests))

        val test = molecularHistory.latestOrangeMolecularRecord()

        if (test == null && ihcTestEvaluation.filteredTests.isEmpty()) {
            return EvaluationFactory.undetermined("No MMR deficiency test result", isMissingMolecularResultForEvaluation = true)
        }

        if (test == null) {
            return when {
                certainPositiveIhcResult -> EvaluationFactory.pass("dMMR by IHC", inclusionEvents = setOf("IHC dMMR"))
                certainNegativeIhcResult -> EvaluationFactory.fail("Tumor is not dMMR by IHC")
                else -> EvaluationFactory.undetermined("Undetermined dMMR result by IHC")
            }
        }

        val drivers = test.drivers
        val msiVariants = drivers.variants
            .filter { variant -> variant.gene in GeneConstants.MMR_GENES && variant.isReportable }

        val biallelicMsiVariants = msiVariants.filter { it.isBiallelic == true }
        val nonBiallelicMsiVariants = msiVariants.filter { it.isBiallelic == false }
        val unknownBiallelicMsiVariants = msiVariants.filter { it.isBiallelic == null }

        val msiCopyNumbers = drivers.copyNumbers.filter { it.gene in GeneConstants.MMR_GENES && it.canonicalImpact.type.isDeletion }
        val msiHomozygousDisruptions = drivers.homozygousDisruptions.filter { it.gene in GeneConstants.MMR_GENES }
        val msiGenesWithBiallelicDriver = genesFrom(biallelicMsiVariants, msiCopyNumbers, msiHomozygousDisruptions)

        val msiDisruptions = drivers.disruptions.filter { it.gene in GeneConstants.MMR_GENES && it.isReportable }
        val msiGenesWithNonBiallelicDriver = genesFrom(nonBiallelicMsiVariants, msiDisruptions)

        val msiGenesWithUnknownBiallelicDriver = genesFrom(unknownBiallelicMsiVariants)

        return when {
            test.characteristics.microsatelliteStability?.isUnstable == true && certainNegativeIhcResult -> {
                EvaluationFactory.warn("Tumor is MMR proficient by IHC but MSI by molecular test", inclusionEvents = setOf(MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE))
            }

            test.characteristics.microsatelliteStability?.isUnstable == true -> {
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

            certainPositiveIhcResult && test.characteristics.microsatelliteStability?.isUnstable == false -> {
                EvaluationFactory.warn("Tumor is dMMR by IHC but MSS by molecular test", inclusionEvents = setOf("dMMR by IHC"))
            }

            certainPositiveIhcResult -> EvaluationFactory.pass("dMMR by IHC", inclusionEvents = setOf("IHC dMMR"))

            certainNegativeIhcResult || test.characteristics.microsatelliteStability?.isUnstable == false -> {
                EvaluationFactory.fail("Tumor is not dMMR")
            }

            else -> {
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
        }
    }

    private fun genesFrom(vararg geneAlterations: Iterable<GeneAlteration>) =
        Format.concat(geneAlterations.asList().flatten().map(GeneAlteration::gene))
}