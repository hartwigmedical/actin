package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
import com.hartwig.actin.clinical.datamodel.ImmutableCypInteraction
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class MedicationSelectorTest {
    @Test
    fun canFilterForActive() {
        val medications = listOf(TestMedicationFactory.builder().name("active").build())
        val filtered = MedicationTestFactory.alwaysActive().active(medications)
        Assert.assertEquals(1, filtered.size.toLong())
        Assert.assertEquals("active", filtered[0].name())
    }

    @Test
    fun canFilterOnAnyTermInName() {
        val medications = listOf(
            TestMedicationFactory.builder().name("name 1").build(),
            TestMedicationFactory.builder().name("name 1 with some extension").build(),
            TestMedicationFactory.builder().name("name 2").build(),
            TestMedicationFactory.builder().name("name 3").build()
        )
        val filtered = MedicationTestFactory.alwaysActive().activeWithAnyTermInName(medications, setOf("Name 1", "2"))
        Assert.assertEquals(3, filtered.size.toLong())
        Assert.assertNotNull(findByName(filtered, "name 1"))
        Assert.assertNotNull(findByName(filtered, "name 1 with some extension"))
        Assert.assertNotNull(findByName(filtered, "name 2"))
    }

    @Test
    fun canFilterOnOneExactCategory() {
        val medications = listOf(
            TestMedicationFactory.builder().name("no categories").build(),
            TestMedicationFactory.builder().name("wrong categories").atc(
                AtcTestFactory.atcClassificationBuilder()
                    .anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("wrong category 1").build()).build()
            ).build(),
            TestMedicationFactory.builder().name("right categories").atc(
                AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("category 1").build())
                    .build()
            ).build(),
        )

        val filtered = MedicationTestFactory.alwaysActive().active(medications)
            .filter { (allLevels(it) intersect setOf(ImmutableAtcLevel.builder().code("category 1").name("").build())).isNotEmpty() }

        Assert.assertEquals(1, filtered.size.toLong())
        Assert.assertEquals("right categories", filtered[0].name())
    }

    @Test
    fun canFilterOnAnyExactCategory() {
        val medications = listOf(
            TestMedicationFactory.builder().name("no categories").build(),
            TestMedicationFactory.builder().name("wrong categories").atc(
                AtcTestFactory.atcClassificationBuilder()
                    .anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("wrong category 1").build()).build()
            ).build(),
            TestMedicationFactory.builder().name("right category 1").atc(
                AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("category 1").build())
                    .build()
            ).build(),
            TestMedicationFactory.builder().name("right category 2").atc(
                AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("category 3").build())
                    .build()
            ).build()
        )
        val filtered = MedicationTestFactory.alwaysActive().active(medications).filter {
            (allLevels(it) intersect setOf(
                ImmutableAtcLevel.builder().code("category 1").name("").build(),
                ImmutableAtcLevel.builder().code("category 3").name("").build()
            )).isNotEmpty()
        }

        Assert.assertEquals(2, filtered.size.toLong())
        Assert.assertNotNull(findByName(medications, "right category 1"))
        Assert.assertNotNull(findByName(medications, "right category 2"))
    }

    @Test
    fun canFilterOnActiveOrRecentlyStopped() {
        val minStopDate = LocalDate.of(2019, 11, 20)
        val medications = listOf(
            TestMedicationFactory.builder().name("no categories").build(),
            TestMedicationFactory.builder().name("wrong categories").atc(
                AtcTestFactory.atcClassificationBuilder()
                    .anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("wrong category 1").build()).build()
            ).build(),
            TestMedicationFactory.builder()
                .name("right category 1 recently stopped")
                .atc(
                    AtcTestFactory.atcClassificationBuilder()
                        .anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("category 1").build()).build()
                )
                .stopDate(minStopDate.plusDays(1))
                .build(),
            TestMedicationFactory.builder()
                .name("right category 1 stopped long ago")
                .atc(
                    AtcTestFactory.atcClassificationBuilder()
                        .anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("category 1").build()).build()
                )
                .stopDate(minStopDate.minusDays(1))
                .build()
        )
        val filtered =
            MedicationTestFactory.alwaysInactive().activeOrRecentlyStopped(medications, minStopDate)
                .filter { (allLevels(it) intersect setOf(ImmutableAtcLevel.builder().code("category 1").name("").build())).isNotEmpty() }
        Assert.assertEquals(1, filtered.size.toLong())
        Assert.assertNotNull(findByName(medications, "right category 1 recently stopped"))
    }

    @Test
    fun canFilterOnActiveWithCypInteraction() {
        val medications = listOf(
            TestMedicationFactory.builder().name("no cyp interactions").build(),
            TestMedicationFactory.builder().name("uses CYP9A9 inducer").addCypInteractions(
                ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.INDUCER).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build(),
            TestMedicationFactory.builder().name("uses CYP9A9 inhibitor").addCypInteractions(
                ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.INHIBITOR).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build(),
            TestMedicationFactory.builder().name("uses different CYP inhibitor").addCypInteractions(
                ImmutableCypInteraction.builder().cyp("3A4").type(CypInteraction.Type.INHIBITOR).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build(),
        )
        val filtered = MedicationTestFactory.alwaysActive().activeWithCypInteraction(medications, "9A9", CypInteraction.Type.INHIBITOR)
        Assert.assertEquals(1, filtered.size.toLong())
        Assert.assertNotNull(findByName(medications, "uses CYP9A9 inhibitor"))
    }

    @Test
    fun canFilterOnActiveWithAnyCypInducer() {
        val medications = listOf(
            TestMedicationFactory.builder().name("uses any CYP inducer").addCypInteractions(
                ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.INDUCER).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build(),
            TestMedicationFactory.builder().name("uses any CYP inducer").addCypInteractions(
                ImmutableCypInteraction.builder().cyp("3A4").type(CypInteraction.Type.INDUCER).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build(),
            TestMedicationFactory.builder().name("uses CYP inhibitor").addCypInteractions(
                ImmutableCypInteraction.builder().cyp("3A4").type(CypInteraction.Type.INHIBITOR).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build(),
        )
        val filtered = MedicationTestFactory.alwaysActive().activeWithCypInteraction(medications, null, CypInteraction.Type.INDUCER)
        Assert.assertEquals(2, filtered.size.toLong())
        Assert.assertNotNull(findByName(medications, "uses any CYP inducer"))
    }

    @Test
    fun canFilterOnActiveOrRecentlyStoppedWithCypInteraction() {
        val minStopDate = LocalDate.of(2019, 11, 20)
        val medications = listOf(
            TestMedicationFactory.builder().name("no cyp interactions").build(),
            TestMedicationFactory.builder().name("uses CYP9A9 inducer").addCypInteractions(
                ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.INDUCER).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build(),
            TestMedicationFactory.builder().name("uses different CYP inducer").addCypInteractions(
                ImmutableCypInteraction.builder().cyp("3A4").type(CypInteraction.Type.INDUCER).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build(),
            TestMedicationFactory.builder()
                .name("CYP9A9 inhibitor recently stopped")
                .addCypInteractions(
                    ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.INHIBITOR)
                        .strength(CypInteraction.Strength.STRONG).build()
                )
                .stopDate(minStopDate.plusDays(1))
                .build(),
            TestMedicationFactory.builder()
                .name("CYP9A9 inhibitor stopped long ago")
                .addCypInteractions(
                    ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.INDUCER).strength(CypInteraction.Strength.STRONG)
                        .build()
                )
                .stopDate(minStopDate.minusDays(1))
                .build()
        )
        val filtered = MedicationTestFactory.alwaysInactive()
            .activeOrRecentlyStoppedWithCypInteraction(medications, "9A9", CypInteraction.Type.INHIBITOR, minStopDate)
        Assert.assertEquals(1, filtered.size.toLong())
        Assert.assertNotNull(findByName(medications, "CYP9A9 inhibitor recently stopped"))
    }

    companion object {
        private fun findByName(medications: List<Medication>, nameToFind: String): Medication {
            for (medication in medications) {
                if (medication.name() == nameToFind) {
                    return medication
                }
            }
            throw IllegalStateException("Could not find medication with name: $nameToFind")
        }

        private fun allLevels(it: Medication) = it.atc()?.allLevels() ?: emptySet<AtcLevel>()
    }
}