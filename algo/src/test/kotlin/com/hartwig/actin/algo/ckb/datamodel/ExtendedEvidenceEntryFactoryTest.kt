package com.hartwig.actin.algo.ckb.datamodel

import com.hartwig.actin.algo.ckb.ExtendedEvidenceEntryFactory
import com.hartwig.actin.algo.ckb.json.CkbDerivedMetric
import com.hartwig.actin.algo.ckb.json.CkbMolecularProfile
import com.hartwig.actin.algo.ckb.json.CkbVariantRequirementDetail
import org.junit.Assert.assertEquals
import org.junit.Test

class ExtendedEvidenceEntryFactoryTest {

    @Test
    fun `Can convert variant requirements`() {
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
        assertEquals(expected, actual)
    }

    @Test
    fun `Can convert gender`() {
        val genderProvided = ExtendedEvidenceEntryFactory.convertGender("10", "15", "25")
        val genderNotProvided = ExtendedEvidenceEntryFactory.convertGender(null, "15", "25")
        val bothGendersNotProvided = ExtendedEvidenceEntryFactory.convertGender(null, null, "25")
        assertEquals(10, genderProvided)
        assertEquals(10, genderNotProvided)
        assertEquals(null, bothGendersNotProvided)
    }

    @Test
    fun `Can convert primary tumor locations`() {
        val actual = ExtendedEvidenceEntryFactory.convertPrimaryTumorLocation("{\"right\": 45, \"left\": 136, \"both or unknown\": 2}")
        val expected = mapOf("right" to 45.0, "left" to 136.0, "both or unknown" to 2.0)
        assertEquals(expected, actual)
    }

    @Test
    fun `Can convert metastatic sites`() {
        val actual = ExtendedEvidenceEntryFactory.convertMetastaticSites("Liver only: 58 (32%), Lung only: 10 (6%)")
        val expected = mapOf("Liver only" to ValuePercentage(58, 32.0), "Lung only" to ValuePercentage(10, 6.0))
        assertEquals(expected, actual)
    }

    @Test
    fun `Can convert confidence interval`() {
        val actual = ExtendedEvidenceEntryFactory.convertConfidenceInterval("4.0 - 6.8")
        val expected = listOf("4.0 ", " 6.8")
        assertEquals(expected, actual)
    }

    @Test
    fun `Can convert primary end point value`() {
        val monthsEndPointType = ExtendedEvidenceEntryFactory.convertPrimaryEndPointValue("16.0", "Months")
        val yesEndPointType = ExtendedEvidenceEntryFactory.convertPrimaryEndPointValue("Y", "Y/N")
        val noEndPointType = ExtendedEvidenceEntryFactory.convertPrimaryEndPointValue("N", "Y/N")
        val NREndPointType = ExtendedEvidenceEntryFactory.convertPrimaryEndPointValue("NR", "Months")
        assertEquals(16.0, monthsEndPointType)
        assertEquals(1.0, yesEndPointType)
        assertEquals(0.0, noEndPointType)
        assertEquals(null, NREndPointType)
    }

    @Test
    fun `Can convert derived metric`() {
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
        assertEquals(expected, actual)
    }
}