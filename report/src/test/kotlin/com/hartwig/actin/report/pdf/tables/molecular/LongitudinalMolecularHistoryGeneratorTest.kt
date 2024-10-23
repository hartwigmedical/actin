package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.report.pdf.assertRow
import com.hartwig.actin.report.pdf.getWrappedTable
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

val FIRST_TEST = TestMolecularFactory.createMinimalTestMolecularRecord().copy(date = LocalDate.of(2024, 7, 21))
val SECOND_TEST = FIRST_TEST.copy(date = FIRST_TEST.date?.plusDays(1))
val VARIANT = TestMolecularFactory.createProperVariant().copy(variantAlleleFrequency = 10.0)
val FUSION = TestMolecularFactory.createProperFusion()

class LongitudinalMolecularHistoryGeneratorTest {

    @Test
    fun `Should create table with header with column for each test`() {
        val result = LongitudinalMolecularHistoryGenerator(MolecularHistory(listOf(FIRST_TEST, SECOND_TEST)), 1f)
        assertThat(result.title()).isEqualTo("Molecular history")
        assertRow(
            getWrappedTable(result).header,
            0,
            "Event",
            "Description",
            "Driver likelihood",
            "2024-07-21\nHartwig WGS",
            "2024-07-22\nHartwig WGS"
        )
    }

    @Test
    fun `Should create row for each variant and mark as detected in correct tests`() {
        val result = LongitudinalMolecularHistoryGenerator(
            MolecularHistory(
                listOf(
                    FIRST_TEST.copy(drivers = Drivers(variants = setOf(VARIANT))), SECOND_TEST
                )
            ), 1f
        )
        assertRow(
            getWrappedTable(result),
            0,
            "BRAF V600E\n(Tier I)",
            "Missense\nGain of function\nHotspot",
            "High",
            "Detected (VAF 10.0%)",
            "Not detected"
        )
    }

    @Test
    fun `Should sort variants by tier then gene then event`() {
        val tierOneVariant = VARIANT
        val tierTwoVariant = VARIANT.copy(evidence = TestClinicalEvidenceFactory.withOffLabelExperimentalTreatment("test"))
        val tierOneGeneTwoVariant = tierOneVariant.copy(gene = "KRAS", event = "KRAS G12C")
        val tierOneGeneTwoVariantEventTwo = tierOneVariant.copy(gene = "KRAS", event = "KRAS G12D")
        val tierOneGeneTwoLowLikelihoodFusion =
            FUSION.copy(geneStart = "BRAF", geneEnd = "KRAS", driverLikelihood = DriverLikelihood.LOW, event = "BRAF - KRAS fusion")
        val result = LongitudinalMolecularHistoryGenerator(
            MolecularHistory(
                listOf(
                    FIRST_TEST.copy(
                        drivers = Drivers(
                            variants = setOf(
                                tierOneGeneTwoVariantEventTwo,
                                tierOneGeneTwoVariant,
                                tierTwoVariant,
                                tierOneVariant
                            ).shuffled().toSet(),
                            fusions = setOf(tierOneGeneTwoLowLikelihoodFusion)
                        )
                    )
                )
            ), 1f
        )
        assertRow(
            getWrappedTable(result),
            0,
            "BRAF V600E\n(Tier I)",
            "Missense\nGain of function\nHotspot",
            "High",
            "Detected (VAF 10.0%)",
        )
        assertRow(
            getWrappedTable(result),
            1,
            "KRAS G12C\n(Tier I)",
            "Missense\nGain of function\nHotspot",
            "High",
            "Detected (VAF 10.0%)",
        )
        assertRow(
            getWrappedTable(result),
            2,
            "KRAS G12D\n(Tier I)",
            "Missense\nGain of function\nHotspot",
            "High",
            "Detected (VAF 10.0%)",
        )
        assertRow(
            getWrappedTable(result),
            3,
            "BRAF - KRAS fusion\n(Tier I)",
            "Fusion\n" +
                    "Known fusion\n" +
                    "Gain of function",
            "Low",
            "Detected"
        )
        assertRow(
            getWrappedTable(result),
            4,
            "BRAF V600E\n(Tier II)",
            "Missense\nGain of function\nHotspot",
            "High",
            "Detected (VAF 10.0%)",
        )
    }

    @Test
    fun `Should create row for TMB and assign value to the correct test`() {
        val result = LongitudinalMolecularHistoryGenerator(
            MolecularHistory(
                listOf(
                    FIRST_TEST.copy(characteristics = MolecularCharacteristics(tumorMutationalBurden = 1.0)),
                    SECOND_TEST.copy(characteristics = MolecularCharacteristics(tumorMutationalBurden = 2.0))
                )
            ), 1f
        )
        assertRow(getWrappedTable(result), 0, "TMB", "", "", "1.0", "2.0")
    }

    @Test
    fun `Should create row for MSI and assign value to the correct test`() {
        val result = LongitudinalMolecularHistoryGenerator(
            MolecularHistory(
                listOf(
                    FIRST_TEST.copy(characteristics = MolecularCharacteristics(isMicrosatelliteUnstable = false)),
                    SECOND_TEST.copy(characteristics = MolecularCharacteristics(isMicrosatelliteUnstable = true)),
                    SECOND_TEST.copy(
                        date = SECOND_TEST.date?.plusDays(1), characteristics = MolecularCharacteristics()
                    )
                )
            ), 1f
        )
        assertRow(getWrappedTable(result), 1, "MSI", "", "", "Stable", "Unstable", "")
    }

    @Test
    fun `Should create row for fusion and mark as detected in correct tests`() {
        val result = LongitudinalMolecularHistoryGenerator(
            MolecularHistory(
                listOf(
                    FIRST_TEST.copy(drivers = Drivers(fusions = setOf(FUSION))),
                    SECOND_TEST
                )
            ), 1f
        )
        assertRow(
            getWrappedTable(result),
            0,
            "EML4 - ALK fusion\n(Tier I)",
            "Fusion\n" +
                    "Known fusion\n" +
                    "Gain of function",
            "High",
            "Detected",
            "Not detected"
        )
    }
}


