package com.hartwig.actin.clinical.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public final class LabInterpretationFactory {

    static final Set<String> RELEVANT_LAB_NAMES =
            Sets.newHashSet("Albumin", "CKD-EPI eGFR", "Creatinine", "Hemoglobin", "Thrombocytes", "Total bilirubin");

    static final Set<String> RELEVANT_LAB_CODES = Sets.newHashSet("ALAT", "ALP", "ASAT");

    private LabInterpretationFactory() {
    }

    @NotNull
    public static LabInterpretation fromLabValues(@NotNull List<LabValue> labValues) {
        Multimap<String, LabValue> labValuesByName = ArrayListMultimap.create();
        for (String name : RELEVANT_LAB_NAMES) {
            labValuesByName.putAll(name, filterByName(labValues, name));
        }

        Multimap<String, LabValue> labValuesByCode = ArrayListMultimap.create();
        for (String code : RELEVANT_LAB_CODES) {
            labValuesByCode.putAll(code, filterByCode(labValues, code));
        }

        return new LabInterpretation(labValuesByName, labValuesByCode);
    }

    @NotNull
    private static List<LabValue> filterByName(@NotNull List<LabValue> labValues, @NotNull String name) {
        List<LabValue> filtered = Lists.newArrayList();
        for (LabValue labValue : labValues) {
            if (labValue.name().equals(name)) {
                filtered.add(labValue);
            }
        }
        return filtered;
    }

    @NotNull
    private static List<LabValue> filterByCode(@NotNull List<LabValue> labValues, @NotNull String code) {
        List<LabValue> filtered = Lists.newArrayList();
        for (LabValue labValue : labValues) {
            if (labValue.code().equals(code)) {
                filtered.add(labValue);
            }
        }
        return filtered;
    }
}
