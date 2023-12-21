package com.hartwig.actin.clinical.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ClinicalRecordComparatorTest {

    @Test
    public void canSortClinicalRecords() {
        ClinicalRecord record1 = withPatientId("1");
        ClinicalRecord record2 = withPatientId("2");
        ClinicalRecord record3 = withPatientId("3");

        List<ClinicalRecord> records = Lists.newArrayList(record2, record3, record1);
        records.sort(new ClinicalRecordComparator());

        assertEquals(3, records.size());
        assertEquals(record1, records.get(0));
        assertEquals(record2, records.get(1));
        assertEquals(record3, records.get(2));
    }

    @NotNull
    private static ClinicalRecord withPatientId(@NotNull String patientId) {
        return ImmutableClinicalRecord.builder().from(TestClinicalFactory.createMinimalTestClinicalRecord()).patientId(patientId).build();
    }
}