package com.hartwig.actin.clinical.serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.sort.ClinicalRecordComparator;
import com.hartwig.actin.util.Paths;
import com.hartwig.actin.util.json.GsonSerializer;

import org.jetbrains.annotations.NotNull;

public final class ClinicalRecordJson {

    private static final String CLINICAL_JSON_EXTENSION = ".clinical.json";

    private ClinicalRecordJson() {
    }

    public static void write(@NotNull List<ClinicalRecord> records, @NotNull String directory) throws IOException {
        String path = Paths.forceTrailingFileSeparator(directory);
        for (ClinicalRecord record : records) {
            String jsonFile = path + record.patientId() + CLINICAL_JSON_EXTENSION;

            BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile));
            writer.write(toJson(record));
            writer.close();
        }
    }

    @NotNull
    public static List<ClinicalRecord> readFromDir(@NotNull String directory) throws IOException {
        List<ClinicalRecord> records = Lists.newArrayList();
        File[] files = new File(directory).listFiles();
        if (files == null) {
            throw new IllegalArgumentException("Could not retrieve clinical json files from " + directory);
        }

        for (File file : files) {
            records.add(fromJson(Files.readString(file.toPath())));
        }

        records.sort(new ClinicalRecordComparator());

        return records;
    }

    @NotNull
    public static ClinicalRecord read(@NotNull String clinicalJson) throws IOException {
        return fromJson(Files.readString(new File(clinicalJson).toPath()));
    }

    @VisibleForTesting
    @NotNull
    static String toJson(@NotNull ClinicalRecord record) {
        return GsonSerializer.create().toJson(record);
    }

    @VisibleForTesting
    @NotNull
    static ClinicalRecord fromJson(@NotNull String json) {
        return ClinicalGsonDeserializer.create().fromJson(json, ImmutableClinicalRecord.class);
    }

}
