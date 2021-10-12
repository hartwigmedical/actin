package com.hartwig.actin.datamodel;

import java.util.List;

import com.hartwig.actin.datamodel.clinical.ClinicalRecord;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClinicalModel {

    @NotNull
    private final List<ClinicalRecord> records;

    public ClinicalModel(@NotNull final List<ClinicalRecord> records) {
        this.records = records;
    }

    @NotNull
    public List<ClinicalRecord> records() {
        return records;
    }

    @Nullable
    public ClinicalRecord findClinicalRecordForSample(@NotNull String sampleId) {
        for (ClinicalRecord record : records) {
            if (record.sampleId().equals(sampleId)) {
                return record;
            }
        }

        return null;
    }
}
