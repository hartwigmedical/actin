package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalLoad
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import org.apache.logging.log4j.LogManager

object EvidenceRegressionReporter {

    private val LOGGER = LogManager.getLogger(EvidenceRegressionReporter::class.java)

    fun report(oldTest: MolecularTest, newTest: MolecularTest) {
        LOGGER.info("Comparing old and new molecular tests for evidence changes: ${testDescriptor(oldTest)} vs ${testDescriptor(newTest)}")

        compareDrivers(
            "Variants",
            oldTest.drivers.variants,
            newTest.drivers.variants,
            ::clearEvidence
        )
        compareDrivers(
            "CopyNumbers",
            oldTest.drivers.copyNumbers,
            newTest.drivers.copyNumbers,
            ::clearEvidence
        )
        compareDrivers(
            "HomozygousDisruptions",
            oldTest.drivers.homozygousDisruptions,
            newTest.drivers.homozygousDisruptions,
            ::clearEvidence
        )
        compareDrivers(
            "Disruptions",
            oldTest.drivers.disruptions,
            newTest.drivers.disruptions,
            ::clearEvidence
        )
        compareDrivers(
            "Fusions",
            oldTest.drivers.fusions,
            newTest.drivers.fusions,
            ::clearEvidence
        )
        compareDrivers(
            "Viruses",
            oldTest.drivers.viruses,
            newTest.drivers.viruses,
            ::clearEvidence
        )
        compareCharacteristic(
            "MicrosatelliteStability",
            oldTest.characteristics.microsatelliteStability,
            newTest.characteristics.microsatelliteStability,
            ::clearEvidence
        )
        compareCharacteristic(
            "HomologousRecombination",
            oldTest.characteristics.homologousRecombination,
            newTest.characteristics.homologousRecombination,
            ::clearEvidence
        )
        compareCharacteristic(
            "TumorMutationalBurden",
            oldTest.characteristics.tumorMutationalBurden,
            newTest.characteristics.tumorMutationalBurden,
            ::clearEvidence
        )
        compareCharacteristic(
            "TumorMutationalLoad",
            oldTest.characteristics.tumorMutationalLoad,
            newTest.characteristics.tumorMutationalLoad,
            ::clearEvidence
        )
    }

    private fun testDescriptor(test: MolecularTest): String {
        return "${test.experimentType} - ${test.date ?: "(Unknown date)"} - ${test.testTypeDisplay ?: "(No test type display)"}"
    }

    private data class EvidenceDiff<T>(
        val base: T,
        val inOld: ClinicalEvidence?,
        val inNew: ClinicalEvidence?
    )

    private fun <T> compareDrivers(
        label: String,
        oldList: List<T>,
        newList: List<T>,
        clearEvidence: (T) -> T
    ) where T : Any {
        val oldSet = oldList.toSet()
        val newSet = newList.toSet()
        val same = oldSet intersect newSet
        val onlyInOld = oldSet - same
        val onlyInNew = newSet - same

        val onlyOldByBase = onlyInOld.groupBy { clearEvidence(it) }
        val onlyNewByBase = onlyInNew.groupBy { clearEvidence(it) }
        val allBases = (onlyOldByBase.keys + onlyNewByBase.keys)

        val diffs = allBases.map { base ->
            EvidenceDiff(
                base = base,
                inOld = onlyOldByBase[base]?.map { getEvidence(it) }?.firstOrNull(),
                inNew = onlyNewByBase[base]?.map { getEvidence(it) }?.firstOrNull()
            )
        }

        val onlyInOldBases = diffs.filter { it.inNew == null && it.inOld != null }
        val onlyInNewBases = diffs.filter { it.inOld == null && it.inNew != null }
        val changedBases = diffs.filter { it.inOld != null && it.inNew != null }

        LOGGER.info("[$label] Number in old: ${oldSet.size}, new: ${newSet.size}, matching: ${same.size}")
        if (onlyInOldBases.isNotEmpty()) {
            LOGGER.info("[$label] Only in old: ${onlyInOldBases.size}")
            onlyInOldBases.forEach { diff ->
                LOGGER.info("  Base: ${diff.base}")
                LOGGER.info("    Old Evidence: ${diff.inOld}")
                LOGGER.info("    New Evidence: ${diff.inNew}")
            }
        }
        if (onlyInNewBases.isNotEmpty()) {
            LOGGER.info("[$label] Only in new: ${onlyInNewBases.size}")
            onlyInNewBases.forEach { diff ->
                LOGGER.info("  Base: ${diff.base}")
                LOGGER.info("    Old Evidence: ${diff.inOld}")
                LOGGER.info("    New Evidence: ${diff.inNew}")
            }
        }
        if (changedBases.isNotEmpty()) {
            LOGGER.info("[$label] Changed evidence: ${changedBases.size}")
            changedBases.forEach { diff ->
                LOGGER.info("  Base: ${diff.base}")
                LOGGER.info("    Old Evidence: ${diff.inOld}")
                LOGGER.info("    New Evidence: ${diff.inNew}")
                if (diff.inOld != null && diff.inNew != null && diff.inOld != diff.inNew) {
                    compareEvidence(label, diff.inOld, diff.inNew)
                }
            }
        }
    }

