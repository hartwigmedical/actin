package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.datamodel.AtcClassification
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class EhrMedicationExtractorTest {

    private val atcModel = mockk<AtcModel>()
    private val qtProlongatingRiskCuration = mockk<CurationDatabase<QTProlongatingConfig>>()
    private val cypInteractionCuration = mockk<CurationDatabase<CypInteractionConfig>>()
    private val ehrPatientRecord = createEhrRecord()
    private val atcClassification = atcClassification()
    private val extractor = EhrMedicationExtractor(atcModel, qtProlongatingRiskCuration, cypInteractionCuration)
    private val ehrMedication = EhrMedication(
        name = "medication_name",
        atcCode = "atc",
        administrationRoute = "route",
        dosage = 1.0,
        dosageUnit = "unit",
        frequency = 2.0,
        frequencyUnit = "unit",
        periodBetweenDosagesValue = 3.0,
        periodBetweenDosagesUnit = "unit",
        administrationOnlyIfNeeded = false,
        startDate = LocalDate.of(2024, 2, 26),
        endDate = LocalDate.of(2024, 2, 26),
        isTrial = false,
        isSelfCare = false
    )
    private val medication = Medication(
        name = "atc_name",
        administrationRoute = "route",
        dosage = Dosage(
            dosageMin = 1.0, dosageMax = 1.0,
            dosageUnit = "unit", frequency = 2.0, frequencyUnit = "unit",
            periodBetweenValue = 3.0, periodBetweenUnit = "unit",
            ifNeeded = false
        ),
        startDate = LocalDate.of(2024, 2, 26),
        stopDate = LocalDate.of(2024, 2, 26),
        atc = atcClassification,
        qtProlongatingRisk = QTProlongatingRisk.KNOWN,
        cypInteractions = listOf(CypInteraction(CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG, "cyp_gene")),
        isTrialMedication = false,
        isSelfCare = false
    )


    @Test
    fun `Should curate QT and CYP and extract medication`() {
        every { qtProlongatingRiskCuration.find("atc_name") } returns setOf(
            QTProlongatingConfig(
                "atc_name",
                false,
                QTProlongatingRisk.KNOWN
            )
        )
        every { cypInteractionCuration.find("atc_name") } returns setOf(
            CypInteractionConfig(
                "atc_name",
                false,
                listOf(CypInteraction(CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG, "cyp_gene"))
            )
        )

        val result = extractor.extract(ehrPatientRecord)
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).containsExactly(medication)
    }

    @Test
    fun `Should default CYP and QT when no config found`() {
        every { qtProlongatingRiskCuration.find("atc_name") } returns emptySet()
        every { cypInteractionCuration.find("atc_name") } returns emptySet()
        val result = extractor.extract(ehrPatientRecord)
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).containsExactly(
            medication.copy(
                qtProlongatingRisk = QTProlongatingRisk.UNKNOWN,
                cypInteractions = emptyList()
            )
        )
    }

    private fun atcClassification(): AtcClassification {
        val atcClassification = mockk<AtcClassification>()
        every { atcClassification.chemicalSubstance } returns AtcLevel("atc_code", "atc_name")
        every { atcModel.resolveByCode("atc", "") } returns atcClassification
        return atcClassification
    }

    private fun createEhrRecord(): EhrPatientRecord {
        val ehrPatientRecord = mockk<EhrPatientRecord>()
        every { ehrPatientRecord.medications } returns listOf(ehrMedication)
        every { ehrPatientRecord.patientDetails } returns EhrPatientDetail(1980, "MALE", LocalDate.of(2024, 2, 26), "hashedId")
        return ehrPatientRecord
    }

}