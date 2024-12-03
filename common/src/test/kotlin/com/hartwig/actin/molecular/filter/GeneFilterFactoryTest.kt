package com.hartwig.actin.molecular.filter

import com.hartwig.actin.molecular.filter.GeneFilterFactory.createAlwaysValid
import com.hartwig.actin.molecular.filter.GeneFilterFactory.createFromKnownGenes
import com.hartwig.serve.datamodel.molecular.common.GeneRole
import com.hartwig.serve.datamodel.molecular.gene.ImmutableKnownGene
import com.hartwig.serve.datamodel.molecular.gene.KnownGene
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GeneFilterFactoryTest {

    @Test
    fun `Should create always valid`() {
        assertThat(createAlwaysValid()).isNotNull()
    }

    @Test
    fun `Should filter for known genes`() {
        val knownGene: KnownGene = ImmutableKnownGene.builder().gene("gene A").geneRole(GeneRole.UNKNOWN).build()
        val filter = createFromKnownGenes(setOf(knownGene))
        assertThat(filter.include("gene A")).isTrue
        assertThat(filter.include("gene B")).isFalse
    }
}