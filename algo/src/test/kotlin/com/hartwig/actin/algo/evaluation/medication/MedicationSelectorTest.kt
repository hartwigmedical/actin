package com.hartwig.actin.algo.evaluation.medication

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
            TestMedicationFactory.builder().name("wrong categories").addCategories("wrong category 1").build(),
            TestMedicationFactory.builder().name("right categories").addCategories("category 1", "category 2").build(),
        )
        val filtered = MedicationTestFactory.alwaysActive().activeWithExactCategory(medications, "Category 1")
        Assert.assertEquals(1, filtered.size.toLong())
        Assert.assertEquals("right categories", filtered[0].name())
    }

    @Test
    fun canFilterOnAnyExactCategory() {
        val medications = listOf(
            TestMedicationFactory.builder().name("no categories").build(),
            TestMedicationFactory.builder().name("wrong categories").addCategories("wrong category 1").build(),
            TestMedicationFactory.builder().name("right category 1").addCategories("category 1", "category 2").build(),
            TestMedicationFactory.builder().name("right category 2").addCategories("category 3").build()
        )
        val filtered = MedicationTestFactory.alwaysActive().activeWithAnyExactCategory(medications, setOf("Category 1", "Category 3"))
        Assert.assertEquals(2, filtered.size.toLong())
        Assert.assertNotNull(findByName(medications, "right category 1"))
        Assert.assertNotNull(findByName(medications, "right category 2"))
    }

    @Test
    fun canFilterOnActiveOrRecentlyStopped() {
        val minStopDate = LocalDate.of(2019, 11, 20)
        val medications = listOf(
            TestMedicationFactory.builder().name("no categories").build(),
            TestMedicationFactory.builder().name("wrong categories").addCategories("wrong category 1").build(),
            TestMedicationFactory.builder()
                .name("right category 1 recently stopped")
                .addCategories("category 1")
                .stopDate(minStopDate.plusDays(1))
                .build(),
            TestMedicationFactory.builder()
                .name("right category 1 stopped long ago")
                .addCategories("category 1")
                .stopDate(minStopDate.minusDays(1))
                .build()
        )
        val filtered = MedicationTestFactory.alwaysInactive().activeOrRecentlyStoppedWithCategory(medications, "Category 1", minStopDate)
        Assert.assertEquals(1, filtered.size.toLong())
        Assert.assertNotNull(findByName(medications, "right category 1 recently stopped"))
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
    }
}