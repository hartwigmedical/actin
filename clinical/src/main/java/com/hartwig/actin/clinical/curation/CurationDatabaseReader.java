package com.hartwig.actin.clinical.curation;

import java.io.IOException;
import java.util.List;

import com.hartwig.actin.clinical.curation.config.CancerRelatedComplicationConfig;
import com.hartwig.actin.clinical.curation.config.CancerRelatedComplicationConfigFile;
import com.hartwig.actin.clinical.curation.config.ECGConfig;
import com.hartwig.actin.clinical.curation.config.ECGConfigFile;
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig;
import com.hartwig.actin.clinical.curation.config.LesionLocationConfigFile;
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig;
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfigFile;
import com.hartwig.actin.clinical.curation.config.MedicationTypeConfig;
import com.hartwig.actin.clinical.curation.config.MedicationTypeConfigFile;
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfigFile;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfigFile;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfigFile;
import com.hartwig.actin.clinical.curation.config.ToxicityConfig;
import com.hartwig.actin.clinical.curation.config.ToxicityConfigFile;
import com.hartwig.actin.clinical.curation.translation.AllergyTranslation;
import com.hartwig.actin.clinical.curation.translation.AllergyTranslationFile;
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslation;
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslationFile;
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation;
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslationFile;
import com.hartwig.actin.util.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class CurationDatabaseReader {

    private static final Logger LOGGER = LogManager.getLogger(CurationDatabaseReader.class);

    private static final String PRIMARY_TUMOR_TSV = "primary_tumor.tsv";
    private static final String LESION_LOCATION_TSV = "lesion_location.tsv";
    private static final String ONCOLOGICAL_HISTORY_TSV = "oncological_history.tsv";
    private static final String NON_ONCOLOGICAL_HISTORY_TSV = "non_oncological_history.tsv";
    private static final String ECG_TSV = "ecg.tsv";
    private static final String CANCER_RELATED_COMPLICATION_TSV = "cancer_related_complication.tsv";
    private static final String TOXICITY_TSV = "toxicity.tsv";
    private static final String MEDICATION_DOSAGE_TSV = "medication_dosage.tsv";
    private static final String MEDICATION_TYPE_TSV = "medication_type.tsv";

    private static final String LABORATORY_TRANSLATION_TSV = "laboratory_translation.tsv";
    private static final String ALLERGY_TRANSLATION_TSV = "allergy_translation.tsv";
    private static final String BLOOD_TRANSFUSION_TRANSLATION_TSV = "blood_transfusion_translation.tsv";

    private CurationDatabaseReader() {
    }

    @NotNull
    public static CurationDatabase read(@NotNull String clinicalCurationDirectory) throws IOException {
        LOGGER.info("Reading clinical curation config from {}", clinicalCurationDirectory);

        String basePath = Paths.forceTrailingFileSeparator(clinicalCurationDirectory);

        return ImmutableCurationDatabase.builder()
                .primaryTumorConfigs(readPrimaryTumorConfigs(basePath + PRIMARY_TUMOR_TSV))
                .lesionLocationConfigs(readLesionLocationConfigs(basePath + LESION_LOCATION_TSV))
                .oncologicalHistoryConfigs(readOncologicalHistoryConfigs(basePath + ONCOLOGICAL_HISTORY_TSV))
                .nonOncologicalHistoryConfigs(readNonOncologicalHistoryConfigs(basePath + NON_ONCOLOGICAL_HISTORY_TSV))
                .ecgConfigs(readECGConfigs(basePath + ECG_TSV))
                .cancerRelatedComplicationConfigs(readCancerRelatedComplicationConfigs(basePath + CANCER_RELATED_COMPLICATION_TSV))
                .toxicityConfigs(readToxicityConfigs(basePath + TOXICITY_TSV))
                .medicationDosageConfigs(readMedicationDosageConfigs(basePath + MEDICATION_DOSAGE_TSV))
                .medicationTypeConfigs(readMedicationTypeConfigs(basePath + MEDICATION_TYPE_TSV))
                .laboratoryTranslations(readLaboratoryTranslations(basePath + LABORATORY_TRANSLATION_TSV))
                .allergyTranslations(readAllergyTranslations(basePath + ALLERGY_TRANSLATION_TSV))
                .bloodTransfusionTranslations(readBloodTransfusionTranslations(basePath + BLOOD_TRANSFUSION_TRANSLATION_TSV))
                .build();
    }

    @NotNull
    private static List<PrimaryTumorConfig> readPrimaryTumorConfigs(@NotNull String primaryTumorTsv) throws IOException {
        List<PrimaryTumorConfig> configs = PrimaryTumorConfigFile.read(primaryTumorTsv);
        LOGGER.info(" Read {} primary tumor configs from {}", configs.size(), primaryTumorTsv);
        return configs;
    }

    @NotNull
    private static List<LesionLocationConfig> readLesionLocationConfigs(@NotNull String lesionLocationTsv) throws IOException {
        List<LesionLocationConfig> configs = LesionLocationConfigFile.read(lesionLocationTsv);
        LOGGER.info(" Read {} lesion location configs from {}", configs.size(), lesionLocationTsv);
        return configs;
    }

    @NotNull
    private static List<OncologicalHistoryConfig> readOncologicalHistoryConfigs(@NotNull String oncologicalHistoryTsv) throws IOException {
        List<OncologicalHistoryConfig> configs = OncologicalHistoryConfigFile.read(oncologicalHistoryTsv);
        LOGGER.info(" Read {} oncological history configs from {}", configs.size(), oncologicalHistoryTsv);
        return configs;
    }

    @NotNull
    private static List<NonOncologicalHistoryConfig> readNonOncologicalHistoryConfigs(@NotNull String nonOncologicalHistoryTsv)
            throws IOException {
        List<NonOncologicalHistoryConfig> configs = NonOncologicalHistoryConfigFile.read(nonOncologicalHistoryTsv);
        LOGGER.info(" Read {} non-oncological history configs from {}", configs.size(), nonOncologicalHistoryTsv);
        return configs;
    }

    @NotNull
    private static List<ECGConfig> readECGConfigs(final String ecgTsv) throws IOException {
        List<ECGConfig> configs = ECGConfigFile.read(ecgTsv);
        LOGGER.info(" Read {} ECG configs from {}", configs.size(), ecgTsv);
        return configs;
    }

    @NotNull
    private static List<CancerRelatedComplicationConfig> readCancerRelatedComplicationConfigs(@NotNull String cancerRelationComplicationTsv)
            throws IOException {
        List<CancerRelatedComplicationConfig> configs = CancerRelatedComplicationConfigFile.read(cancerRelationComplicationTsv);
        LOGGER.info(" Read {} cancer related complication configs from {}", configs.size(), cancerRelationComplicationTsv);
        return configs;
    }

    @NotNull
    private static List<ToxicityConfig> readToxicityConfigs(@NotNull String toxicityTsv) throws IOException {
        List<ToxicityConfig> configs = ToxicityConfigFile.read(toxicityTsv);
        LOGGER.info(" Read {} toxicity configs from {}", configs.size(), toxicityTsv);
        return configs;
    }

    @NotNull
    private static List<MedicationDosageConfig> readMedicationDosageConfigs(@NotNull String medicationDosageTsv) throws IOException {
        List<MedicationDosageConfig> configs = MedicationDosageConfigFile.read(medicationDosageTsv);
        LOGGER.info(" Read {} medication dosage configs from {}", configs.size(), medicationDosageTsv);
        return configs;
    }

    @NotNull
    private static List<MedicationTypeConfig> readMedicationTypeConfigs(@NotNull String medicationTypeTsv) throws IOException {
        List<MedicationTypeConfig> configs = MedicationTypeConfigFile.read(medicationTypeTsv);
        LOGGER.info(" Read {} medication type configs from {}", configs.size(), medicationTypeTsv);
        return configs;
    }

    @NotNull
    private static List<LaboratoryTranslation> readLaboratoryTranslations(@NotNull String laboratoryTranslationTsv) throws IOException {
        List<LaboratoryTranslation> translations = LaboratoryTranslationFile.read(laboratoryTranslationTsv);
        LOGGER.info(" Read {} laboratory translations from {}", translations.size(), laboratoryTranslationTsv);
        return translations;
    }

    @NotNull
    private static List<AllergyTranslation> readAllergyTranslations(@NotNull String allergyTranslationTsv) throws IOException {
        List<AllergyTranslation> translations = AllergyTranslationFile.read(allergyTranslationTsv);
        LOGGER.info(" Read {} allergy translations from {}", translations.size(), allergyTranslationTsv);
        return translations;
    }

    @NotNull
    private static List<BloodTransfusionTranslation> readBloodTransfusionTranslations(@NotNull String bloodTransfusionTranslationTsv)
            throws IOException {
        List<BloodTransfusionTranslation> translations = BloodTransfusionTranslationFile.read(bloodTransfusionTranslationTsv);
        LOGGER.info(" Read {} blood transfusion translations from {}", translations.size(), bloodTransfusionTranslationTsv);
        return translations;
    }
}
