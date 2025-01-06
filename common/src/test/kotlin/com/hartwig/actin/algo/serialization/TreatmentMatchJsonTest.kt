package com.hartwig.actin.algo.serialization

import com.hartwig.actin.algo.serialization.TreatmentMatchJson.fromJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson.read
import com.hartwig.actin.algo.serialization.TreatmentMatchJson.toJson
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

class TreatmentMatchJsonTest {

    private val algoDirectory = resourceOnClasspath("algo")
    private val treatmentMatchJson = algoDirectory + File.separator + "patient.treatment_match.json"

    @Test
    fun `Should be able to convert treatment match JSON back and forth`() {
        val minimal = TestTreatmentMatchFactory.createMinimalTreatmentMatch()
        val convertedMinimal = fromJson(toJson(minimal))
        assertThat(convertedMinimal).isEqualTo(minimal)

        val proper = TestTreatmentMatchFactory.createProperTreatmentMatch()
        val convertedProper = fromJson(toJson(proper))
        assertThat(convertedProper).isEqualTo(proper)
    }

    @Test
    fun `Should sort messages prior to serialization`() {
        val proper = TestTreatmentMatchFactory.createProperTreatmentMatch()
        val trialMatch: TrialMatch = proper.trialMatches[0]
        val key = trialMatch.evaluations.keys.first()
        val match: TreatmentMatch = proper.copy(
            trialMatches = listOf(
                trialMatch.copy(
                    evaluations = mapOf(
                        key to Evaluation(
                            result = EvaluationResult.PASS,
                            recoverable = false,
                            passMessages = setOf("msg 2", "msg 1", "msg 3"),
                        )
                    ),
                    cohorts = emptyList(),
                    nonEvaluableCohorts = emptyList()
                )
            ),
            personalizedDataAnalysis = null
        )

        //@formatter:off
        val expectedJson = ("""
            {
                "patientId":"ACTN01029999",
                "sampleId":"ACTN01029999T",
                "referenceDate":{"year":2021,"month":8,"day":2},
                "referenceDateIsLive":true,
                "trialMatches":[
                {
                    "identification":{
                        "trialId":"Test Trial 1","open":true,"acronym":"TEST-1","title":"Example test trial 1","nctId":"NCT00000010",
                        "phase":"PHASE_1","source":"NKI","locations":["Antoni van Leeuwenhoek"]},
                    "isPotentiallyEligible":true,
                    "evaluations":[
                        [{"references":[{"id":"I-01","text":"Patient must be an adult"}],
                        "function":{"rule":"IS_AT_LEAST_X_YEARS_OLD","parameters":[]}},
                        {"result":"PASS","recoverable":false,"inclusionMolecularEvents":[],"exclusionMolecularEvents":[],
                        "passMessages":["msg 1","msg 2","msg 3"],
                        "warnMessages":[],
                        "undeterminedMessages":[],,
                        "failMessages":[],"isMissingGenesForSufficientEvaluation":false}]],
                    "cohorts":[],
                    "nonEvaluableCohorts":[]
                }],
                "standardOfCareMatches":[
                    {"treatmentCandidate":
                        {"treatment":{"name":"Pembrolizumab","isSystemic":true,
                        "synonyms":[],"displayOverride":null,"categories":[],"types":[],"treatmentClass":"OTHER_TREATMENT"},
                        "optional":true,"eligibilityFunctions":[
                        {"rule":"HAS_KNOWN_ACTIVE_CNS_METASTASES","parameters":[]}],"additionalCriteriaForRequirement":[]},
                    "evaluations":[
                        {"result":"PASS","recoverable":false,"inclusionMolecularEvents":[],
                        "exclusionMolecularEvents":[],"passSpecificMessages":["Patient has active CNS metastases"],
                        "passGeneralMessages":["Active CNS metastases"],"warnSpecificMessages":[],"warnGeneralMessages":[],
                        "undeterminedSpecificMessages":[],"undeterminedGeneralMessages":[],"failSpecificMessages":[],
                        "failGeneralMessages":[],"isMissingGenesForSufficientEvaluation":false}],
                    "annotations":[
                        {"acronym":"Study of Pembrolizumab","phase":"Phase III",
                        "treatments":[
                            {"name":"PEMBROLIZUMAB",
                            "drugs":[{"name":"PEMBROLIZUMAB","drugTypes":["TOPO1_INHIBITOR"],"category":"CHEMOTHERAPY",
                            "displayOverride":null}],"synonyms":[],"displayOverride":null,"isSystemic":true,"maxCycles":null,
                            "treatmentClass":"DRUG_TREATMENT"}],
                        "therapeuticSetting":"ADJUVANT",
                        "variantRequirements":[{"name":"MSI high","requirementType":"required"}],
                        "trialReferences":[
                            {"patientPopulations":[
                            {"name":"Pembrolizumab","isControl":true,"ageMin":55,"ageMax":65,
                            "ageMedian":60.0,"numberOfPatients":200,"numberOfMale":100,"numberOfFemale":100,
                            "patientsWithWho0":100,"patientsWithWho1":0,"patientsWithWho2":0,"patientsWithWho3":0,
                            "patientsWithWho4":0,"patientsWithWho0to1":0,"patientsWithWho1to2":0,
                            "patientsPerPrimaryTumorLocation":{"Rectum":100},"mutations":null,
                            "patientsWithPrimaryTumorRemovedComplete":50,"patientsWithPrimaryTumorRemovedPartial":25,
                            "patientsWithPrimaryTumorRemoved":25,"patientsPerMetastaticSites":
                            {"Lung":{"value":100,"percentage":100.0}},"timeOfMetastases":"BOTH","treatment":
                            {"name":"PEMBROLIZUMAB","drugs":[{"name":"PEMBROLIZUMAB","drugTypes":["TOPO1_INHIBITOR"],
                            "category":"CHEMOTHERAPY","displayOverride":null}],"synonyms":[],"displayOverride":null,
                            "isSystemic":true,"maxCycles":null,"treatmentClass":"DRUG_TREATMENT"},
                            "priorSystemicTherapy":"Chemo","patientsWithMSI":33,
                            "medianFollowUpForSurvival":"30","medianFollowUpPFS":"30",
                            "analysisGroups":[{"id":1,"name":"Analysis group","nPatients":200,"endPoints":[
                            {"id":2,"name":"Median Progression-Free Survival","value":6.8,"unitOfMeasure":"MONTHS",
                            "confidenceInterval":null,"type":"PRIMARY",
                            "derivedMetrics":[{"relativeMetricId":1,"value":16.0,"type":"PRIMARY","confidenceInterval":{"lowerLimit":14.0,
                            "upperLimit":18.8},"pValue":"0.0002"}]}]}],"priorTherapies":"5-FU","patientsPerRace":null,
                            "patientsPerRegion":null}],
                            "url":"http://www.ncbi.nlm.nih.gov/pubmed/12345678"}]}],
                    "generalPfs":{"value":136.5,"numPatients":98,"min":74,"max":281,"iqr":46.0},
                    "generalOs":{"value":215.0,"numPatients":90,"min":121,"max":470,"iqr":110.1},
                    "resistanceEvidence":[
                        {"event":"BRAF amp","treatmentName":"Pembrolizumab","resistanceLevel":"A",
                        "isTested":null,"isFound":false,"evidenceUrls":["website"]}]}
                    ],
                "personalizedDataAnalysis":null,
                "maxMolecularTestAge":null}
                """).lineSequence().joinToString("") { it.trim() }
        //@formatter:on

        assertThat(toJson(match)).isEqualTo(expectedJson)
    }

    @Test
    fun `Should be able to read treatment match JSON`() {
        val match = read(treatmentMatchJson)
        assertThat(match.patientId).isEqualTo("ACTN01029999")
        assertThat(match.trialMatches).hasSize(1)
        val trialMatch = match.trialMatches[0]
        assertThat(trialMatch.evaluations).hasSize(1)
        assertThat(trialMatch.cohorts).hasSize(3)
    }
}