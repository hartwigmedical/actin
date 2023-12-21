package com.hartwig.actin.clinical.sort;

import java.util.Comparator;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;

import org.jetbrains.annotations.NotNull;

public class ClinicalRecordComparator implements Comparator<ClinicalRecord> {

    @Override
    public int compare(@NotNull ClinicalRecord record1, @NotNull ClinicalRecord record2) {
        return record1.patientId().compareTo(record2.patientId());
    }
}
