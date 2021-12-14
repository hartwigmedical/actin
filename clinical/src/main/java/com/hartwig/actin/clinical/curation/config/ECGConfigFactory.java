package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class ECGConfigFactory implements CurationConfigFactory<ECGConfig> {

    @NotNull
    @Override
    public ECGConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        boolean isQTCF = parts[fields.get("isQTCF")].equals("1");
        Integer qtcfValue = null;
        String qtcfUnit = null;

        if (isQTCF) {
            qtcfValue = Integer.parseInt(parts[fields.get("qtcfValue")]);
            qtcfUnit = parts[fields.get("qtcfUnit")];
        }

        String interpretation = parts[fields.get("interpretation")];
        return ImmutableECGConfig.builder()
                .input(parts[fields.get("input")])
                .ignore(interpretation.equals("NULL"))
                .interpretation(interpretation)
                .isQTCF(isQTCF)
                .qtcfValue(qtcfValue)
                .qtcfUnit(qtcfUnit)
                .build();
    }
}
