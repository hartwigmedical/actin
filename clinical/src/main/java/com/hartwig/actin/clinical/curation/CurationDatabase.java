package com.hartwig.actin.clinical.curation;

import java.util.List;

import com.hartwig.actin.clinical.curation.config.CancerRelatedComplicationConfig;
import com.hartwig.actin.clinical.curation.config.ECGConfig;
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig;
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig;
import com.hartwig.actin.clinical.curation.config.MedicationTypeConfig;
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.ToxicityConfig;
import com.hartwig.actin.clinical.curation.translation.AllergyTranslation;
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class CurationDatabase {

    @NotNull
    public abstract List<PrimaryTumorConfig> primaryTumorConfigs();

    @NotNull
    public abstract List<LesionLocationConfig> lesionLocationConfigs();

    @NotNull
    public abstract List<OncologicalHistoryConfig> oncologicalHistoryConfigs();

    @NotNull
    public abstract List<NonOncologicalHistoryConfig> nonOncologicalHistoryConfigs();

    @NotNull
    public abstract List<ECGConfig> ecgConfigs();

    @NotNull
    public abstract List<CancerRelatedComplicationConfig> cancerRelatedComplicationConfigs();

    @NotNull
    public abstract List<ToxicityConfig> toxicityConfigs();

    @NotNull
    public abstract List<MedicationDosageConfig> medicationDosageConfigs();

    @NotNull
    public abstract List<MedicationTypeConfig> medicationTypeConfigs();

    @NotNull
    public abstract List<LaboratoryTranslation> laboratoryTranslations();

    @NotNull
    public abstract List<AllergyTranslation> allergyTranslations();

}
