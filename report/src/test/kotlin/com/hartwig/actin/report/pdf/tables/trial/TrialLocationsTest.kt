package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TrialLocationsTest {

    @Test
    fun `Should display all locations when there are few locations`() {
        assertThat(TrialLocations.actinTrialLocation(emptySet(), null, setOf("1", "2"), false)).isEqualTo("1, 2")

        assertThat(
            TrialLocations.actinTrialLocation(
                emptySet(),
                TrialSource.EXAMPLE,
                setOf("Another", "Example"),
                false
            )
        ).isEqualTo("Another, Example")
    }

    @Test
    fun `Should display many-warning when there are many locations and no requesting source`() {
        assertThat(TrialLocations.actinTrialLocation(emptySet(), null, setOf("1", "2", "3", "4"), false)).isEqualTo("3+ locations")
        
        assertThat(TrialLocations.actinTrialLocation(emptySet(), null, setOf("1", "2", "3", "4"), true)).isEqualTo("3+ locations (see link)")
    }

    @Test
    fun `Should treat requesting source from multiple sources the same as single source`() {
        assertThat(
            TrialLocations.actinTrialLocation(
                setOf(TrialSource.EXAMPLE, TrialSource.NKI),
                TrialSource.EXAMPLE,
                setOf("Example", "2", "3", "4"),
                false
            )
        ).isEqualTo("Example and 3 other locations")

        assertThat(
            TrialLocations.actinTrialLocation(
                setOf(TrialSource.NKI, TrialSource.EMC),
                TrialSource.EXAMPLE,
                setOf("1", "2", "3", "4"),
                false
            )
        ).isEqualTo("3+ locations")
    }

    @Test
    fun `Should hide other locations when many locations and requesting source is present`() {
        assertThat(
            TrialLocations.actinTrialLocation(
                setOf(TrialSource.EXAMPLE),
                TrialSource.EXAMPLE,
                setOf("Example", "2", "3", "4"),
                false
            )
        ).isEqualTo("Example and 3 other locations")

        assertThat(
            TrialLocations.actinTrialLocation(
                setOf(TrialSource.EXAMPLE),
                TrialSource.EXAMPLE,
                setOf("Example", "2", "3", "4"),
                true
            )
        ).isEqualTo("Example and 3 other locations (see link)")

        assertThat(
            TrialLocations.actinTrialLocation(
                emptySet(),
                TrialSource.EXAMPLE,
                setOf("Example", "2", "3", "4"),
                true
            )
        ).isEqualTo("Example and 3 other locations (see link)")
    }
} 