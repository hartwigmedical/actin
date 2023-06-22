package com.hartwig.actin.clinical.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.io.Resources;
import com.hartwig.actin.clinical.datamodel.treatment.Drug;
import com.hartwig.actin.clinical.datamodel.treatment.DrugClass;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug;
import com.hartwig.actin.clinical.datamodel.treatment.Therapy;
import com.hartwig.actin.clinical.datamodel.treatment.Treatment;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;

import org.junit.Test;

public class TreatmentJsonTest {

    private static final String CLINICAL_DIRECTORY = Resources.getResource("clinical").getPath();
    private static final String TREATMENT_JSON = CLINICAL_DIRECTORY + File.separator + "treatment.json";

    @Test
    public void shouldReadDrugsFromJsonFile() throws IOException {
        Set<Drug> expectedDrugs = Set.of(ImmutableDrug.builder().name("Capecitabine").addDrugClasses(DrugClass.ANTIMETABOLITE).build(),
                ImmutableDrug.builder().name("Oxaliplatin").addDrugClasses(DrugClass.PLATINUM_COMPOUND).build());
        Map<String, Drug> drugsByName =
                expectedDrugs.stream().collect(Collectors.toMap(drug -> drug.name().toLowerCase(), Function.identity()));

        List<Treatment> treatments = TreatmentJson.read(TREATMENT_JSON, drugsByName);
        assertEquals(1, treatments.size());
        Therapy therapy = (Therapy) treatments.get(0);
        assertEquals("Capecitabine+Oxaliplatin", therapy.name());
        assertTrue(therapy.isSystemic());
        assertEquals(Set.of(TreatmentCategory.CHEMOTHERAPY), therapy.categories());

        assertEquals(expectedDrugs, therapy.drugs());
    }
}