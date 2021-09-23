package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TumorDetails;

import org.junit.Test;

public class CurationModelTest {

    private static final String CURATION_DIRECTORY = Resources.getResource("clinical/curation").getPath();

    @Test
    public void canCreateFromCurationDirectory() throws IOException {
        assertNotNull(CurationModel.fromCurationDirectory(CURATION_DIRECTORY));
    }

    @Test
    public void canCurateTumorDetails() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        TumorDetails curated = model.toTumorDetails("Unknown", "Carcinoma");
        assertEquals("Unknown", curated.primaryTumorLocation());

        TumorDetails missing = model.toTumorDetails("Does not", "Exist");
        assertNull(missing.primaryTumorLocation());
    }

    @Test
    public void canCuratePriorTreatments() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        List<PriorTumorTreatment> priorTreatments =
                model.toPriorTumorTreatments(Lists.newArrayList("Resection 2020", "no systemic treatment", "cannot curate"));

        assertEquals(1, priorTreatments.size());
        assertEquals("Primary Resection", priorTreatments.get(0).surgeryType());
    }
}