package com.hartwig.actin.algo.ckb

import com.hartwig.actin.algo.ckb.datamodel.ConfidenceInterval
import com.hartwig.actin.algo.ckb.datamodel.DerivedMetric
import com.hartwig.actin.algo.ckb.datamodel.ValuePercentage
import com.hartwig.actin.algo.ckb.datamodel.VariantRequirement
import com.hartwig.actin.algo.ckb.json.CkbDerivedMetric
import com.hartwig.actin.algo.ckb.json.CkbMolecularProfile
import com.hartwig.actin.algo.ckb.json.CkbVariantRequirementDetail
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtendedEvidenceEntryFactoryTest {

    @Test
    fun `Should convert therapeutic setting to Intent`() {
        val actual = ExtendedEvidenceEntryFactory.extractTherapeuticSettingFromString("Adjuvant")
        val expected = Intent.ADJUVANT
        assertThat(actual).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should return exception when unknown therapeutic setting is provided`() {
        ExtendedEvidenceEntryFactory.extractTherapeuticSettingFromString("Unknown therapeutic setting")
    }

    @Test
    fun `Should convert variant requirements`() {
        val name = "EGFR positive"
        val requirementType = "required"
        val actual = ExtendedEvidenceEntryFactory.convertVariantRequirements(
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
        val genderProvided = ExtendedEvidenceEntryFactory.convertGender("10", "15", "25")
        assertThat(genderProvided).isEqualTo(10)
    }

    @Test
    fun `Should derive gender count from other gender and total when count not provided`() {
        val genderNotProvided = ExtendedEvidenceEntryFactory.convertGender(null, "15", "25")
        assertThat(genderNotProvided).isEqualTo(10)
    }

    @Test
    fun `Should return null gender when neither count is provided`() {
        val bothGendersNotProvided = ExtendedEvidenceEntryFactory.convertGender(null, null, "25")
        assertThat(bothGendersNotProvided).isEqualTo(null)
    }

    @Test
    fun `Should convert primary tumor locations`() {
        val actual = ExtendedEvidenceEntryFactory.convertPrimaryTumorLocation("{\"right\": 45, \"left\": 136, \"both or unknown\": 2}")
        val expected = mapOf("right" to 45.0, "left" to 136.0, "both or unknown" to 2.0)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should convert metastatic sites`() {
        val actual = ExtendedEvidenceEntryFactory.convertMetastaticSites("Liver only: 58 (32%), Lung only: 10 (6%)")
        val expected = mapOf("Liver only" to ValuePercentage(58, 32.0), "Lung only" to ValuePercentage(10, 6.0))
        assertThat(actual).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if incorrect metastatic sites formatting`() {
        ExtendedEvidenceEntryFactory.convertMetastaticSites("Liver only: 58-32%, Lung only: 10-6%")
    }

    @Test
    fun `Should convert confidence interval`() {
        val actual = ExtendedEvidenceEntryFactory.convertConfidenceInterval("4.0 - 6.8")
        val expected = ConfidenceInterval(4.0, 6.8)
        assertThat(actual).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if invalid confidence interval is provided`() {
        ExtendedEvidenceEntryFactory.convertConfidenceInterval("4.0;6.8")
    }

    @Test
    fun `Should use primary end point value when number is provided`() {
        val monthsEndPointType = ExtendedEvidenceEntryFactory.convertPrimaryEndPointValue("16.0", "Months")
        assertThat(monthsEndPointType).isEqualTo(16.0)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception when primary end point value is not a number`() {
        ExtendedEvidenceEntryFactory.convertPrimaryEndPointValue("invalid input", "Months")
    }

    @Test
    fun `Should set primary end point value to 1 when Y is provided and primary end point type is YN`() {
        val yesEndPointType = ExtendedEvidenceEntryFactory.convertPrimaryEndPointValue("Y", "Y/N")
        assertThat(yesEndPointType).isEqualTo(1.0)
    }

    @Test
    fun `Should set primary end point value to 0 when N is provided and primary end point type is YN`() {
        val noEndPointType = ExtendedEvidenceEntryFactory.convertPrimaryEndPointValue("N", "Y/N")
        assertThat(noEndPointType).isEqualTo(0.0)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception when primary end point type is YN and primary end point value is neither Y or N`() {
        ExtendedEvidenceEntryFactory.convertPrimaryEndPointValue("invalid input", "Y/N")
    }

    @Test
    fun `Should return null primary end point value when primary end point value is NR`() {
        val NREndPointType = ExtendedEvidenceEntryFactory.convertPrimaryEndPointValue("NR", "Months")
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
        val actual = ExtendedEvidenceEntryFactory.convertDerivedMetric(listOf(json))
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