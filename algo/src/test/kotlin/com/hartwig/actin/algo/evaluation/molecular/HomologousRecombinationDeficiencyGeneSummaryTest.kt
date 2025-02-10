package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HomologousRecombinationDeficiencyGeneSummaryTest {

    @Test
    fun `Should correctly classify HRD drivers`() {
        val drivers = Drivers(
            variants = listOf(
                hrdVariant(
                    gene = "BRCA1",
                    isBiallelic = true,
                    isHotspot = true,
                    driverLikelihood = DriverLikelihood.HIGH
                ),
                hrdVariant(
                    gene = "BRCA2",
                    isBiallelic = false,
                    isHotspot = true,
                    driverLikelihood = DriverLikelihood.HIGH
                ),
                hrdVariant(
                    gene = "PALB2",
                    isBiallelic = true,
                    isHotspot = false,
                    driverLikelihood = DriverLikelihood.HIGH
                ),
                hrdVariant(
                    gene = "BRCA1",
                    isBiallelic = false,
                    isHotspot = false,
                    driverLikelihood = DriverLikelihood.HIGH
                ),
                hrdVariant(
                    gene = "BRCA2",
                    isBiallelic = true,
                    isHotspot = false,
                    driverLikelihood = DriverLikelihood.MEDIUM
                ),
                hrdVariant(
                    gene = "RAD51C",
                    isBiallelic = false,
                    isHotspot = false,
                    driverLikelihood = DriverLikelihood.LOW
                ),
                hrdVariant(
                    gene = "PALB2",
                    isReportable = false,
                    isBiallelic = true,
                    isHotspot = true,
                    driverLikelihood = DriverLikelihood.HIGH
                )
            ),
            copyNumbers = listOf(
                TestCopyNumberFactory.createMinimal().copy(
                    gene = "BRCA2",
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.LOSS)
                ),
                TestCopyNumberFactory.createMinimal().copy(
                    gene = "BRCA1",
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN)
                ),
                TestCopyNumberFactory.createMinimal().copy(
                    gene = "Unmatched",
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.LOSS)
                )
            ),
            homozygousDisruptions = listOf(
                TestHomozygousDisruptionFactory.createMinimal().copy(gene = "PALB2"),
                TestHomozygousDisruptionFactory.createMinimal().copy(gene = "Unmatched")
            ),
            disruptions = listOf(
                TestDisruptionFactory.createMinimal().copy(gene = "RAD51C", isReportable = true),
                TestDisruptionFactory.createMinimal().copy(gene = "BRCA1", isReportable = false),
                TestDisruptionFactory.createMinimal().copy(gene = "Unmatched", isReportable = true)
            )
        )

        val summary = HomologousRecombinationDeficiencyGeneSummary.createForDrivers(drivers)
        assertThat(summary.hrdGenesWithNonBiallelicHotspot).containsExactlyInAnyOrder("BRCA2")
        assertThat(summary.hrdGenesWithBiallelicHotspot).containsExactlyInAnyOrder("BRCA1")
        assertThat(summary.hrdGenesWithNonBiallelicNonHotspotHighDriver).containsExactlyInAnyOrder("BRCA1")
        assertThat(summary.hrdGenesWithNonBiallelicNonHotspotNonHighDriver).containsExactlyInAnyOrder("RAD51C")
        assertThat(summary.hrdGenesWithBiallelicNonHotspotHighDriver).containsExactlyInAnyOrder("PALB2")
        assertThat(summary.hrdGenesWithBiallelicNonHotspotNonHighDriver).containsExactlyInAnyOrder("BRCA2")
        assertThat(summary.hrdGenesWithDeletionOrPartialLoss).containsExactlyInAnyOrder("BRCA2")
        assertThat(summary.hrdGenesWithHomozygousDisruption).containsExactlyInAnyOrder("PALB2")
        assertThat(summary.hrdGenesWithNonHomozygousDisruption).containsExactlyInAnyOrder("RAD51C")
    }

    private fun hrdVariant(
        gene: String,
        isBiallelic: Boolean,
        isHotspot: Boolean,
        driverLikelihood: DriverLikelihood,
        isReportable: Boolean = true
    ): Variant {
        return TestVariantFactory.createMinimal().copy(
            gene = gene,
            isReportable = isReportable,
            isHotspot = isHotspot,
            driverLikelihood = driverLikelihood,
            extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = isBiallelic)
        )
    }
}