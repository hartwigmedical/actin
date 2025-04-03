package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.report.pdf.assertRow
import com.hartwig.actin.report.pdf.getWrappedTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

val FIRST_TEST = TestMolecularFactory.createMinimalTestMolecularRecord().copy(date = LocalDate.of(2024, 7, 21))
val SECOND_TEST = FIRST_TEST.copy(date = FIRST_TEST.date?.plusDays(1))
val VARIANT = TestMolecularFactory.createProperVariant().copy(variantAlleleFrequency = 10.0)
val FUSION = TestMolecularFactory.createProperFusion()
private const val HIGH = "High"
private const val VAF = "VAF 10.0%"
private const val NOT_DETECTED = ""
private const val DETECTED = "Detected"

class LongitudinalMolecularHistoryGeneratorTest {

    @Test
    fun `Should create table with header with column for each test`() {
        val result = LongitudinalMolecularHistoryGenerator(MolecularHistory(listOf(FIRST_TEST, SECOND_TEST)), emptyList(), 1f)
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
        val molecularHistory = MolecularHistory(
            listOf(
                FIRST_TEST.copy(
                    drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(VARIANT))
                ), SECOND_TEST
            )
        )
        val result = LongitudinalMolecularHistoryGenerator(molecularHistory, emptyList(), 1f)
        assertRow(
            getWrappedTable(result),
            0,
            "BRAF V600E\n(Tier I)",
            "Mutation (Hotspot)\nGain of function",
            HIGH,
            VAF,
            NOT_DETECTED
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
        val molecularHistory = MolecularHistory(
            listOf(
                FIRST_TEST.copy(
                    drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                        variants = listOf(tierOneGeneTwoVariantEventTwo, tierOneGeneTwoVariant, tierTwoVariant, tierOneVariant),
                        fusions = listOf(tierOneGeneTwoLowLikelihoodFusion)
                    )
                )
            )
        )
        val result = LongitudinalMolecularHistoryGenerator(molecularHistory, emptyList(), 1f)
        assertRow(
            getWrappedTable(result),
            0,
            "BRAF V600E\n(Tier I)",
            "Mutation (Hotspot)\nGain of function",
            HIGH,
            VAF,
        )
        assertRow(
            getWrappedTable(result),
            1,
            "KRAS G12C\n(Tier I)",
            "Mutation (Hotspot)\nGain of function",
            HIGH,
            VAF,
        )
        assertRow(
            getWrappedTable(result),
            2,
            "KRAS G12D\n(Tier I)",
            "Mutation (Hotspot)\nGain of function",
            HIGH,
            VAF,
        )
        assertRow(
            getWrappedTable(result),
            3,
            "BRAF - KRAS fusion\n(Tier I)",
            "Known fusion\nGain of function",
            "Low",
            DETECTED
        )
        assertRow(
            getWrappedTable(result),
            4,
            "BRAF V600E\n(Tier II)",
            "Mutation (Hotspot)\nGain of function",
            HIGH,
            VAF,
        )
    }

    @Test
    fun `Should create row for TMB and assign value to the correct test`() {
        val molecularHistory = MolecularHistory(
            listOf(
                FIRST_TEST.copy(
                    characteristics = withTumorMutationalBurden(test = FIRST_TEST, score = 1.0)
                ),
                SECOND_TEST.copy(
                    characteristics = withTumorMutationalBurden(test = SECOND_TEST, score = 2.0)
                )
            )
        )
        val result = LongitudinalMolecularHistoryGenerator(molecularHistory, emptyList(), 1f)
        assertRow(getWrappedTable(result), 0, "TMB", "", "", "1.0", "2.0")
    }

    @Test
    fun `Should create row for MSI and assign value to the correct test`() {
        val molecularHistory = MolecularHistory(
            listOf(
                FIRST_TEST.copy(characteristics = withMicrosatelliteStability(test = FIRST_TEST, isUnstable = false)),
                SECOND_TEST.copy(characteristics = withMicrosatelliteStability(test = SECOND_TEST, isUnstable = true)),
                SECOND_TEST.copy(
                    date = SECOND_TEST.date?.plusDays(1), characteristics = TestMolecularFactory.createMinimalTestCharacteristics()
                )
            )
        )
        val result = LongitudinalMolecularHistoryGenerator(molecularHistory, emptyList(), 1f)
        assertRow(getWrappedTable(result), 1, "MSI", "", "", "Stable", "Unstable", "")
    }

    @Test
    fun `Should create row for fusion and mark as detected in correct tests`() {
        val molecularHistory = MolecularHistory(
            listOf(
                FIRST_TEST.copy(
                    drivers = TestMolecularFactory.createMinimalTestDrivers().copy(fusions = listOf(FUSION))
                ), SECOND_TEST
            )
        )
        val result = LongitudinalMolecularHistoryGenerator(molecularHistory, emptyList(), 1f)
        assertRow(
            getWrappedTable(result),
            0,
            "EML4::ALK fusion\n(Tier I)",
            "Known fusion\nGain of function",
            HIGH,
            DETECTED,
            NOT_DETECTED
        )
    }

    private fun withTumorMutationalBurden(test: MolecularRecord, score: Double): MolecularCharacteristics {
        return test.characteristics.copy(
            tumorMutationalBurden = TumorMutationalBurden(
                score = score,
                isHigh = false,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        )
    }

    private fun withMicrosatelliteStability(test: MolecularRecord, isUnstable: Boolean): MolecularCharacteristics {
        return test.characteristics.copy(
            microsatelliteStability = MicrosatelliteStability(
                microsatelliteIndelsPerMb = 0.0,
                isUnstable = isUnstable,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        )
    }
}


