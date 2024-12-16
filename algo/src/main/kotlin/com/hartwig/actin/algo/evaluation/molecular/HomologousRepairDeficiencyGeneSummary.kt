package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.GeneAlteration
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType

private data class HRDDriverClassification(val isBiallelic: Boolean, val isHotspot: Boolean, val isHighDriver: Boolean)

data class HomologousRepairDeficiencyGeneSummary(
    val hrdGenesWithNonBiallelicHotspot: Set<String>,
    val hrdGenesWithBiallelicHotspot: Set<String>,
    val hrdGenesWithNonBiallelicNonHotspotHighDriver: Set<String>,
    val hrdGenesWithNonBiallelicNonHotspotNonHighDriver: Set<String>,
    val hrdGenesWithBiallelicNonHotspotHighDriver: Set<String>,
    val hrdGenesWithBiallelicNonHotspotNonHighDriver: Set<String>,
    val hrdGenesWithDeletionOrPartialLoss: Set<String>,
    val hrdGenesWithHomozygousDisruption: Set<String>,
    val hrdGenesWithNonHomozygousDisruption : Set<String>
) {

    val hrdGenesWithBiallelicDriver = (hrdGenesWithBiallelicHotspot + hrdGenesWithBiallelicNonHotspotHighDriver +
            hrdGenesWithBiallelicNonHotspotNonHighDriver + hrdGenesWithHomozygousDisruption + hrdGenesWithDeletionOrPartialLoss)
    val hrdGenesWithNonBiallelicDriver = (hrdGenesWithNonBiallelicNonHotspotHighDriver + hrdGenesWithNonBiallelicHotspot +
            hrdGenesWithNonHomozygousDisruption + hrdGenesWithNonBiallelicNonHotspotNonHighDriver)

    companion object {
        private val BIALLELIC_HOTSPOT = HRDDriverClassification(isBiallelic = true, isHotspot = true, isHighDriver = true)
        private val NON_BIALLELIC_HOTSPOT = HRDDriverClassification(isBiallelic = false, isHotspot = true, isHighDriver = true)
        private val BIALLELIC_NON_HOTSPOT_HIGH_DRIVER = HRDDriverClassification(isBiallelic = true, isHotspot = false, isHighDriver = true)
        private val BIALLELIC_NON_HOTSPOT_NON_HIGH_DRIVER =
            HRDDriverClassification(isBiallelic = true, isHotspot = false, isHighDriver = false)
        private val NON_BIALLELIC_NON_HOTSPOT_HIGH_DRIVER =
            HRDDriverClassification(isBiallelic = false, isHotspot = false, isHighDriver = true)
        private val NON_BIALLELIC_NON_HOTSPOT_NON_HIGH_DRIVER =
            HRDDriverClassification(isBiallelic = false, isHotspot = false, isHighDriver = false)

        fun createForDrivers(drivers: Drivers): HomologousRepairDeficiencyGeneSummary {
            val hrdVariantGroups = drivers.variants
                .filter { it.gene in MolecularConstants.HRD_GENES && it.isReportable && it.extendedVariantDetails != null }
                .groupBy(
                    { variant ->
                        HRDDriverClassification(
                            variant.extendedVariantDetails!!.isBiallelic,
                            variant.isHotspot,
                            variant.driverLikelihood == DriverLikelihood.HIGH
                        )
                    },
                    Variant::gene
                )
                .mapValues { it.value.toSet() }

            val hrdGenesWithDeletionOrPartialLoss = drivers.copyNumbers
                .filter { it.canonicalImpact.type == CopyNumberType.LOSS && it.gene in MolecularConstants.HRD_GENES }
                .map(GeneAlteration::gene)
                .toSet()
            val hrdGenesWithHomozygousDisruption = drivers.homozygousDisruptions.filter { it.gene in MolecularConstants.HRD_GENES }
                .map(GeneAlteration::gene)
                .toSet()
            val hrdGenesWithNonHomozygousDisruption = drivers.disruptions
                .filter { it.gene in MolecularConstants.HRD_GENES && it.isReportable }
                .map(GeneAlteration::gene)
                .toSet()

            return HomologousRepairDeficiencyGeneSummary(
                hrdGenesWithNonBiallelicHotspot = hrdVariantGroups[NON_BIALLELIC_HOTSPOT] ?: emptySet(),
                hrdGenesWithBiallelicHotspot = hrdVariantGroups[BIALLELIC_HOTSPOT] ?: emptySet(),
                hrdGenesWithNonBiallelicNonHotspotHighDriver = hrdVariantGroups[NON_BIALLELIC_NON_HOTSPOT_HIGH_DRIVER] ?: emptySet(),
                hrdGenesWithNonBiallelicNonHotspotNonHighDriver = hrdVariantGroups[NON_BIALLELIC_NON_HOTSPOT_NON_HIGH_DRIVER] ?: emptySet(),
                hrdGenesWithBiallelicNonHotspotHighDriver = hrdVariantGroups[BIALLELIC_NON_HOTSPOT_HIGH_DRIVER] ?: emptySet(),
                hrdGenesWithBiallelicNonHotspotNonHighDriver = hrdVariantGroups[BIALLELIC_NON_HOTSPOT_NON_HIGH_DRIVER] ?: emptySet(),
                hrdGenesWithDeletionOrPartialLoss = hrdGenesWithDeletionOrPartialLoss,
                hrdGenesWithHomozygousDisruption = hrdGenesWithHomozygousDisruption,
                hrdGenesWithNonHomozygousDisruption = hrdGenesWithNonHomozygousDisruption
            )
        }
    }
}
