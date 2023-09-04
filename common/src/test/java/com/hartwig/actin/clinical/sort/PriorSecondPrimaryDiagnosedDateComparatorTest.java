package com.hartwig.actin.clinical.sort;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.TestPriorSecondPrimaryFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class PriorSecondPrimaryDiagnosedDateComparatorTest {

    @Test
    public void shouldSortOnDiagnosedYearThenDiagnosedMonthNullsLast() {
        PriorSecondPrimary secondPrimary1 = withYearMonth(2022, 2);
        PriorSecondPrimary secondPrimary2 = withYearMonth(2022, 5);
        PriorSecondPrimary secondPrimary3 = withYearMonth(2022, null);
        PriorSecondPrimary secondPrimary4 = withYearMonth(2023, 1);
        PriorSecondPrimary secondPrimary5 = withYearMonth(null, null);

        List<PriorSecondPrimary> sorted =
                Lists.newArrayList(secondPrimary2, secondPrimary3, secondPrimary5, secondPrimary4, secondPrimary1);
        sorted.sort(new PriorSecondPrimaryDiagnosedDateComparator());

        assertThat(sorted.get(0)).isEqualTo(secondPrimary1);
        assertThat(sorted.get(1)).isEqualTo(secondPrimary2);
        assertThat(sorted.get(2)).isEqualTo(secondPrimary3);
        assertThat(sorted.get(3)).isEqualTo(secondPrimary4);
        assertThat(sorted.get(4)).isEqualTo(secondPrimary5);
    }

    @NotNull
    private static PriorSecondPrimary withYearMonth(@Nullable Integer diagnosedYear, @Nullable Integer diagnosedMonth) {
        return TestPriorSecondPrimaryFactory.builder().diagnosedYear(diagnosedYear).diagnosedMonth(diagnosedMonth).build();
    }
}