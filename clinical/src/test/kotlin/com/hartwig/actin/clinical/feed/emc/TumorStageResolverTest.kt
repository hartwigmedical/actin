package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.datamodel.clinical.TumorStage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TumorStageResolverTest {

    @Test
    fun `Should resolve tumor stage`() {
        assertThat(TumorStageResolver.resolve("IIb")).isEqualTo(TumorStage.IIB)
        assertThat(TumorStageResolver.resolve("Not a stage")).isNull()
    }
}