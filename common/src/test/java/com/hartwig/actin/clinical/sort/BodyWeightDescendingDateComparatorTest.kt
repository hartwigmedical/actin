package com.hartwig.actin.clinical.sort

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class BodyWeightDescendingDateComparatorTest {
    @Test
    fun canSortBodyWeights() {
        val weight1: BodyWeight = builder().date(LocalDate.of(2020, 4, 4)).value(0.0).unit(Strings.EMPTY).build()
        val weight2: BodyWeight = builder().date(LocalDate.of(2020, 4, 4)).value(80.0).unit("unit 1").build()
        val weight3: BodyWeight = builder().date(LocalDate.of(2020, 4, 4)).value(80.0).unit("unit 2").build()
        val weight4: BodyWeight = builder().date(LocalDate.of(2021, 4, 4)).build()
        val weights: List<BodyWeight> = Lists.newArrayList(weight1, weight2, weight4, weight3)
        weights.sort(BodyWeightDescendingDateComparator())
        Assert.assertEquals(weight4, weights[0])
        Assert.assertEquals(weight2, weights[1])
        Assert.assertEquals(weight3, weights[2])
        Assert.assertEquals(weight1, weights[3])
    }

    companion object {
        private fun builder(): ImmutableBodyWeight.Builder {
            return ImmutableBodyWeight.builder().value(0.0).unit(Strings.EMPTY)
        }
    }
}