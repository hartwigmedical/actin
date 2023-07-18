package com.hartwig.actin.treatment.input.datamodel;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.hartwig.actin.clinical.datamodel.treatment.DrugType;
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatmentType;
import com.hartwig.actin.clinical.datamodel.treatment.RadiotherapyType;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreatmentCategoryInput {
    private static final Logger LOGGER = LogManager.getLogger(TreatmentCategoryInput.class);

    @NotNull
    private final TreatmentCategory mappedCategory;
    @Nullable
    private final Set<TreatmentType> mappedTypes;

    private TreatmentCategoryInput(@NotNull TreatmentCategory mappedCategory) {
        this(mappedCategory, null);
    }

    private TreatmentCategoryInput(@NotNull TreatmentCategory mappedCategory, @Nullable Set<TreatmentType> mappedTypes) {
        this.mappedCategory = mappedCategory;
        this.mappedTypes = mappedTypes;
    }

    @NotNull
    public TreatmentCategory mappedCategory() {
        return mappedCategory;
    }

    @Nullable
    public Set<TreatmentType> mappedTypes() {
        return mappedTypes;
    }

    @NotNull
    public String display() {
        return this.toString().replaceAll("_", " ").toLowerCase();
    }

    @NotNull
    public static TreatmentCategoryInput fromString(@NotNull String input) {
        String query = inputToEnumString(input);
        try {
            return new TreatmentCategoryInput(TreatmentCategory.valueOf(query));
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Treatment category not found for query string {}", query);
        }

        TreatmentType treatmentType = resolveTreatmentType(query);
        return new TreatmentCategoryInput(treatmentType.category(), Set.of(treatmentType));
    }

    @NotNull
    public static TreatmentType treatmentTypeFromString(@NotNull String input) {
        return resolveTreatmentType(inputToEnumString(input));
    }

    @NotNull
    private static TreatmentType resolveTreatmentType(@NotNull String query) {
        List<Function<String, TreatmentType>> typeCreators =
                List.of(DrugType::valueOf, RadiotherapyType::valueOf, OtherTreatmentType::valueOf);
        for (Function<String, TreatmentType> createType : typeCreators) {
            try {
                return createType.apply(query);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Type not found for query string {}", query);
            }
        }
        throw new IllegalArgumentException("Could not resolve string to a treatment category or type: " + query);
    }

    @NotNull
    private static String inputToEnumString(@NotNull String input) {
        return input.trim().replaceAll(" ", "_").toUpperCase();
    }
}
