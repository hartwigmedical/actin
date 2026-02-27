package com.hartwig.actin.molecular.orange

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AminoAcidTest {

    @Test
    fun `Should convert amino acids to single letters`() {
        assertThat(AminoAcid.forceSingleLetterAminoAcids("p.Val600Glu")).isEqualTo("p.V600E")
        assertThat(AminoAcid.forceSingleLetterAminoAcids("p.V600E")).isEqualTo("p.V600E")
    }
}