package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.clinical.curation.config.BiopsyLocationConfig;
import com.hartwig.actin.clinical.curation.config.CancerRelatedComplicationConfig;
import com.hartwig.actin.clinical.curation.config.CurationConfig;
import com.hartwig.actin.clinical.curation.config.ECGConfig;
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.ToxicityConfig;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CurationDatabaseReaderTest {

    private static final String CURATION_DIRECTORY = Resources.getResource("clinical/curation").getPath();

    @Test
    public void canReadFromTestDirectory() throws IOException {
        CurationDatabase database = CurationDatabaseReader.read(CURATION_DIRECTORY);

        assertPrimaryTumorConfigs(database.primaryTumorConfigs());
        assertBiopsyLocationConfigs(database.biopsyLocationConfigs());
        assertOncologicalHistoryConfigs(database.oncologicalHistoryConfigs());
        assertNonOncologicalHistoryConfigs(database.nonOncologicalHistoryConfigs());
        assertECGConfigs(database.ecgConfigs());
        assertCancerRelatedComplicationConfigs(database.cancerRelatedComplicationConfigs());
        assertToxicityConfigs(database.toxicityConfigs());
    }

    private static void assertPrimaryTumorConfigs(@NotNull List<PrimaryTumorConfig> configs) {
        assertEquals(1, configs.size());

        PrimaryTumorConfig config = configs.get(0);
        assertEquals("Unknown | Carcinoma", config.input());
        assertEquals("Unknown", config.primaryTumorLocation());
        assertEquals("CUP", config.primaryTumorSubLocation());
        assertEquals("Carcinoma", config.primaryTumorType());
        assertEquals(Strings.EMPTY, config.primaryTumorSubType());
        assertEquals(Strings.EMPTY, config.primaryTumorExtraDetails());
        assertEquals(1, config.doids().size());
        assertTrue(config.doids().contains("299"));
    }

    private static void assertBiopsyLocationConfigs(@NotNull List<BiopsyLocationConfig> configs) {
        assertEquals(1, configs.size());

        BiopsyLocationConfig config = configs.get(0);
        assertEquals("Lever", config.input());
        assertEquals("Liver", config.location());
    }

    private static void assertOncologicalHistoryConfigs(@NotNull List<OncologicalHistoryConfig> configs) {
        assertEquals(2, configs.size());

        OncologicalHistoryConfig config1 = find(configs, "Capecitabine/Oxaliplatin 2020");
        assertFalse(config1.ignore());

        PriorTumorTreatment curated1 = (PriorTumorTreatment) config1.curatedObject();
        assertEquals("Capecitabine+Oxaliplatin", curated1.name());
        assertEquals(2020, (int) curated1.year());
        assertEquals("chemotherapy", curated1.category());
        assertTrue(curated1.isSystemic());
        assertEquals("antimetabolite,platinum", curated1.chemoType());
        assertNull(curated1.immunoType());
        assertNull(curated1.targetedType());
        assertNull(curated1.hormoneType());
        assertNull(curated1.stemCellTransType());
        assertNull(curated1.radiotherapyType());
        assertNull(curated1.surgeryType());

        OncologicalHistoryConfig config2 = find(configs, "Breast 2018");
        assertFalse(config2.ignore());

        PriorSecondPrimary curated2 = (PriorSecondPrimary) config2.curatedObject();
        assertEquals("Breast", curated2.tumorLocation());
        assertEquals(Strings.EMPTY, curated2.tumorSubLocation());
        assertEquals("Carcinoma", curated2.tumorType());
        assertEquals(Strings.EMPTY, curated2.tumorSubType());
        assertTrue(curated2.doids().isEmpty());
        assertTrue(curated2.isSecondPrimaryCured());
        assertEquals(2019, (int) curated2.curedYear());
    }

    private static void assertNonOncologicalHistoryConfigs(@NotNull List<NonOncologicalHistoryConfig> configs) {
        assertEquals(2, configs.size());
        NonOncologicalHistoryConfig config1 = find(configs, "Levercirrose/ sarcoidose");
        assertFalse(config1.ignore());

        assertEquals("Liver cirrhosis and sarcoidosis", config1.curated().name());
        assertEquals("Liver disease", config1.curated().category());
        assertEquals(2, config1.curated().doids().size());
        assertTrue(config1.curated().doids().contains("5082"));
        assertTrue(config1.curated().doids().contains("11335"));

        NonOncologicalHistoryConfig config2 = find(configs, "NA");
        assertTrue(config2.ignore());
        assertNull(config2.curated());
    }

    private static void assertECGConfigs(@NotNull List<ECGConfig> configs) {
        assertEquals(1, configs.size());

        ECGConfig config = configs.get(0);
        assertEquals("Sinus Tachycardia", config.input());
        assertEquals("Sinus tachycardia", config.interpretation());
    }

    private static void assertCancerRelatedComplicationConfigs(@NotNull List<CancerRelatedComplicationConfig> configs) {
        assertEquals(1, configs.size());

        CancerRelatedComplicationConfig config = configs.get(0);
        assertEquals("something", config.input());
        assertEquals("curated something", config.name());
    }

    private static void assertToxicityConfigs(@NotNull List<ToxicityConfig> configs) {
        assertEquals(1, configs.size());

        ToxicityConfig config = configs.get(0);
        assertEquals("Neuropathy GR3", config.input());
        assertEquals("Neuropathy", config.name());
        assertEquals(3, (int) config.grade());
    }

    @NotNull
    private static <T extends CurationConfig> T find(@NotNull List<T> configs, @NotNull String input) {
        for (T config : configs) {
            if (config.input().equals(input)) {
                return config;
            }
        }

        throw new IllegalStateException("Could not find input '" + input + "' in configs");
    }
}