package com.hartwig.actin.clinical.interpretation

import com.google.common.collect.Sets
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver.fromStringList
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver.toStringList
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class TreatmentCategoryResolverTest {
    @Test
    fun allTreatmentCategoriesCanBeConvertedBackAndForth() {
        for (category in TreatmentCategory.values()) {
            val set: Set<TreatmentCategory> = Sets.newHashSet(category)
            Assert.assertEquals(set, fromStringList(toStringList(set)))
        }
    }

    @Test
    fun canConvertCategoriesToStrings() {
        Assert.assertEquals(Strings.EMPTY, toStringList(Sets.newHashSet()))
        Assert.assertEquals("Chemotherapy", toStringList(Sets.newHashSet(TreatmentCategory.CHEMOTHERAPY)))
        Assert.assertEquals("Antiviral therapy", toStringList(Sets.newHashSet(TreatmentCategory.ANTIVIRAL_THERAPY)))
        val categories: MutableSet<TreatmentCategory> = Sets.newTreeSet()
        categories.add(TreatmentCategory.CHEMOTHERAPY)
        categories.add(TreatmentCategory.RADIOTHERAPY)
        Assert.assertEquals("Chemotherapy, Radiotherapy", toStringList(categories))
    }

    @Test
    fun canConvertStringsToCategories() {
        Assert.assertEquals(Sets.newHashSet(TreatmentCategory.ANTIVIRAL_THERAPY), fromStringList("Antiviral therapy"))
    }
}