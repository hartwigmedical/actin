package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class MolecularTestConfigFactory implements CurationConfigFactory<MolecularTestConfig> {

    @NotNull
    @Override
    public MolecularTestConfig create(@NotNull final Map<String, Integer> fields, @NotNull final String[] parts) {
        boolean ignore = CurationUtil.isIgnoreString(parts[fields.get("test")]);
        return ImmutableMolecularTestConfig.builder()
                .input(parts[fields.get("input")])
                .ignore(ignore)
                .curated(!ignore ? curateObject(fields, parts) : null)
                .build();
    }

    @NotNull
    private static PriorMolecularTest curateObject(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutablePriorMolecularTest.builder()
                .test(parts[fields.get("test")])
                .item(parts[fields.get("item")])
                .measure(ResourceFile.optionalString(parts[fields.get("measure")]))
                .scoreText(ResourceFile.optionalString(parts[fields.get("scoreText")]))
                .scoreValuePrefix(ResourceFile.optionalString(parts[fields.get("scoreValuePrefix")]))
                .scoreValue(ResourceFile.optionalNumber(parts[fields.get("scoreValue")]))
                .scoreValueUnit(ResourceFile.optionalString(parts[fields.get("scoreValueUnit")]))
                .build();
    }
}
