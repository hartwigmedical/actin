package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import org.apache.logging.log4j.LogManager

object EvidenceRegressionReporter {

    private val LOGGER = LogManager.getLogger(EvidenceRegressionReporter::class.java)

    fun report(oldTest: MolecularTest, newTest: MolecularTest) {
        LOGGER.info("Comparing old and new molecular tests for evidence changes: ${oldTest.testTypeDisplay} vs ${newTest.testTypeDisplay}")

        compareDrivers(oldTest.drivers, newTest.drivers)
//        compareCharacteristics(oldTest.characteristics, newTest.characteristics)
    }

    private fun compareDrivers(oldDrivers: Drivers, newDrivers: Drivers) {

        val oldVariants = oldDrivers.variants.toSet()
        val newVariants = newDrivers.variants.toSet()

        val same = oldVariants intersect newVariants
        val onlyInOld = oldVariants - same
        val onlyInNew = newVariants - same


        val onlyOldByBase = onlyInOld.groupBy { clearEvidence(it) }
        val onlyNewByBase = onlyInNew.groupBy { clearEvidence(it) }
        val allBases = (onlyOldByBase.keys + onlyNewByBase.keys)

        data class EvidenceDiff(
            val baseVariant: Variant,
            val inOld: List<ClinicalEvidence>,
            val inNew: List<ClinicalEvidence>
        )

        val diffs = allBases.map { base ->
            EvidenceDiff(
                baseVariant = base,
                inOld = onlyOldByBase[base]?.map { it.evidence } ?: emptyList(),
                inNew = onlyNewByBase[base]?.map { it.evidence } ?: emptyList()
            )
        }

        val onlyInOldBases = diffs.filter { it.inNew.isEmpty() && it.inOld.isNotEmpty() }
        val onlyInNewBases = diffs.filter { it.inOld.isEmpty() && it.inNew.isNotEmpty() }
        val changedBases = diffs.filter { it.inOld.isNotEmpty() && it.inNew.isNotEmpty() }

        // Log the differences
        if (onlyInOldBases.isNotEmpty()) {
            LOGGER.info("Variants only in old test: ${onlyInOldBases.map { it.baseVariant }}")
        }
        if (onlyInNewBases.isNotEmpty()) {
            LOGGER.info("Variants only in new test: ${onlyInNewBases.map { it.baseVariant }}")
        }
        if (changedBases.isNotEmpty()) {
            LOGGER.info("Variants with changed evidence:")
            changedBases.forEach { diff ->
                LOGGER.info("Variant: ${diff.baseVariant}, Old Evidence: ${diff.inOld}, New Evidence: ${diff.inNew}")
            }
        }
    }

    fun clearEvidence(variant: Variant): Variant {
        return variant.copy(evidence = ClinicalEvidence(emptySet(), emptySet()))
    }
}