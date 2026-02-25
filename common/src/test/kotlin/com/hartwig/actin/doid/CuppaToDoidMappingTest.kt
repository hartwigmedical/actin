package com.hartwig.actin.doid

import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CuppaToDoidMappingTest {

    private val mapping = CuppaToDoidMapping.createFromFile(
        resourceOnClasspath("cuppa_doid_mapping_test.tsv")
    )

    @Test
    fun `Should resolve known CUPPA types to DOIDs`() {
        assertThat(mapping.doidsForCuppaType("HPB: Pancreas")).containsExactly("4074")
        assertThat(mapping.doidsForCuppaType("Skin: Melanoma")).containsExactly("8923")
    }

    @Test
    fun `Should resolve CUPPA types with multiple DOIDs`() {
        assertThat(mapping.doidsForCuppaType("Esophagus/Stomach")).containsExactlyInAnyOrder("1107", "10534")
        assertThat(mapping.doidsForCuppaType("Colorectum/Small intestine/Appendix")).containsExactlyInAnyOrder("0050861", "4906", "3608")
    }

    @Test
    fun `Should return null for unknown CUPPA type`() {
        assertThat(mapping.doidsForCuppaType("Unknown type")).isNull()
    }
}
