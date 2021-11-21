package com.hartwig.actin.treatment.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata;

import org.junit.Test;

public class CohortMetadataComparatorTest {

    @Test
    public void canSortCohortMetadata() {
        CohortMetadata metadata1 = ImmutableCohortMetadata.builder().cohortId("A").description("First").open(true).build();
        CohortMetadata metadata2 = ImmutableCohortMetadata.builder().cohortId("A").description("First").open(false).build();
        CohortMetadata metadata3 = ImmutableCohortMetadata.builder().cohortId("A").description("Second").open(true).build();
        CohortMetadata metadata4 = ImmutableCohortMetadata.builder().cohortId("B").description("Third").open(true).build();

        List<CohortMetadata> metadata = Lists.newArrayList(metadata3, metadata1, metadata4, metadata2);
        metadata.sort(new CohortMetadataComparator());

        assertEquals(metadata1, metadata.get(0));
        assertEquals(metadata2, metadata.get(1));
        assertEquals(metadata3, metadata.get(2));
        assertEquals(metadata4, metadata.get(3));
    }
}