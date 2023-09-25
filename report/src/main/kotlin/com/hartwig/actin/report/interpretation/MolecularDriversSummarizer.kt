package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers

class MolecularDriversSummarizer private constructor(
    private val molecularDrivers: MolecularDrivers,
    private val evaluatedCohortsInterpreter: EvaluatedCohortsInterpreter
) {
    fun keyGenesWithVariants(): List<String> {
        return keyGenesForAlterations(molecularDrivers.variants())
    }

    fun keyAmplifiedGenes(): List<String> {
        return molecularDrivers.copyNumbers()
            .asSequence()
            .filter { it.type().isGain }
            .filter(::isKeyDriver)
            .map { it.gene() + if (it.type() == CopyNumberType.PARTIAL_GAIN) " (partial)" else "" }
            .distinct()
            .toList()
    }

    fun keyDeletedGenes(): List<String> {
        return keyGenesForAlterations(molecularDrivers.copyNumbers().filter { it.type().isLoss })
    }

    fun keyHomozygouslyDisruptedGenes(): List<String> {
        return keyGenesForAlterations(molecularDrivers.homozygousDisruptions())
    }

    fun keyFusionEvents(): List<String> {
        return molecularDrivers.fusions().filter(::isKeyDriver).map(Fusion::event).distinct()
    }

    fun keyVirusEvents(): List<String> {
        return molecularDrivers.viruses()
            .filter(::isKeyDriver)
            .map { String.format("%s (%s integrations detected)", it.type(), it.integrations()) }
            .distinct()
    }

    fun actionableEventsThatAreNotKeyDrivers(): List<String> {
        val nonDisruptionDrivers = listOf(
            molecularDrivers.variants(),
            molecularDrivers.copyNumbers(),
            molecularDrivers.fusions(),
            molecularDrivers.homozygousDisruptions(),
            molecularDrivers.viruses()
        ).flatten().filterNot(::isKeyDriver)
        return (nonDisruptionDrivers + molecularDrivers.disruptions().toList())
            .filter(evaluatedCohortsInterpreter::driverIsActionable)
            .map(Driver::event)
            .distinct()
    }

    companion object {
        fun fromMolecularDriversAndEvaluatedCohorts(
            molecularDrivers: MolecularDrivers,
            cohorts: List<EvaluatedCohort>
        ): MolecularDriversSummarizer {
            return MolecularDriversSummarizer(molecularDrivers, EvaluatedCohortsInterpreter.fromEvaluatedCohorts(cohorts))
        }

        private fun isKeyDriver(driver: Driver): Boolean {
            return driver.driverLikelihood() == DriverLikelihood.HIGH && driver.isReportable
        }

        private fun <T> keyGenesForAlterations(geneAlterationStream: Iterable<T>): List<String> where T : GeneAlteration, T : Driver {
            return geneAlterationStream.filter(::isKeyDriver).map(GeneAlteration::gene).distinct()
        }
    }
}