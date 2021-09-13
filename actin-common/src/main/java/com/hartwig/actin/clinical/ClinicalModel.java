package com.hartwig.actin.clinical;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClinicalModel {

    @NotNull
    private final List<ClinicalRecord> records;

    ClinicalModel(@NotNull final List<ClinicalRecord> records) {
        this.records = records;
    }

    // TODO Implement properly and use.
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
