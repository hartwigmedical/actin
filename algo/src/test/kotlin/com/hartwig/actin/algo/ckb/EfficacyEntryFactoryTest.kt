package com.hartwig.actin.algo.ckb

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.algo.ckb.json.CkbDerivedMetric
import com.hartwig.actin.algo.ckb.json.CkbExtendedEvidenceTestFactory
import com.hartwig.actin.algo.ckb.json.CkbMolecularProfile
import com.hartwig.actin.algo.ckb.json.CkbTherapy
import com.hartwig.actin.algo.ckb.json.CkbVariantRequirementDetail
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
    private val extendedEvidenceEntryFactory = EfficacyEntryFactory(treatmentDatabase)

    @Test
    fun `Should convert minimal test extended evidence database`() {
        val result =
            extendedEvidenceEntryFactory.extractCkbExtendedEvidence(CkbExtendedEvidenceTestFactory.createMinimalTestExtendedEvidenceDatabase())
        assertThat(result).isNotNull
    }

    @Test
    fun `Should convert proper test extended evidence database`() {
        val result =
            extendedEvidenceEntryFactory.extractCkbExtendedEvidence(CkbExtendedEvidenceTestFactory.createProperTestExtendedEvidenceDatabase())
        assertThat(result).isNotNull
    }

    @Test
    fun `Should convert therapies`() {
        val name = "CAPECITABINE+OXALIPLATIN"
        val actual = extendedEvidenceEntryFactory.convertTherapies(
            listOf(
                CkbTherapy(
                    id = 1,
                    therapyName = name,
                    synonyms = null
                )
            )
        )
        val expected = listOf("CAPECITABINE+OXALIPLATIN")
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should convert therapeutic setting to Intent`() {
        val actual = extendedEvidenceEntryFactory.extractTherapeuticSettingFromString("Adjuvant")
        val expected = Intent.ADJUVANT
        assertThat(actual).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should return exception when unknown therapeutic setting is provided`() {
        extendedEvidenceEntryFactory.extractTherapeuticSettingFromString("Unknown therapeutic setting")
    }

    @Test
    fun `Should convert variant requirements`() {
        val name = "EGFR positive"
        val requirementType = "required"
        val actual = extendedEvidenceEntryFactory.convertVariantRequirements(
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
        val genderProvided = extendedEvidenceEntryFactory.convertGender("10", "15", "25")
        assertThat(genderProvided).isEqualTo(10)
    }

    @Test
    fun `Should derive gender count from other gender and total when count not provided`() {
        val genderNotProvided = extendedEvidenceEntryFactory.convertGender(null, "15", "25")
        assertThat(genderNotProvided).isEqualTo(10)
    }

    @Test
    fun `Should return null gender when neither count is provided`() {
        val bothGendersNotProvided = extendedEvidenceEntryFactory.convertGender(null, null, "25")
        assertThat(bothGendersNotProvided).isEqualTo(null)
    }

    @Test
    fun `Should convert primary tumor locations`() {
        val actual = extendedEvidenceEntryFactory.convertPrimaryTumorLocation("{\"right\": 45, \"left\": 136, \"both or unknown\": 2}")
        val expected = mapOf("right" to 45.0, "left" to 136.0, "both or unknown" to 2.0)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should convert primary tumor locations when provided in different format`() {
        val actual = extendedEvidenceEntryFactory.convertPrimaryTumorLocation("Colon: 292 (58.5%), Rectum: 207 (41.5%)")
        val expected = mapOf("Colon" to 292, "Rectum" to 207)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should convert metastatic sites`() {
        val actual = extendedEvidenceEntryFactory.convertMetastaticSites("Liver only: 58 (32%), Lung only: 10 (6%)")
        val expected = mapOf("Liver only" to ValuePercentage(58, 32.0), "Lung only" to ValuePercentage(10, 6.0))
        assertThat(actual).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if incorrect metastatic sites formatting`() {
        extendedEvidenceEntryFactory.convertMetastaticSites("Liver only: 58-32%, Lung only: 10-6%")
    }

    @Test
    fun `Should convert time of metastases`() {
        val actual = extendedEvidenceEntryFactory.convertTimeOfMetastases("Metachronous")
        val expected = TimeOfMetastases.METACHRONOUS
        assertThat(actual).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if unknown time of metastases`() {
        extendedEvidenceEntryFactory.convertMetastaticSites("Two months after primary diagnosis")
    }

    @Test
    fun `Should convert region`() {
        val actual = extendedEvidenceEntryFactory.convertRaceOrRegion("North America/Western Europe/Australia: 154, Rest of world: 354")
        val expected = mapOf("North America/Western Europe/Australia" to 154, "Rest of world" to 354)
        assertThat(actual).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception region formatting is incorrect`() {
        extendedEvidenceEntryFactory.convertRaceOrRegion("North America/Western Europe/Australia, Rest of world: 354")
    }

    @Test
    fun `Should convert race`() {
        val actual = extendedEvidenceEntryFactory.convertRaceOrRegion("\nBlack: 2,\nWhite: 26")
        val expected = mapOf("Black" to 2, "White" to 26)
        assertThat(actual).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if race formatting is incorrect`() {
        extendedEvidenceEntryFactory.convertRaceOrRegion("Black, White: 26")
    }

    @Test
    fun `Should convert confidence interval`() {
        val actual = extendedEvidenceEntryFactory.convertConfidenceInterval("4.0 - 6.8")
        val expected = ConfidenceInterval(4.0, 6.8)
        assertThat(actual).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if invalid confidence interval is provided`() {
        extendedEvidenceEntryFactory.convertConfidenceInterval("4.0;6.8")
    }

    @Test
    fun `Should use primary end point value when number is provided`() {
        val monthsEndPointType = extendedEvidenceEntryFactory.convertPrimaryEndPointValue("16.0", "Months")
        assertThat(monthsEndPointType).isEqualTo(16.0)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception when primary end point value is not a number`() {
        extendedEvidenceEntryFactory.convertPrimaryEndPointValue("invalid input", "Months")
    }

    @Test
    fun `Should set primary end point value to 1 when Y is provided and primary end point type is YN`() {
        val yesEndPointType = extendedEvidenceEntryFactory.convertPrimaryEndPointValue("Y", "Y/N")
        assertThat(yesEndPointType).isEqualTo(1.0)
    }

    @Test
    fun `Should set primary end point value to 0 when N is provided and primary end point type is YN`() {
        val noEndPointType = extendedEvidenceEntryFactory.convertPrimaryEndPointValue("N", "Y/N")
        assertThat(noEndPointType).isEqualTo(0.0)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception when primary end point type is YN and primary end point value is neither Y or N`() {
        extendedEvidenceEntryFactory.convertPrimaryEndPointValue("invalid input", "Y/N")
    }

    @Test
    fun `Should return null primary end point value when primary end point value is NR`() {
        val NREndPointType = extendedEvidenceEntryFactory.convertPrimaryEndPointValue("NR", "Months")
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
        val actual = extendedEvidenceEntryFactory.convertDerivedMetric(listOf(json))
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