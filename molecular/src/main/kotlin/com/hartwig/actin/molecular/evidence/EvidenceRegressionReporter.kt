package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import org.apache.logging.log4j.LogManager

object EvidenceRegressionReporter {

    private val LOGGER = LogManager.getLogger(EvidenceRegressionReporter::class.java)

    fun report(oldTest: MolecularTest, newTest: MolecularTest) {
        LOGGER.info("Comparing old and new molecular tests for evidence changes: ${testDescriptor(oldTest)} vs ${testDescriptor(newTest)}")

        compareDrivers(oldTest.drivers, newTest.drivers)
//        compareCharacteristics(oldTest.characteristics, newTest.characteristics)
    }

    private fun testDescriptor(test: MolecularTest): String {
        return "${test.experimentType} - ${test.date ?: "(Unknown date)"} - ${test.testTypeDisplay ?: "(No test type display)"}"
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
        LOGGER.info("Number of variants in old test: ${oldVariants.size}")
        LOGGER.info("Number of variants in new test: ${newVariants.size}")
        LOGGER.info("Number of matching variants in both tests: ${same.size}")

        if (onlyInOldBases.isNotEmpty()) {
            LOGGER.info("Number of variants only in old test: ${onlyInOldBases.size}")
            onlyInOldBases.forEach { diff ->
                LOGGER.info("  Base Variant: ${diff.baseVariant}")
                LOGGER.info("    Old Evidence (num=${diff.inOld.size}): ${diff.inOld}")
                LOGGER.info("    New Evidence (num=${diff.inNew.size}): ${diff.inNew}")
            }
        }
        if (onlyInNewBases.isNotEmpty()) {
            LOGGER.info("Number of variants only in new test: ${onlyInNewBases.size}")
            onlyInNewBases.forEach { diff ->
                LOGGER.info("  Base Variant: ${diff.baseVariant}")
                LOGGER.info("    Old Evidence (num=${diff.inOld.size}): ${diff.inOld}")
                LOGGER.info("    New Evidence (num=${diff.inNew.size}): ${diff.inNew}")
            }
        }
        if (changedBases.isNotEmpty()) {
            LOGGER.info("Number of variants with changed evidence: ${changedBases.size}")
            changedBases.forEach { diff ->
                LOGGER.info("  Base Variant: ${diff.baseVariant}")
                LOGGER.info("    Old Evidence (num=${diff.inOld.size}): ${diff.inOld}")
                LOGGER.info("    New Evidence (num=${diff.inNew.size}): ${diff.inNew}")
            }
        }
    }

    fun clearEvidence(variant: Variant): Variant {
        return variant.copy(evidence = ClinicalEvidence(emptySet(), emptySet()))
    }
}