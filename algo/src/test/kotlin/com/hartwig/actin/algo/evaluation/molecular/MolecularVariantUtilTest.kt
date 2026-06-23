package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.molecular.MolecularVariantUtil.toProteinImpact
import com.hartwig.actin.algo.evaluation.molecular.MolecularVariantUtil.variantTypesForInput
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.trial.VariantTypeInput
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MolecularVariantUtilTest {

    @Test
    fun `Should strip p dot prefix from hgvs protein impact`() {
        assertThat(toProteinImpact("p.V600E")).isEqualTo("V600E")
    }

    @Test
    fun `Should return impact unchanged when no p dot prefix`() {
        assertThat(toProteinImpact("V600E")).isEqualTo("V600E")
    }

    @Test
    fun `Should return empty string for empty input`() {
        assertThat(toProteinImpact("")).isEqualTo("")
    }

    @Test
    fun `Should map each VariantTypeInput to correct VariantType set`() {
        assertThat(variantTypesForInput(VariantTypeInput.SNV)).containsExactly(VariantType.SNV)
        assertThat(variantTypesForInput(VariantTypeInput.MNV)).containsExactly(VariantType.MNV)
        assertThat(variantTypesForInput(VariantTypeInput.INSERT)).containsExactly(VariantType.INSERT)
        assertThat(variantTypesForInput(VariantTypeInput.DELETE)).containsExactly(VariantType.DELETE)
        assertThat(variantTypesForInput(VariantTypeInput.INDEL)).containsExactlyInAnyOrder(VariantType.INSERT, VariantType.DELETE)
    }
}