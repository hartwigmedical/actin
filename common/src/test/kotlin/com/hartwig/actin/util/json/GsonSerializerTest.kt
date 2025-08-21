package com.hartwig.actin.util.json

import com.google.gson.reflect.TypeToken
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.evidence.Country
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GsonSerializerTest {

    private val gson = GsonSerializer.createBuilder().create()

    @Test
    fun `Should sort comparable sets`() {
        val variant1 = variant(DriverLikelihood.HIGH, "BRAF", "V600E", "1800")
        val variant2 = variant(DriverLikelihood.HIGH, "BRAF", "V600E", "1801")
        val variant3 = variant(DriverLikelihood.HIGH, "BRAF", "V601E", "1800")
        val variant4 = variant(DriverLikelihood.HIGH, "NTRK", "V601E", "1800")
        val variant5 = variant(DriverLikelihood.MEDIUM, "BRAF", "V600E", "1800")
        val variants = setOf(variant3, variant5, variant1, variant4, variant2)

        val deserialized = gson.fromJson<List<Variant>>(gson.toJson(variants), object : TypeToken<List<Variant>>() {}.type)
        assertThat(deserialized).isEqualTo(variants.sorted())
    }

    @Test
    fun `Should sort non-comparable sets by converting elements to string`() {
        val set = setOf(3, "2", Country.NETHERLANDS, null)
        val deserialized = gson.fromJson<List<String>>(gson.toJson(set), object : TypeToken<List<String>>() {}.type)
        assertThat(deserialized).isEqualTo(listOf("2", "3", Country.NETHERLANDS.toString(), null))
    }

    @Test
    fun `Should sort nulls last`() {
        val set = setOf(Intent.PALLIATIVE, null, Intent.ADJUVANT)
        val deserialized = gson.fromJson<List<Intent?>>(gson.toJson(set), object : TypeToken<List<Intent?>>() {}.type)
        assertThat(deserialized).isEqualTo(listOf(Intent.ADJUVANT, Intent.PALLIATIVE, null))
    }

    private fun variant(driverLikelihood: DriverLikelihood, gene: String, hgvsProteinImpact: String, hgvsCodingImpact: String): Variant {
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