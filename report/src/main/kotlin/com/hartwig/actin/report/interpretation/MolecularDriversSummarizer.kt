package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.Virus
import java.util.stream.Stream

class MolecularDriversSummarizer private constructor(
    private val molecularDrivers: MolecularDrivers,
    private val evaluatedCohortsInterpreter: EvaluatedCohortsInterpreter
) {
    fun keyGenesWithVariants(): Stream<String> {
        return keyGenesForAlterations(
            molecularDrivers.variants().stream()
        )
    }

    fun keyAmplifiedGenes(): Stream<String> {
        return molecularDrivers.copyNumbers()
            .stream()
            .filter { copyNumber: CopyNumber -> copyNumber.type().isGain }
            .filter { driver: CopyNumber -> isKeyDriver(driver) }
            .map { amp: CopyNumber -> amp.gene() + if (amp.type() == CopyNumberType.PARTIAL_GAIN) " (partial)" else "" }
            .distinct()
    }

    fun keyDeletedGenes(): Stream<String> {
        return keyGenesForAlterations(molecularDrivers.copyNumbers().stream().filter { copyNumber: CopyNumber -> copyNumber.type().isLoss })
    }

    fun keyHomozygouslyDisruptedGenes(): Stream<String> {
        return keyGenesForAlterations(molecularDrivers.homozygousDisruptions().stream())
    }

    fun keyFusionEvents(): Stream<String> {
        return molecularDrivers.fusions().stream().filter { driver: Fusion -> isKeyDriver(driver) }
            .map { obj: Fusion -> obj.event() }.distinct()
    }

    fun keyVirusEvents(): Stream<String> {
        return molecularDrivers.viruses()
            .stream()
            .filter { driver: Virus -> isKeyDriver(driver) }
            .map { virus: Virus -> String.format("%s (%s integrations detected)", virus.type(), virus.integrations()) }
            .distinct()
    }

    fun actionableEventsThatAreNotKeyDrivers(): Stream<String> {
        val nonDisruptionDrivers: Stream<out Driver> = Stream.of(
            molecularDrivers.variants(),
            molecularDrivers.copyNumbers(),
            molecularDrivers.fusions(),
            molecularDrivers.homozygousDisruptions(),
            molecularDrivers.viruses()
        ).flatMap { obj: Set<Driver?> -> obj.stream() }
            .filter { driver: Driver -> !isKeyDriver(driver) }
        return Stream.concat(nonDisruptionDrivers, molecularDrivers.disruptions().stream())
            .filter { driver: Driver -> evaluatedCohortsInterpreter.driverIsActionable(driver) }
            .map { obj: Driver -> obj.event() }
            .distinct()
    }

    companion object {
        fun fromMolecularDriversAndEvaluatedCohorts(
            molecularDrivers: MolecularDrivers,
            cohorts: List<EvaluatedCohort?>
        ): MolecularDriversSummarizer {
            return MolecularDriversSummarizer(molecularDrivers, EvaluatedCohortsInterpreter(cohorts))
        }

        private fun isKeyDriver(driver: Driver): Boolean {
            return driver.driverLikelihood() == DriverLikelihood.HIGH && driver.isReportable
        }

        private fun <T> keyGenesForAlterations(geneAlterationStream: Stream<T>): Stream<String> where T : GeneAlteration?, T : Driver? {
            return geneAlterationStream.filter { driver: T -> isKeyDriver(driver) }
                .map { obj: T -> obj!!.gene() }.distinct()
        }
    }
}