package com.hartwig.actin.algo.ckb

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.algo.ckb.json.CkbDerivedMetric
import com.hartwig.actin.algo.ckb.json.CkbExtendedEvidenceTestFactory
import com.hartwig.actin.algo.ckb.json.CkbMolecularProfile
import com.hartwig.actin.algo.ckb.json.CkbTherapy
import com.hartwig.actin.algo.ckb.json.CkbVariantRequirementDetail
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.efficacy.ConfidenceInterval
import com.hartwig.actin.efficacy.DerivedMetric
import com.hartwig.actin.efficacy.TimeOfMetastases
import com.hartwig.actin.efficacy.ValuePercentage
import com.hartwig.actin.efficacy.VariantRequirement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EfficacyEntryFactoryTest {

    private val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
    private val evidenceEntryFactory = EfficacyEntryFactory(treatmentDatabase)

    @Test
    fun `Should convert minimal test extended evidence database`() {
        val result =
            evidenceEntryFactory.convertCkbExtendedEvidence(CkbExtendedEvidenceTestFactory.createMinimalTestExtendedEvidenceDatabase())
        assertThat(result).isNotNull
    }

    @Test
    fun `Should convert proper test extended evidence database`() {
        val result =
            evidenceEntryFactory.convertCkbExtendedEvidence(CkbExtendedEvidenceTestFactory.createProperTestExtendedEvidenceDatabase())
        assertThat(result).isNotNull
    }

    @Test
    fun `Should convert title to acronym`() {
        val actual = evidenceEntryFactory.extractAcronymFromTitle("Maintenance Treatment Versus Observation After Induction in Advanced Colorectal Carcinoma (CAIRO3)", "NCT000001")
        assertThat(actual).isEqualTo("CAIRO3")
    }

    @Test
    fun `Should convert title to nctId if acronym is missing`() {
        val actual = evidenceEntryFactory.extractAcronymFromTitle("Maintenance Treatment Versus Observation After Induction in Advanced Colorectal Carcinoma", "NCT000001")
        assertThat(actual).isEqualTo("NCT000001")
    }

    @Test
    fun `Should convert therapies`() {
        val ckbExtendedEvidenceEntry = CkbExtendedEvidenceTestFactory.createMinimalTestExtendedEvidenceDatabase().first()
        ckbExtendedEvidenceEntry.therapies = listOf(
            CkbTherapy(
                id = 1,
                therapyName = "Oxaliplatin + Capecitabine",
                synonyms = "CAPOX"
            )
        )
        val actual = evidenceEntryFactory.convertCkbExtendedEvidence(listOf(ckbExtendedEvidenceEntry)).first().treatments

        val expected = listOf(
            DrugTreatment(
                name = "CAPECITABINE+OXALIPLATIN",
                drugs = setOf(
                    Drug(
                        name = "CAPECITABINE",
                        drugTypes = setOf(DrugType.ANTIMETABOLITE),
                        category = TreatmentCategory.CHEMOTHERAPY,
                        displayOverride = null
                    ), Drug(name = "OXALIPLATIN", drugTypes = setOf(DrugType.PLATINUM_COMPOUND), category = TreatmentCategory.CHEMOTHERAPY)
                )
            )
        )
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should convert therapeutic setting to Intent`() {
        val actual = evidenceEntryFactory.extractTherapeuticSettingFromString("Adjuvant")
        assertThat(actual).isEqualTo(Intent.ADJUVANT)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should return exception when unknown therapeutic setting is provided`() {
        evidenceEntryFactory.extractTherapeuticSettingFromString("Unknown therapeutic setting")
    }

    @Test
    fun `Should convert variant requirements`() {
        val name = "EGFR positive"
        val requirementType = "required"
        val actual = evidenceEntryFactory.convertVariantRequirements(
            listOf(
                CkbVariantRequirementDetail(
                    molecularProfile = CkbMolecularProfile(id = 3, profileName = name),
                    requirementType = requirementType
                )
            )
        )
        val expected = listOf(
            VariantRequirement(
                name = name,
                requirementType = requirementType
            )
        )
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should use gender count when provided`() {
        val genderProvided = evidenceEntryFactory.convertGender("10", "15", "25")
        assertThat(genderProvided).isEqualTo(10)
    }

    @Test
    fun `Should derive gender count from other gender and total when count not provided`() {
        val genderNotProvided = evidenceEntryFactory.convertGender(null, "15", "25")
        assertThat(genderNotProvided).isEqualTo(10)
    }

    @Test
    fun `Should return null gender when neither count is provided`() {
        val bothGendersNotProvided = evidenceEntryFactory.convertGender(null, null, "25")
        assertThat(bothGendersNotProvided).isEqualTo(null)
    }

    @Test
    fun `Should convert primary tumor locations`() {
        val actual = evidenceEntryFactory.convertPrimaryTumorLocation("{\"right\": 45, \"left\": 136, \"both or unknown\": 2}")
        val expected = mapOf("right" to 45.0, "left" to 136.0, "both or unknown" to 2.0)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should convert primary tumor locations when provided in different format`() {
        val actual = evidenceEntryFactory.convertPrimaryTumorLocation("Colon: 292 (58.5%), Rectum: 207 (41.5%)")
        val expected = mapOf("Colon" to 292, "Rectum" to 207)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should convert metastatic sites`() {
        val actual = evidenceEntryFactory.convertMetastaticSites("Liver only: 58 (32%), Lung only: 10 (6%)")
        val expected = mapOf("Liver only" to ValuePercentage(58, 32.0), "Lung only" to ValuePercentage(10, 6.0))
        assertThat(actual).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if incorrect metastatic sites formatting`() {
        evidenceEntryFactory.convertMetastaticSites("Liver only: 58-32%, Lung only: 10-6%")
    }

    @Test
    fun `Should convert time of metastases`() {
        val actual = evidenceEntryFactory.convertTimeOfMetastases("Metachronous")
        assertThat(actual).isEqualTo(TimeOfMetastases.METACHRONOUS)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if unknown time of metastases`() {
        evidenceEntryFactory.convertMetastaticSites("Two months after primary diagnosis")
    }

    @Test
    fun `Should convert region`() {
        val actual = evidenceEntryFactory.convertRaceOrRegion("North America/Western Europe/Australia: 154, Rest of world: 354")
        val expected = mapOf("North America/Western Europe/Australia" to 154, "Rest of world" to 354)
        assertThat(actual).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception region formatting is incorrect`() {
        evidenceEntryFactory.convertRaceOrRegion("North America/Western Europe/Australia, Rest of world: 354")
    }

    @Test
    fun `Should convert race`() {
        val actual = evidenceEntryFactory.convertRaceOrRegion("\nBlack: 2,\nWhite: 26")
        val expected = mapOf("Black" to 2, "White" to 26)
        assertThat(actual).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if race formatting is incorrect`() {
        evidenceEntryFactory.convertRaceOrRegion("Black, White: 26")
    }

    @Test
    fun `Should convert confidence interval`() {
        val actual = evidenceEntryFactory.convertConfidenceInterval("4.0 - 6.8")
        val expected = ConfidenceInterval(4.0, 6.8)
        assertThat(actual).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if invalid confidence interval is provided`() {
        evidenceEntryFactory.convertConfidenceInterval("4.0;6.8")
    }

    @Test
    fun `Should use primary end point value when number is provided`() {
        val monthsEndPointType = evidenceEntryFactory.convertEndPointValue("16.0", "Months")
        assertThat(monthsEndPointType).isEqualTo(16.0)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception when primary end point value is not a number`() {
        evidenceEntryFactory.convertEndPointValue("invalid input", "Months")
    }

    @Test
    fun `Should set primary end point value to 1 when Y is provided and primary end point type is YN`() {
        val yesEndPointType = evidenceEntryFactory.convertEndPointValue("Y", "Y/N")
        assertThat(yesEndPointType).isEqualTo(1.0)
    }

    @Test
    fun `Should set primary end point value to 0 when N is provided and primary end point type is YN`() {
        val noEndPointType = evidenceEntryFactory.convertEndPointValue("N", "Y/N")
        assertThat(noEndPointType).isEqualTo(0.0)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception when primary end point type is YN and primary end point value is neither Y or N`() {
        evidenceEntryFactory.convertEndPointValue("invalid input", "Y/N")
    }

    @Test
    fun `Should return null primary end point value when primary end point value is NR`() {
        val NREndPointType = evidenceEntryFactory.convertEndPointValue("NR", "Months")
        assertThat(NREndPointType).isEqualTo(null)
    }

    @Test
    fun `Should convert derived metric`() {
        val json = CkbDerivedMetric(
            relativeMetricId = 1,
            comparatorStatistic = "16.0",
            comparatorStatisticType = "PRIMARY",
            confidenceInterval95Cs = "14.0 - 18.8",
            pValue = "0.0002"
        )
        val actual = evidenceEntryFactory.convertDerivedMetric(listOf(json))
        val expected = listOf(
            DerivedMetric(
                relativeMetricId = 1,
                value = 16.0,
                type = "PRIMARY",
                confidenceInterval = ConfidenceInterval(
                    lowerLimit = 14.0,
                    upperLimit = 18.8
                ),
                pValue = "0.0002"
            )
        )
        assertThat(actual).isEqualTo(expected)
    }
}