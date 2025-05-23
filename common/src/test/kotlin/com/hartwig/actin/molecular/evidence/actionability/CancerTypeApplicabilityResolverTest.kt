package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.evidence.CancerTypeMatchApplicability
import com.hartwig.serve.datamodel.common.ImmutableCancerType
import com.hartwig.serve.datamodel.common.ImmutableIndication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CancerTypeApplicabilityResolverTest {

    val resolver = CancerTypeApplicabilityResolver(setOf("1", "2", "3"))

    @Test
    fun `Should resolve indication to specific cancer type when match to doids`() {
        assertThat(
            resolver.resolve(
                indication("2")
            )
        ).isEqualTo(CancerTypeMatchApplicability.SPECIFIC_TYPE)
    }

    @Test
    fun `Should resolve indication to all cancer types when doid is all cancers`() {
        assertThat(
            resolver.resolve(
                indication("162")
            )
        ).isEqualTo(CancerTypeMatchApplicability.ALL_TYPES)
    }

    @Test
    fun `Should resolve indication to other cancer type when no match to doids`() {
        assertThat(
            resolver.resolve(
                indication("4")
            )
        ).isEqualTo(CancerTypeMatchApplicability.OTHER_TYPE)
    }

    private fun indication(doid: String) =
        ImmutableIndication.builder().applicableType(ImmutableCancerType.builder().name("cancer type").doid(doid).build()).build()
}