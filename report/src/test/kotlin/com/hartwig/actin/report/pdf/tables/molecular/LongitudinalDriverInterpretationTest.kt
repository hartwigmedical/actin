package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.CodingEffect
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LongitudinalDriverInterpretationTest {

    @Test
    fun `Should interpret coding effect and protein effect when gain or loss of function`() {
        testProteinEffect(ProteinEffect.LOSS_OF_FUNCTION, "Loss of function")
        testProteinEffect(ProteinEffect.LOSS_OF_FUNCTION_PREDICTED, "Loss of function")
        testProteinEffect(ProteinEffect.GAIN_OF_FUNCTION, "Gain of function")
        testProteinEffect(ProteinEffect.GAIN_OF_FUNCTION_PREDICTED, "Gain of function")
    }

    @Test
    fun `Should interpret amplification`() {
        val result = LongitudinalDriverInterpretation.interpret(
            TestCopyNumberFactory.createMinimal().copy(type = CopyNumberType.FULL_GAIN)
        )
        assertThat(result).isEqualTo("Amplification")
    }

    @Test
    fun `Should interpret deletion`() {
        val result = LongitudinalDriverInterpretation.interpret(
            TestCopyNumberFactory.createMinimal().copy(type = CopyNumberType.LOSS)
        )
        assertThat(result).isEqualTo("Deletion")
    }

    @Test
    fun `Should interpret hotspot`() {
        val result = LongitudinalDriverInterpretation.interpret(
            TestVariantFactory.createMinimal().copy(isHotspot = true)
        )
        assertThat(result).contains("Hotspot")
    }

    @Test
    fun `Should interpret VUS`() {
        val result = LongitudinalDriverInterpretation.interpret(
            TestVariantFactory.createMinimal()
        )
        assertThat(result).contains("VUS")
    }

    private fun testProteinEffect(proteinEffect: ProteinEffect, expectedDescription: String) {
        val result = LongitudinalDriverInterpretation.interpret(
            TestVariantFactory.createMinimal().copy(
                canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(codingEffect = CodingEffect.MISSENSE),
                proteinEffect = proteinEffect
            )
        )
        assertThat(result).isEqualTo("Missense\n$expectedDescription")
    }
}