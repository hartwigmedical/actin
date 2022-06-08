package com.hartwig.actin.serve.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.serve.datamodel.TestServeRecordFactory;

import org.junit.Test;

public class ServeRecordComparatorTest {

    @Test
    public void canSortServeRecords() {
        ServeRecord record1 = TestServeRecordFactory.builder().trial("trial 1").cohort(null).build();
        ServeRecord record2 = TestServeRecordFactory.builder().trial("trial 1").cohort("cohort 1").build();
        ServeRecord record3 = TestServeRecordFactory.builder().trial("trial 1").cohort("cohort 2").build();
        ServeRecord record4 = TestServeRecordFactory.builder().trial("trial 2").cohort("cohort 1").build();

        List<ServeRecord> records = Lists.newArrayList(record1, record2, record3, record4);
        records.sort(new ServeRecordComparator());

        assertEquals(record2, records.get(0));
        assertEquals(record3, records.get(1));
        assertEquals(record1, records.get(2));
        assertEquals(record4, records.get(3));
    }
}