package com.hartwig.actin.report.pdf.chapters

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialMatchingDetailsChapterTest {

    @Test
    fun `Can insert spaces around plus signs`() {
        assertThat(TrialMatchingDetailsChapter.insertSpacesAroundPlus("nothing to be  done here")).isEqualTo("nothing to be  done here")

        assertThat(TrialMatchingDetailsChapter.insertSpacesAroundPlus(" + ")).isEqualTo(" + ")
        assertThat(TrialMatchingDetailsChapter.insertSpacesAroundPlus("t+t")).isEqualTo("t + t")
        assertThat(TrialMatchingDetailsChapter.insertSpacesAroundPlus("1+1=2")).isEqualTo("1 + 1=2")
        assertThat(TrialMatchingDetailsChapter.insertSpacesAroundPlus("1 + 1=2")).isEqualTo("1 + 1=2")
    }
}