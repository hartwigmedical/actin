package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.medication.MedicationSelector.Companion.SYSTEMIC_ADMINISTRATION_ROUTE_SET
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.Medication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class MedicationSelectorTest {

    @Test
    fun `Should filter for active`() {
        val medications = listOf(MedicationTestFactory.medication(name = "active"))
        val filtered = MedicationTestFactory.alwaysActive().active(medications)
        assertThat(filtered).hasSize(1)
        assertThat(filtered[0].name).isEqualTo("active")
    }

    @Test
    fun `Should filter for planned`() {
        val medications = listOf(MedicationTestFactory.medication(name = "planned"))
        val filtered = MedicationTestFactory.alwaysPlanned().planned(medications)
        assertThat(filtered.map(Medication::name)).containsExactly("planned")
    }

    @Test
    fun `Should filter on active or recently stopped`() {
        val minStopDate = LocalDate.of(2019, 11, 20)
        val medications = listOf(
            MedicationTestFactory.medication(name = "recently stopped", stopDate = minStopDate.plusDays(1)),
            MedicationTestFactory.medication(name = "stopped long ago", stopDate = minStopDate.minusDays(1))
        )
        val filtered =
            MedicationTestFactory.alwaysInactive().activeOrRecentlyStopped(medications, minStopDate)
        assertThat(filtered.map(Medication::name)).containsExactly("recently stopped")
    }

    @Test
    fun `Should filter on active with any term in name`() {
        val medications = listOf(
            MedicationTestFactory.medication(name = "name 1"),
            MedicationTestFactory.medication(name = "name 1 with some extension"),
            MedicationTestFactory.medication(name = "name 2"),
            MedicationTestFactory.medication(name = "name 3")
        )
        val filtered = MedicationTestFactory.alwaysActive().activeWithAnyTermInName(medications, setOf("Name 1", "2"))
        assertThat(filtered.map(Medication::name)).containsExactlyInAnyOrder("name 1", "name 1 with some extension", "name 2")
    }

    @Test
    fun `Should filter on planned with any term in name`() {
        val medications = listOf(
            MedicationTestFactory.medication(name = "name 1"),
            MedicationTestFactory.medication(name = "name 1 with some extension"),
            MedicationTestFactory.medication(name = "name 2"),
            MedicationTestFactory.medication(name = "name 3")
        )
        val filtered = MedicationTestFactory.alwaysPlanned().plannedWithAnyTermInName(medications, setOf("Name 1", "2"))
        assertThat(filtered.map(Medication::name)).containsExactlyInAnyOrder("name 1", "name 1 with some extension", "name 2")
    }

    @Test
    fun `Should filter on active with CYP interaction`() {
        val medications = listOf(
            MedicationTestFactory.medication(name = "no cyp interactions"),
            MedicationTestFactory.medicationWithCypInteraction(
                name = "uses CYP9A9 inducer", cyp = "9A9", type = CypInteraction.Type.INDUCER, strength = CypInteraction.Strength.STRONG
            ),
            MedicationTestFactory.medicationWithCypInteraction(
                name = "uses CYP9A9 inhibitor", cyp = "9A9", type = CypInteraction.Type.INHIBITOR, strength = CypInteraction.Strength.STRONG
            ),
            MedicationTestFactory.medicationWithCypInteraction(
                name = "uses different CYP inhibitor",
                cyp = "3A4",
                type = CypInteraction.Type.INHIBITOR,
                strength = CypInteraction.Strength.STRONG
            )
        )
        val filtered = MedicationTestFactory.alwaysActive().activeWithCypInteraction(medications, "9A9", CypInteraction.Type.INHIBITOR)
        assertThat(filtered.map(Medication::name)).containsExactly("uses CYP9A9 inhibitor")
    }

    @Test
    fun `Should filter on planned with cyp interaction`() {
        val medications = listOf(
            MedicationTestFactory.medication(name = "no cyp interactions"),
            MedicationTestFactory.medicationWithCypInteraction(
                "9A9", CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG, name = "plans to use CYP9A9 inducer"
            ),
            MedicationTestFactory.medicationWithCypInteraction(
                "9A9", CypInteraction.Type.INHIBITOR, CypInteraction.Strength.STRONG, name = "plans to use CYP9A9 inhibitor"
            ),
            MedicationTestFactory.medicationWithCypInteraction(
                "3A4", CypInteraction.Type.INHIBITOR, CypInteraction.Strength.STRONG, name = "plans to use different CYP inhibitor"
            )
        )
        val filtered = MedicationTestFactory.alwaysPlanned().plannedWithCypInteraction(medications, "9A9", CypInteraction.Type.INHIBITOR)
        assertThat(filtered.map(Medication::name)).containsExactly("plans to use CYP9A9 inhibitor")
    }

    @Test
    fun `Should filter on active with any CYP inducer`() {
        val medications = listOf(
            MedicationTestFactory.medicationWithCypInteraction(
                name = "uses a CYP inducer", cyp = "9A9", type = CypInteraction.Type.INDUCER, strength = CypInteraction.Strength.STRONG
            ),
            MedicationTestFactory.medicationWithCypInteraction(
                name = "uses other CYP inducer", cyp = "3A4", type = CypInteraction.Type.INDUCER, strength = CypInteraction.Strength.STRONG
            ),
            MedicationTestFactory.medicationWithCypInteraction(
                name = "uses CYP inhibitor", cyp = "3A4", type = CypInteraction.Type.INHIBITOR, strength = CypInteraction.Strength.STRONG
            )
        )
        val filtered = MedicationTestFactory.alwaysActive().activeWithCypInteraction(medications, null, CypInteraction.Type.INDUCER)
        assertThat(filtered.map(Medication::name)).containsExactlyInAnyOrder("uses a CYP inducer", "uses other CYP inducer")
    }

    @Test
    fun `Should filter on active or recently stopped with CYP interaction`() {
        val minStopDate = LocalDate.of(2019, 11, 20)
        val medications = listOf(
            MedicationTestFactory.medication(name = "no cyp interactions"),
            MedicationTestFactory.medicationWithCypInteraction(
                name = "uses CYP9A9 inducer", cyp = "9A9", type = CypInteraction.Type.INDUCER, strength = CypInteraction.Strength.STRONG
            ),
            MedicationTestFactory.medicationWithCypInteraction(
                name = "uses other CYP inducer", cyp = "3A4", type = CypInteraction.Type.INDUCER, strength = CypInteraction.Strength.STRONG
            ),
            MedicationTestFactory.medicationWithCypInteraction(
                name = "CYP9A9 inhibitor recently stopped",
                cyp = "9A9",
                type = CypInteraction.Type.INHIBITOR,
                strength = CypInteraction.Strength.STRONG,
                stopDate = minStopDate.plusDays(1)
            ),
            MedicationTestFactory.medicationWithCypInteraction(
                name = "CYP9A9 inhibitor stopped long ago",
                cyp = "9A9",
                type = CypInteraction.Type.INDUCER,
                strength = CypInteraction.Strength.STRONG,
                stopDate = minStopDate.minusDays(1)
            )
        )
        val filtered = MedicationTestFactory.alwaysInactive()
            .activeOrRecentlyStoppedWithCypInteraction(medications, "9A9", CypInteraction.Type.INHIBITOR, minStopDate)
        assertThat(filtered.map(Medication::name)).containsExactly("CYP9A9 inhibitor recently stopped")
    }

    @Test
    fun `Should correctly check systemic status`() {
        val medications = listOf(
            MedicationTestFactory.medication(name = "systemic", administrationRoute = SYSTEMIC_ADMINISTRATION_ROUTE_SET.iterator().next()),
            MedicationTestFactory.medication(name = "non-systemic", administrationRoute = "non-systemic route"),
        )
        val filtered = medications.filter { MedicationTestFactory.alwaysActive().isSystemic(it) }
        assertThat(filtered).hasSize(1)
        assertThat(filtered[0].name).isEqualTo("systemic")
    }

    @Test
    fun `Should correctly check systemic plus active status`() {
        val medications = listOf(
            MedicationTestFactory.medication(
                name = "systemic and active", administrationRoute = SYSTEMIC_ADMINISTRATION_ROUTE_SET.iterator().next()
            ),
        )
        val filtered = medications.filter { MedicationTestFactory.alwaysActive().isSystemicAndActive(it) }
        assertThat(filtered).hasSize(1)
        assertThat(filtered[0].name).isEqualTo("systemic and active")
    }

    @Test
    fun `Should correctly check systemic plus planned status`() {
        val medications = listOf(
            MedicationTestFactory.medication(
                name = "systemic and planned", administrationRoute = SYSTEMIC_ADMINISTRATION_ROUTE_SET.iterator().next()
            ),
        )
        val filtered = medications.filter { MedicationTestFactory.alwaysPlanned().isSystemicAndPlanned(it) }
        assertThat(filtered).hasSize(1)
        assertThat(filtered[0].name).isEqualTo("systemic and planned")
    }
}