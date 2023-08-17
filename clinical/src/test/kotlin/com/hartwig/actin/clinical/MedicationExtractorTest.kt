package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.ANATOMICAL
import com.hartwig.actin.clinical.curation.ATC_CODE
import com.hartwig.actin.clinical.curation.CHEMICAL
import com.hartwig.actin.clinical.curation.CHEMICAL_SUBSTANCE
import com.hartwig.actin.clinical.curation.PHARMACOLOGICAL
import com.hartwig.actin.clinical.curation.THERAPEUTIC
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.ImmutableAtcClassification
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
import com.hartwig.actin.clinical.datamodel.ImmutableDosage
import com.hartwig.actin.clinical.datamodel.ImmutableMedication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import com.hartwig.actin.clinical.feed.TestFeedFactory.medicationEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class MedicationExtractorTest {

    @Test
    fun `should extract all medication fields`() {
        val entry = medicationEntry(
            status = "active",
            dosageInstruction = "once per day 50-60 mg every month",
            start = LocalDate.of(2019, 2, 2),
            end = LocalDate.of(2019, 4, 4),
            active = true,
            code5ATCCode = ATC_CODE,
            code5ATCDisplay = "PARACETAMOL",
            administrationRoute = "oraal"
        )

        assertThat(EXTRACTOR.extractMedication(entry)).isEqualTo(ImmutableMedication.builder()
            .name("Paracetamol")
            .status(MedicationStatus.ACTIVE)
            .administrationRoute("oral")
            .dosage(ImmutableDosage.builder()
                .dosageMin(50.0)
                .dosageMax(60.0)
                .dosageUnit("mg")
                .frequency(1.0)
                .frequencyUnit("day")
                .periodBetweenValue(1.0)
                .periodBetweenUnit("mo")
                .ifNeeded(false)
                .build())
            .startDate(LocalDate.of(2019, 2, 2))
            .stopDate(LocalDate.of(2019, 4, 4))
            .addCypInteractions(TestCurationFactory.createTestCypInteraction())
            .qtProlongatingRisk(QTProlongatingRisk.POSSIBLE)
            .atc(ImmutableAtcClassification.builder()
                .anatomicalMainGroup(atcLevel("N", ANATOMICAL))
                .therapeuticSubGroup(atcLevel("N02", THERAPEUTIC))
                .pharmacologicalSubGroup(atcLevel("N02B", PHARMACOLOGICAL))
                .chemicalSubGroup(atcLevel("N02BE", CHEMICAL))
                .chemicalSubstance(atcLevel(ATC_CODE, CHEMICAL_SUBSTANCE))
                .build())
            .isSelfCare(false)
            .isTrialMedication(false)
            .build()
        )
    }

    @Test
    fun `should return null for entries with no ATC display or code text`() {
        val entry = medicationEntry(
            status = "",
            dosageInstruction = "Irrelevant",
            start = LocalDate.of(2019, 2, 2),
            end = LocalDate.of(2019, 4, 4),
            active = false,
        )
        assertThat(EXTRACTOR.extractMedication(entry)).isNull()
    }

    @Test
    fun `should interpret as self-care when ATC code and display are empty`() {
        val entry = medicationEntry(
            status = "",
            dosageInstruction = "",
            start = LocalDate.of(2019, 2, 2),
            end = LocalDate.of(2019, 4, 4),
            active = false,
            codeText = "A en B"
        )

        assertThat(EXTRACTOR.extractMedication(entry)!!.isSelfCare).isTrue
    }

    @Test
    fun `should interpret as trial medication when ATC display is empty and ATC code does not start with letter`() {
        val entry = medicationEntry(
            status = "",
            dosageInstruction = "",
            start = LocalDate.of(2019, 2, 2),
            end = LocalDate.of(2019, 4, 4),
            active = false,
            code5ATCCode = "123",
            codeText = "A en B"
        )

        assertThat(EXTRACTOR.extractMedication(entry)!!.isTrialMedication).isTrue
    }

    companion object {
        private val EXTRACTOR = MedicationExtractor(
            TestCurationFactory.createProperTestCurationModel(),
            TestAtcFactory.createProperAtcModel()
        )

        private fun atcLevel(code: String, name: String): AtcLevel = ImmutableAtcLevel.builder().code(code).name(name).build()
    }
}