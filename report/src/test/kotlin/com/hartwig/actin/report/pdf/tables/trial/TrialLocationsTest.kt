package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialLocationsTest {

    @Test
    fun `Should display all locations when count is small`() {
        assertThat(TrialLocations.actinTrialLocation(null, null, setOf("1", "2"), false)).isEqualTo("1, 2")

        assertThat(
            TrialLocations.actinTrialLocation(
                null,
                TrialSource.EXAMPLE,
                setOf("Another", "Example"),
                false
            )
        ).isEqualTo("Another, Example")
    }

    @Test
    fun `Should display many-warning when there are many trials and no requesting source`() {
        assertThat(TrialLocations.actinTrialLocation(null, null, setOf("1", "2", "3", "4"), false)).isEqualTo("3+ locations")
        
        assertThat(TrialLocations.actinTrialLocation(null, null, setOf("1", "2", "3", "4"), true)).isEqualTo("3+ locations - see link")
    }

    @Test
    fun `Should hide other locations when many locations and requesting source is present`() {
        assertThat(
            TrialLocations.actinTrialLocation(
                TrialSource.EXAMPLE,
                TrialSource.EXAMPLE,
                setOf("Example", "2", "3", "4"),
                false
            )
        ).isEqualTo("Example and 3 other locations")

        assertThat(
            TrialLocations.actinTrialLocation(
                TrialSource.EXAMPLE,
                TrialSource.EXAMPLE,
                setOf("Example", "2", "3", "4"),
                true
            )
        ).isEqualTo("Example and 3 other locations - see link")

        assertThat(
            TrialLocations.actinTrialLocation(
                null,
                TrialSource.EXAMPLE,
                setOf("Example", "2", "3", "4"),
                true
            )
        ).isEqualTo("Example and 3 other locations - see link")
    }
} 