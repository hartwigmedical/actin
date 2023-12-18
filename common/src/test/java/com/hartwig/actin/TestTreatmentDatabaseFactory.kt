package com.hartwig.actin;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.clinical.datamodel.treatment.Drug;
import com.hartwig.actin.clinical.datamodel.treatment.DrugType;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableRadiotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.Treatment;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public final class TestTreatmentDatabaseFactory {

    public static final String CAPECITABINE_OXALIPLATIN = "CAPECITABINE+OXALIPLATIN";
    public static final String RADIOTHERAPY = "RADIOTHERAPY";
    public static final String ABLATION = "ABLATION";

    @NotNull
    public static TreatmentDatabase createProper() {
        Map<String, Drug> drugMap =
                Stream.of(chemoDrug("CAPECITABINE", DrugType.ANTIMETABOLITE), chemoDrug("OXALIPLATIN", DrugType.PLATINUM_COMPOUND))
                .collect(Collectors.toMap(drug -> drug.name().toLowerCase(), Function.identity()));

        Treatment capox =
                ImmutableDrugTreatment.builder().name(CAPECITABINE_OXALIPLATIN).addAllDrugs(drugMap.values()).isSystemic(true).build();
        Treatment radiotherapy = ImmutableRadiotherapy.builder().name(RADIOTHERAPY).build();
        Treatment ablation =
                ImmutableOtherTreatment.builder().name(ABLATION).isSystemic(false).addCategories(TreatmentCategory.ABLATION).build();

        Map<String, Treatment> treatmentMap = Stream.of(capox, radiotherapy, ablation)
                .collect(Collectors.toMap(treatment -> treatment.name().toLowerCase(), Function.identity()));

        return new TreatmentDatabase(drugMap, treatmentMap);
    }

    @NotNull
    private static Drug chemoDrug(@NotNull String name, @NotNull DrugType drugType) {
        return ImmutableDrug.builder().name(name).addDrugTypes(drugType).category(TreatmentCategory.CHEMOTHERAPY).build();
    }
}
