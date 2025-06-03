package com.hartwig.actin.molecular

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val GENE = "gene 1"
private val VARIANT =
    TestVariantAlterationFactory.createVariantAlteration(GENE, GeneRole.ONCO, ProteinEffect.NO_EFFECT, false, false)


class MolecularAnnotatorFunctionsTest {

    @Test
    fun `Should annotate protein effect for frameshift in TSG`() {
        val variant = TestVariantFactory.createMinimal().copy(
            gene = GENE,
            geneRole = GeneRole.TSG,
            canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(codingEffect = CodingEffect.NONSENSE_OR_FRAMESHIFT)
        )
        val output = MolecularAnnotatorFunctions.annotateProteinEffect(variant, VARIANT)
        assertThat(output).isEqualTo(ProteinEffect.LOSS_OF_FUNCTION)
    }
}