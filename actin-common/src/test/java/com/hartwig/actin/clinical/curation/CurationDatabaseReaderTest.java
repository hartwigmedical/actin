package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
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
        assertOncologicalHistoryConfigs(database.oncologicalHistoryConfigs());
    }

    private static void assertPrimaryTumorConfigs(@NotNull List<PrimaryTumorConfig> primaryTumorConfigs) {
        assertEquals(1, primaryTumorConfigs.size());

        PrimaryTumorConfig config = primaryTumorConfigs.get(0);
        assertEquals("Unknown | Carcinoma", config.input());
        assertEquals("Unknown", config.primaryTumorLocation());
        assertEquals("CUP", config.primaryTumorSubLocation());
        assertEquals("Carcinoma", config.primaryTumorType());
        assertEquals(Strings.EMPTY, config.primaryTumorSubType());
        assertEquals(Strings.EMPTY, config.primaryTumorExtraDetails());
        assertEquals(1, config.doids().size());
        assertTrue(config.doids().contains("299"));
    }

    private static void assertOncologicalHistoryConfigs(@NotNull List<OncologicalHistoryConfig> oncologicalHistoryConfigs) {
        assertEquals(1, oncologicalHistoryConfigs.size());

        OncologicalHistoryConfig config = oncologicalHistoryConfigs.get(0);
        assertEquals("Capecitabine/Oxaliplatin 2020", config.input());
        assertFalse(config.ignore());

        PriorTumorTreatment curated = (PriorTumorTreatment) config.curatedObject();
        assertEquals("Capecitabine+Oxaliplatin", curated.name());
        assertEquals(2020, (int) curated.year());
        assertEquals("chemotherapy", curated.category());
        assertTrue(curated.isSystemic());
        assertEquals("antimetabolite,platinum", curated.chemoType());
        assertNull(curated.immunoType());
        assertNull(curated.targetedType());
        assertNull(curated.hormoneType());
        assertNull(curated.stemCellTransType());
        assertNull(curated.radiotherapyType());
        assertNull(curated.surgeryType());
    }
}