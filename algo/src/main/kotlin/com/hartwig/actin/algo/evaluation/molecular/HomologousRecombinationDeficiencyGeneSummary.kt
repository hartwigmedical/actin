package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType

private data class HRDDriverClassification(val isBiallelic: Boolean, val isCav: Boolean, val isHighDriver: Boolean)

data class HomologousRecombinationDeficiencyGeneSummary(
    val hrdGenesWithNonBiallelicCav: Set<String>,
    val hrdGenesWithBiallelicCav: Set<String>,
    val hrdGenesWithNonBiallelicNonCavHighDriver: Set<String>,
    val hrdGenesWithNonBiallelicNonCavNonHighDriver: Set<String>,
    val hrdGenesWithBiallelicNonCavHighDriver: Set<String>,
    val hrdGenesWithBiallelicNonCavNonHighDriver: Set<String>,
    val hrdGenesWithDeletionOrPartialDel: Set<String>,
    val hrdGenesWithHomozygousDisruption: Set<String>,
    val hrdGenesWithNonHomozygousDisruption : Set<String>
) {

    val hrdGenesWithBiallelicDriver = (hrdGenesWithBiallelicCav + hrdGenesWithBiallelicNonCavHighDriver +
            hrdGenesWithBiallelicNonCavNonHighDriver + hrdGenesWithHomozygousDisruption + hrdGenesWithDeletionOrPartialDel)
    val hrdGenesWithNonBiallelicDriver = (hrdGenesWithNonBiallelicNonCavHighDriver + hrdGenesWithNonBiallelicCav +
            hrdGenesWithNonHomozygousDisruption + hrdGenesWithNonBiallelicNonCavNonHighDriver)

    companion object {
        private val BIALLELIC_CAV = HRDDriverClassification(isBiallelic = true, isCav = true, isHighDriver = true)
        private val NON_BIALLELIC_CAV = HRDDriverClassification(isBiallelic = false, isCav = true, isHighDriver = true)
        private val BIALLELIC_NON_CAV_HIGH_DRIVER = HRDDriverClassification(isBiallelic = true, isCav = false, isHighDriver = true)
        private val BIALLELIC_NON_CAV_NON_HIGH_DRIVER =
            HRDDriverClassification(isBiallelic = true, isCav = false, isHighDriver = false)
        private val NON_BIALLELIC_NON_CAV_HIGH_DRIVER =
            HRDDriverClassification(isBiallelic = false, isCav = false, isHighDriver = true)
        private val NON_BIALLELIC_NON_CAV_NON_HIGH_DRIVER =
            HRDDriverClassification(isBiallelic = false, isCav = false, isHighDriver = false)

        fun createForDrivers(drivers: Drivers): HomologousRecombinationDeficiencyGeneSummary {
            val hrdVariantGroups = drivers.variants
                .filter { it.gene in MolecularConstants.HRD_GENES && it.isReportable && it.extendedVariantDetails != null }
                .groupBy(
                    { variant ->
                        HRDDriverClassification(
                            variant.extendedVariantDetails!!.isBiallelic,
                            variant.isCancerAssociatedVariant,
                            variant.driverLikelihood == DriverLikelihood.HIGH
                        )
                    },
                    Variant::gene
                )
                .mapValues { it.value.toSet() }

            val hrdGenesWithDeletionOrPartialDel = drivers.copyNumbers
                .filter { it.canonicalImpact.type == CopyNumberType.DEL && it.gene in MolecularConstants.HRD_GENES }
                .map(GeneAlteration::gene)
                .toSet()
            val hrdGenesWithHomozygousDisruption = drivers.homozygousDisruptions.filter { it.gene in MolecularConstants.HRD_GENES }
                .map(GeneAlteration::gene)
                .toSet()
            val hrdGenesWithNonHomozygousDisruption = drivers.disruptions
                .filter { it.gene in MolecularConstants.HRD_GENES && it.isReportable }
                .map(GeneAlteration::gene)
                .toSet()

            return HomologousRecombinationDeficiencyGeneSummary(
                hrdGenesWithNonBiallelicCav = hrdVariantGroups[NON_BIALLELIC_CAV] ?: emptySet(),
                hrdGenesWithBiallelicCav = hrdVariantGroups[BIALLELIC_CAV] ?: emptySet(),
                hrdGenesWithNonBiallelicNonCavHighDriver = hrdVariantGroups[NON_BIALLELIC_NON_CAV_HIGH_DRIVER] ?: emptySet(),
                hrdGenesWithNonBiallelicNonCavNonHighDriver = hrdVariantGroups[NON_BIALLELIC_NON_CAV_NON_HIGH_DRIVER] ?: emptySet(),
                hrdGenesWithBiallelicNonCavHighDriver = hrdVariantGroups[BIALLELIC_NON_CAV_HIGH_DRIVER] ?: emptySet(),
                hrdGenesWithBiallelicNonCavNonHighDriver = hrdVariantGroups[BIALLELIC_NON_CAV_NON_HIGH_DRIVER] ?: emptySet(),
                hrdGenesWithDeletionOrPartialDel = hrdGenesWithDeletionOrPartialDel,
                hrdGenesWithHomozygousDisruption = hrdGenesWithHomozygousDisruption,
                hrdGenesWithNonHomozygousDisruption = hrdGenesWithNonHomozygousDisruption
            )
        }
    }
}