    private fun <T> compareCharacteristic(
        label: String,
        old: T?,
        new: T?,
        clearEvidence: (T) -> T
    ) where T : Any {
        if (old == null && new == null) {
            LOGGER.info("[$label] Both old and new are null (not present in either test)")
            return
        }
        val oldBase = old?.let { clearEvidence(it) }
        val newBase = new?.let { clearEvidence(it) }
        if (oldBase == newBase) {
            if (old != null && new != null && getEvidence(old) != getEvidence(new)) {
                LOGGER.info("[$label] Evidence changed")
                compareEvidence(label, getEvidence(old), getEvidence(new))
            } else {
                LOGGER.info("[$label] No change in base characteristic or evidence " +
                        "(treatments=${getEvidence(old).treatmentEvidence.size}, trials=${getEvidence(old).eligibleTrials.size})")
            }
        } else {
            LOGGER.info("[$label] Base characteristic changed")
            LOGGER.info("  Old: $old")
            LOGGER.info("  New: $new")
        }
    }

    private fun compareEvidence(
        label: String,
        oldEvidence: ClinicalEvidence,
        newEvidence: ClinicalEvidence
    ) {
        // Treatments
        val oldTreatments = oldEvidence.treatmentEvidence
        val newTreatments = newEvidence.treatmentEvidence
        val commonTreatments = oldTreatments.intersect(newTreatments)
        val onlyInOldTreatments = oldTreatments - newTreatments
        val onlyInNewTreatments = newTreatments - oldTreatments

        LOGGER.info("  Treatments in both (n=${commonTreatments.size})")
        LOGGER.info("  Treatments only in old (n=${onlyInOldTreatments.size}): $onlyInOldTreatments")
        LOGGER.info("  Treatments only in new (n=${onlyInNewTreatments.size}): $onlyInNewTreatments")

        // Trials
        val oldTrials = oldEvidence.eligibleTrials
        val newTrials = newEvidence.eligibleTrials
        val commonTrials = oldTrials.intersect(newTrials)
        val onlyInOldTrials = oldTrials - newTrials
        val onlyInNewTrials = newTrials - oldTrials

        LOGGER.info("  Trials in both (n=${commonTrials.size})")
        LOGGER.info("  Trials only in old (n=${onlyInOldTrials.size}): $onlyInOldTrials")
        LOGGER.info("  Trials only in new (n=${onlyInNewTrials.size}): $onlyInNewTrials")
    }

    fun clearEvidence(variant: Variant): Variant =
        variant.copy(evidence = ClinicalEvidence(emptySet(), emptySet()))

    fun clearEvidence(copyNumber: CopyNumber): CopyNumber =
        copyNumber.copy(evidence = ClinicalEvidence(emptySet(), emptySet()))

    fun clearEvidence(homozygousDisruption: HomozygousDisruption): HomozygousDisruption =
        homozygousDisruption.copy(evidence = ClinicalEvidence(emptySet(), emptySet()))

    fun clearEvidence(disruption: Disruption): Disruption =
        disruption.copy(evidence = ClinicalEvidence(emptySet(), emptySet()))

    fun clearEvidence(fusion: Fusion): Fusion =
        fusion.copy(evidence = ClinicalEvidence(emptySet(), emptySet()))

    fun clearEvidence(virus: Virus): Virus =
        virus.copy(evidence = ClinicalEvidence(emptySet(), emptySet()))

    fun clearEvidence(msi: MicrosatelliteStability): MicrosatelliteStability =
        msi.copy(evidence = ClinicalEvidence(emptySet(), emptySet()))

    fun clearEvidence(hr: HomologousRecombination): HomologousRecombination =
        hr.copy(evidence = ClinicalEvidence(emptySet(), emptySet()))

    fun clearEvidence(tmb: TumorMutationalBurden): TumorMutationalBurden =
        tmb.copy(evidence = ClinicalEvidence(emptySet(), emptySet()))

    fun clearEvidence(tml: TumorMutationalLoad): TumorMutationalLoad =
        tml.copy(evidence = ClinicalEvidence(emptySet(), emptySet()))

    @Suppress("UNCHECKED_CAST")
    private fun <T> getEvidence(obj: T): ClinicalEvidence = when (obj) {
        is Variant -> obj.evidence
        is CopyNumber -> obj.evidence
        is HomozygousDisruption -> obj.evidence
        is Disruption -> obj.evidence
        is Fusion -> obj.evidence
        is Virus -> obj.evidence
        is MicrosatelliteStability -> obj.evidence
        is HomologousRecombination -> obj.evidence
        is TumorMutationalBurden -> obj.evidence
        is TumorMutationalLoad -> obj.evidence
        else -> error("Unknown actionable type: ${obj?.javaClass}")
    }
}