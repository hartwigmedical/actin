package com.hartwig.actin;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.clinical.datamodel.treatment.Drug;
import com.hartwig.actin.clinical.datamodel.treatment.DrugType;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.Treatment;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class TestTreatmentDatabaseFactory {

    @NotNull
    public static TreatmentDatabase create() {
        Map<String, Drug> drugMap = Stream.of(drug("CAPECITABINE", DrugType.ANTIMETABOLITE, TreatmentCategory.CHEMOTHERAPY),
                        drug("OXALIPLATIN", DrugType.PLATINUM_COMPOUND, TreatmentCategory.CHEMOTHERAPY))
                .collect(Collectors.toMap(drug -> drug.name().toLowerCase(), Function.identity()));

        Treatment capox =
                ImmutableDrugTherapy.builder().name("CAPECITABINE+OXALIPLATIN").addAllDrugs(drugMap.values()).isSystemic(true).build();

        return new TreatmentDatabase(drugMap, Map.of(capox.name().toLowerCase(), capox));
    }

    @NotNull
    private static Drug drug(@NotNull String name, @NotNull DrugType drugType, @NotNull TreatmentCategory category) {
        return ImmutableDrug.builder().name(name).addDrugTypes(drugType).category(category).build();
    }
}
