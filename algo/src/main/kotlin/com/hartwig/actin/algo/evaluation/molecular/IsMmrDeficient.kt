package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristicEvents
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.molecular.filter.MolecularTestFilter
import com.hartwig.actin.molecular.util.GeneConstants
import java.time.LocalDate

class IsMmrDeficient(private val maxTestAge: LocalDate? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTestEvaluation = IhcTestEvaluation.create("MMR", record.ihcTests)
        val isMmrDeficientIhcResult = isMmrDeficientIhc(ihcTestEvaluation)
        val isMmrProficientIhcResult = isMmrProficientIhc(ihcTestEvaluation)

        val molecularTestFilter = MolecularTestFilter(maxTestAge, false)
        val molecularHistory = MolecularHistory(molecularTestFilter.apply(record.molecularTests))
        val test = findRelevantTest(molecularHistory)

        val drivers = test?.drivers
        val msiVariants = drivers?.variants?.filter { variant -> variant.gene in GeneConstants.MMR_GENES && variant.isReportable }
            ?.groupBy { it.isBiallelic } ?: emptyMap()
        val msiCopyNumbers =
            drivers?.copyNumbers?.filter { it.gene in GeneConstants.MMR_GENES && it.canonicalImpact.type.isDeletion } ?: emptyList()
        val msiHomozygousDisruptions = drivers?.homozygousDisruptions?.filter { it.gene in GeneConstants.MMR_GENES } ?: emptyList()
        val msiGenesWithBiallelicDriver = genesFrom(msiVariants[true] ?: emptyList(), msiCopyNumbers, msiHomozygousDisruptions)

        val msiDisruptions = drivers?.disruptions?.filter { it.gene in GeneConstants.MMR_GENES && it.isReportable } ?: emptyList()
        val msiGenesWithNonBiallelicDriver = genesFrom(msiVariants[false] ?: emptyList(), msiDisruptions)
        val msiGenesWithUnknownBiallelicDriver = genesFrom(msiVariants[null] ?: emptyList())

        val inclusionMolecularEvents = setOf(MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE)

        return when {
            test == null && ihcTestEvaluation.filteredTests.isEmpty() ->
                EvaluationFactory.undetermined("No MMR deficiency test result", isMissingMolecularResultForEvaluation = true)

            test == null -> evaluateIhcOnly(isMmrDeficientIhcResult, isMmrProficientIhcResult)

            test.characteristics.microsatelliteStability?.isUnstable == true && isMmrProficientIhcResult ->
                EvaluationFactory.warn(
                    "Tumor is MMR proficient by IHC but MSI by molecular test",
                    inclusionEvents = inclusionMolecularEvents
                )

            test.characteristics.microsatelliteStability?.isUnstable == true ->
                evaluateMsiWithDrivers(msiGenesWithBiallelicDriver, msiGenesWithNonBiallelicDriver, inclusionMolecularEvents)

            isMmrDeficientIhcResult && test.characteristics.microsatelliteStability?.isUnstable == false ->
                EvaluationFactory.warn("Tumor is dMMR by IHC but MSS by molecular test", inclusionEvents = setOf("MMR deficient"))

            isMmrDeficientIhcResult -> EvaluationFactory.pass("dMMR by IHC", inclusionEvents = setOf("MMR deficient"))

            isMmrProficientIhcResult || test.characteristics.microsatelliteStability?.isUnstable == false ->
                EvaluationFactory.fail("Tumor is not dMMR")

            else -> evaluateUndetermined(msiGenesWithBiallelicDriver, msiGenesWithNonBiallelicDriver, msiGenesWithUnknownBiallelicDriver)
        }
    }

    private fun isMmrDeficientIhc(ihcTestEvaluation: IhcTestEvaluation) =
        ihcTestEvaluation.filteredTests.isNotEmpty() &&
                ihcTestEvaluation.filteredTests.all { it.scoreText?.lowercase() == "deficient" && !it.impliesPotentialIndeterminateStatus }

    private fun isMmrProficientIhc(ihcTestEvaluation: IhcTestEvaluation) =
        ihcTestEvaluation.filteredTests.isNotEmpty() &&
                ihcTestEvaluation.filteredTests.all { it.scoreText?.lowercase() == "proficient" && !it.impliesPotentialIndeterminateStatus }

    private fun findRelevantTest(molecularHistory: MolecularHistory): MolecularTest? {
        val tests = listOfNotNull(molecularHistory.latestOrangeMolecularRecord()) + molecularHistory.allPanels()
        return tests
            .filter { test ->
                test.characteristics.microsatelliteStability != null ||
                        GeneConstants.MMR_GENES.any { gene -> test.testsGene(gene, any("")) }
            }
            .maxByOrNull { it.date ?: LocalDate.MIN }
    }

    private fun evaluateIhcOnly(isMmrDeficientIhcResult: Boolean, isMmrProficientIhcResult: Boolean): Evaluation =
        when {
            isMmrDeficientIhcResult -> EvaluationFactory.pass("Tumor is dMMR by IHC", inclusionEvents = setOf("MMR deficient"))
            isMmrProficientIhcResult -> EvaluationFactory.fail("Tumor is not dMMR by IHC")
            else -> EvaluationFactory.undetermined("Undetermined dMMR result by IHC", isMissingMolecularResultForEvaluation = true)
        }

    private fun evaluateMsiWithDrivers(
        msiGenesWithBiallelicDriver: String,
        msiGenesWithNonBiallelicDriver: String,
        inclusionMolecularEvents: Set<String>
    ): Evaluation =
        when {
            msiGenesWithBiallelicDriver.isNotEmpty() -> EvaluationFactory.pass(
                "Tumor is MSI with biallelic driver event(s) in MMR gene(s) ($msiGenesWithBiallelicDriver)",
                inclusionEvents = inclusionMolecularEvents
            )

            msiGenesWithNonBiallelicDriver.isNotEmpty() -> EvaluationFactory.warn(
                "Tumor is MSI but with only non-biallelic driver event(s) in MMR gene(s) ($msiGenesWithNonBiallelicDriver)",
                inclusionEvents = inclusionMolecularEvents
            )

            else -> EvaluationFactory.warn(
                "Tumor is MSI but without known driver event(s) in MMR gene(s)",
                inclusionEvents = inclusionMolecularEvents
            )
        }

    private fun evaluateUndetermined(
        msiGenesWithBiallelicDriver: String,
        msiGenesWithNonBiallelicDriver: String,
        msiGenesWithUnknownBiallelicDriver: String
    ): Evaluation {
        val message = when {
            msiGenesWithBiallelicDriver.isNotEmpty() -> " but biallelic driver event(s) in MMR gene(s) ($msiGenesWithBiallelicDriver) detected"
            msiGenesWithNonBiallelicDriver.isNotEmpty() -> " but non-biallelic driver event(s) in MMR gene(s) ($msiGenesWithNonBiallelicDriver) detected"
            msiGenesWithUnknownBiallelicDriver.isNotEmpty() -> " but driver event(s) in MMR gene(s) ($msiGenesWithUnknownBiallelicDriver) detected"
            else -> ""
        }
        return EvaluationFactory.undetermined("No MSI test result$message", isMissingMolecularResultForEvaluation = true)
    }

    private fun genesFrom(vararg geneAlterations: Iterable<GeneAlteration>) =
        Format.concat(geneAlterations.asList().flatten().map(GeneAlteration::gene))
}