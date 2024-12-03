package com.hartwig.actin.util.json

import com.google.gson.reflect.TypeToken
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.orange.driver.ExtendedVariantDetails
import com.hartwig.actin.datamodel.molecular.sort.driver.VariantComparator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GsonSerializerTest {

    private val gson = GsonSerializer.create()

    @Test
    fun `Should sort comparable sets with given comparator`() {
        val variant1 = variant(DriverLikelihood.HIGH, "BRAF", "V600E", "1800")
        val variant2 = variant(DriverLikelihood.HIGH, "BRAF", "V600E", "1801")
        val variant3 = variant(DriverLikelihood.HIGH, "BRAF", "V601E", "1800")
        val variant4 = variant(DriverLikelihood.HIGH, "NTRK", "V601E", "1800")
        val variant5 = variant(DriverLikelihood.MEDIUM, "BRAF", "V600E", "1800")
        val variants = setOf(variant3, variant5, variant1, variant4, variant2)

        val deserialized = gson.fromJson<List<ExtendedVariantDetails>>(gson.toJson(variants), object : TypeToken<List<Variant>>() {}.type)
        assertThat(deserialized).isEqualTo(variants.sortedWith(VariantComparator()))
    }

    @Test
    fun `Should sort non-comparable sets by converting elements to string`() {
        val set = setOf(3, "2", CountryName.NETHERLANDS, null)
        val deserialized = gson.fromJson<List<String>>(gson.toJson(set), object : TypeToken<List<String>>() {}.type)
        assertThat(deserialized).isEqualTo(listOf("2", "3", CountryName.NETHERLANDS.toString(), null))
    }

    @Test
    fun `Should sort nulls last`() {
        val set = setOf(Intent.PALLIATIVE, null, Intent.ADJUVANT)
        val deserialized = gson.fromJson<List<Intent?>>(gson.toJson(set), object : TypeToken<List<Intent?>>() {}.type)
        assertThat(deserialized).isEqualTo(listOf(Intent.ADJUVANT, Intent.PALLIATIVE, null))
    }

    private fun variant(
        driverLikelihood: DriverLikelihood, gene: String, hgvsProteinImpact: String, hgvsCodingImpact: String
    ): Variant {
        val canonicalImpact: TranscriptVariantImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(
            hgvsProteinImpact = hgvsProteinImpact,
            hgvsCodingImpact = hgvsCodingImpact
        )
        return TestVariantFactory.createMinimal().copy(
            driverLikelihood = driverLikelihood,
            gene = gene,
            canonicalImpact = canonicalImpact
        )
    }
}