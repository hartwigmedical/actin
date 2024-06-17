## ACTIN-Molecular

ACTIN-Molecular interprets molecular results and maps these results to the datamodel described below. In addition, the data is written to a
per-sample JSON file. ACTIN-Molecular only supports interpretation
of [ORANGE](https://github.com/hartwigmedical/hmftools/tree/master/orange) molecular
results as produced by [HMF Platinum](https://github.com/hartwigmedical/platinum) as well as molecular tests done historically made available via the patient's clinical data.   

The molecular interpreter application requires Java 11+ and can be run as follows:

```
java -cp actin.jar com.hartwig.actin.molecular.MolecularInterpreterApplication \
   -orange_json /path/to/orange.json \
   -serve_directory /path/to/serve_directory \
   -known_genes_tsv /path/to/known_genes.tsv \
   -clinical_json /path/to/actin_clinical.json \
   -output_directory /path/to/where/molecular_json_file_is_written
```

An optional `log_debug` parameter can be provided to generate extra logging.

The following assumptions are made about the inputs:

- The ORANGE JSON is the JSON output from [ORANGE](https://github.com/hartwigmedical/hmftools/tree/master/orange).
- The SERVE directory is the output of [SERVE](https://github.com/hartwigmedical/serve/tree/master/algo) and is used for annotation and
  interpretation of the genomic findings.
- The known genes is a TSV file with gene and geneRole columns. An example can be
  found [here](https://github.com/hartwigmedical/actin/blob/master/common/src/test/resources/known_genes/example_known_genes.tsv).
    - This resource file will be moved into SERVE in the future.
- The clinical JSON is the output of [ACTIN Clinical](https://github.com/hartwigmedical/actin/tree/master/clinical). This file is used to
  extract
  the primary tumor DOIDs, which are used to determine whether evidence is on-label or off-label.

## Integration of non-orange molecular results

Molecular results which are not orange flow via the clinical data and the prior molecular tests. These results are normalized and integrated into
a single molecular history, which can be processed by downstream rules without specific knowledge about what type of test was done. This integration
process is documented in the diagram below.

![Integrating Molecular Data](integrating_molecular_data.png)

The flow of data from provider to rule follows these steps:
- An extractor transforms the data into a model which more easily supports annotation.
- An annotator adds evidence (see [Evidence annotation](#evidence-annotation)). In the case of panel tests not extracted from ORANGE results, we also add genomic positional annotation (using transvar) and driver likelihood.
- The annotators produce either a PanelRecord or MolecularRecord. These both conform to the MolecularTest interface and are combines in a single list in the molecular history.
- Molecular rules can then evaluate the molecular history.

## ACTIN Molecular Datamodel

### Molecular History

The molecular history represents all molecular testing done for a patient. This can include WGS, panels and IHC tests. It is modeled as a
list of molecular tests.

### Molecular test

#### 1 base molecular test

| Field           | Example Value             | Details                                                                   |
|-----------------|---------------------------|---------------------------------------------------------------------------|
| type            | WGS                       | The type of molecular experiment done, either `WGS` or `PANEL`            | 
| date            | 2022-01-14                | The date on which the molecular results were obtained                     |
| evidenceSource  | CKB_EVIDENCE              | The name of the provider of the evidence. Currently always `CKB_EVIDENCE` |
| drivers         | see drivers below         |                                                                           |
| characteristics | see characteristics below |                                                                           |

#### 1 molecular characteristics

| Field                         | Example Value      | Details                                                                                                              |
|-------------------------------|--------------------|----------------------------------------------------------------------------------------------------------------------|
| purity                        | 78%                | The percentage of cells in the sequenced biopsy that originated from the tumor                                       |
| ploidy                        | 3.1                | The average number of copies of any chromosome in the tumor                                                          |
| predictedTumorType            | Melanoma (87%)     | The tumor type of origin predicted based on the molecular data along with a likelihood                               |
| isMicrosatelliteUnstable      | false              | If true, sample is considered microsatellite unstable. Should be empty in case experiment does not determine MSI     |
| microsatelliteEvidence        | See evidence below | The evidence determined for the microsatellite status of specific tumor sample                                       |                                        
| homologousRepairScore         | 0.5                | The probability of this sample being HR deficient.                                                                   |
| isHomologousRepairDeficient   | false              | If true, sample is considered homologous repair deficient. Should be empty in case experiment does not determine HRD |
| homologousRepairEvidence      | See evidence below | The evidence determined for the homologous repair status of specific tumor sample                                    |                                     
| tumorMutationalBurden         | 14.2               | Number of mutations in the genome per Mb. Should be empty in case experiment does not determine TMB                  |
| hasHighTumorMutationalBurden  | true               | If true, sample is considered to have a high tumor mutational burden (otherwise, low)                                |
| tumorMutationalBurdenEvidence | See evidence below | The evidence determined for the tumor mutational burden status of specific tumor sample.                             |
| tumorMutationalLoad           | 115                | Number of missense mutations across the genome. Should be empty in case experiment does not determine TML            |
| hasHighTumorMutationalLoad    | false              | If true, sample is considered to have a high tumor mutational load (otherwise, low)                                  |
| tumorMutationalLoadEvidence   | See evidence below | The evidence determined for the tumor mutational load of specific tumor sample                                       |

#### N driver events

Every potential driver event has the following fields ('general driver fields'):

| Field            | Example Value      | Details                                                                                                              |
|------------------|--------------------|----------------------------------------------------------------------------------------------------------------------|
| isReportable     | true               | Indicates whether this driver event is considered relevant enough to be explicitly mentioned in a report             |
| event            | BRAF V600E         | A human readable string summarizing the driver event                                                                 |
| driverLikelihood | HIGH               | An optional field that indicates the likelihood that the event is a driver (either `HIGH`, `MEDIUM`, `LOW` or empty) |
| evidence         | See evidence below | The evidence determined for this driver in the specific tumor sample                                                 |

Furthermore, every gene driver event is assigned the following fields ('gene driver fields'):

| Field                          | Example Value    | Details                                                                                                                                                                                                                |
|--------------------------------|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| isReportable                   |                  |                                                                                                                                                                                                                        |
| gene                           | BRAF             | The name of the gene                                                                                                                                                                                                   |
| geneRole                       | ONCO             | The role of the gene in cancer (either `BOTH`, `ONCO`, `TSG`, or `UNKNOWN`)                                                                                                                                            |
| driverLikelihood               |                  |                                                                                                                                                                                                                        |
| proteinEffect                  | GAIN_OF_FUNCTION | The effect the specific driver has on the gene (one of `UNKNOWN`, `AMBIGIOUS`, `NO_EFFECT`, `NO_EFFECT_PREDICTED`, `LOSS_OF_FUNCTION`, `LOSS_OF_FUNCTION_PREDICTED`, `GAIN_OF_FUNCTION`, `GAIN_OF_FUNCTION_PREDICTED`) |
| isAssociatedWithDrugResistance | true             | An optional boolean indicating the specific driver event is associated with some form of drug resistance                                                                                                               |

#### N variants

In addition to the driver fields, the following data is captured for all detected variants:

| Field                          | Example Value | Details                                                                                                  |
|--------------------------------|---------------|----------------------------------------------------------------------------------------------------------|
| type                           | SNV           | The type of variant (one of `SNV`, `MNV`, `INSERT`, `DELETE`)                                            | 
| gene                           |               |                                                                                                          |
| geneRole                       |               |                                                                                                          |
| chromosome                     |               |                                                                                                          |
| position                       |               |                                                                                                          |
| ref                            |               |                                                                                                          |
| alt                            |               |                                                                                                          |
| proteinEffect                  |               |                                                                                                          |
| isAssociatedWithDrugResistance |               |                                                                                                          |
| isHotspot                      | true          | Indicates whether this specific variant is a known (pathogenic) hotspot                                  |
| canonicalImpact                | See impact    | The impact of this variant on the canonical transcript of the gene                                       |
| otherImpacts                   | See impact    | A set of impacts on transcripts other than the canonical transcript of the gene                          | 
| isAssociatedWithDrugResistance | true          | An optional boolean indicating the specific driver event is associated with some form of drug resistance |

If we have an ORANGE molecular test done for the sample, we can annotate with the following additional fields (captured
in `ExtendedVariant`).

| Field             | Example Value | Details                                                                                             |
|-------------------|---------------|-----------------------------------------------------------------------------------------------------|
| variantCopyNumber | 3.8           | The number of copies of this variant in the tumor                                                   |
| totalCopyNumber   | 4.0           | The total number of copies in the tumor on the variant genomic position                             |
| isBiallelic       | false         | Indicates whether all alleles in the tumor are affected by this variant or not                      |
| isHotspot         | true          | Indicates whether this specific variant is a known (pathogenic) hotspot                             | 
| clonalLikelihood  | 100%          | Likelihood that the variant exists in every tumor cell (is clonal)                                  |
| phaseGroups       | 1, 2          | The phasing groups this variant belongs to. Variants that are phased share at least one phase group |

The following data is captured as impact of a variant on a specific transcript:

| Field             | Example Value | Details                                                                  |
|-------------------|---------------|--------------------------------------------------------------------------|
| transcriptId      | ENST00001     | The ensembl ID of the transcript                                         | 
| hgvsCodingImpact  | c.123G>T      | The HGVS coding impact on the transcript                                 |
| hgvsProteinImpact | p.V41E        | The HGVS protein impact on the transcript                                |
| affectedCodon     | 41            | Optional field, the codon that is affected by the variant                |
| affectedExon      | 2             | Optional field, the exon that is affected by the variant                 |
| isSpliceRegion    | false         | Indicates whether this variant affects a splice region of the transcript |
| effects           | MISSENSE      | A set of effects that this variant has on the transcript                 |
| codingEffect      | MISSENSE      | A single, summarized coding effect this variant has on the transcript    |

#### N copy numbers

In addition to the driver fields, the following data is captured per copy number:

| Field     | Example Value | Details                                                                        |
|-----------|---------------|--------------------------------------------------------------------------------|
| type      | FULL_GAIN     | The type of copy number (either `FULL_GAIN`, `PARTIAL_GAIN`, `LOSS` or `NONE`) |
| minCopies | 12            | The minimum copy number of the gene along the canonical transcript of the gene |
| maxCopies | 18            | The maximum copy number of the gene along the canonical transcript of the gene |

#### N homozygous disruptions

For homozygous disruptions, no additional data is captured beyond the driver fields.

#### N disruptions

In addition to the driver fields, the following data is captured per disruption:

| Field                 | Example Value | Details                                                                                                        |
|-----------------------|---------------|----------------------------------------------------------------------------------------------------------------|
| type                  | BND           | The type of disruption (either `BND`, `DEL`, `DUP`, `INF`, `INS`, `INV` or `SGL`)                              |
| junctionCopyNumber    | 1.1           | Number of copies affected by this disruption                                                                   |
| undisruptedCopyNumber | 1.8           | Remaining number of copies not impacted by this disruption                                                     |
| regionType            | INTRONIC      | The region where this disruption starts or ends (either `INTRONIC`, `EXONIC`, `UPSTREAM`, `DOWNSTREAM` or `IG` |
| codingContext         | UTR_5P        | The coding context of the disruption (either `ENHANCER`, `UTR_5P`, `CODING`, `NON_CODING`, `UTR_3P`            |
| clusterGroup          | 3             | The ID of the cluster this disruption belongs to                                                               |

#### N fusions

In addition to the general driver fields, the following data is captured per fusion:

| Field                          | Example Value    | Details                                                                         |
|--------------------------------|------------------|---------------------------------------------------------------------------------|
| geneStart                      | EML4             | The gene that makes up the 5' part of the fusion                                |
| geneTranscriptStart            | ENST001          | The ensembl ID of the transcript that makes up the 5' part of the fusion        |
| fusedExonUp                    | 10               | The last exon of the 5' gene included in the fusion                             |
| geneEnd                        | ALK              | The gene that makes up the 3' part of the fusion                                |
| geneTranscriptEnd              | ENST002          | The ensembl ID of the transcript that makes up the 3' part of the fusion        |
| fusedExonDown                  | 22               | The first exon of the 3' gene included in the fusion                            |
| driverType                     | KNOWN_PAIR       | The type of driver fusion                                                       |
| proteinEffect                  | GAIN_OF_FUNCTION | The type of protein effect of the fusion product                                |
| isAssociatedWithDrugResistance | null             | Optional field, indicates whether the fusion is associated with drug resistance |

#### N viruses

In addition to the general driver fields, the following data is captured per virus:

| Field        | Example Value                | Details                                                                                      |
|--------------|------------------------------|----------------------------------------------------------------------------------------------|
| name         | Human papillomavirus type 16 | Name of the virus found in the tumor sample                                                  |
| type         | HUMAN_PAPILLOMA_VIRUS        | The type of virus found in the tumor sample                                                  |
| isReliable   | false                        | Boolean indicated whether the virus has reliably been found and could be considered a driver | 
| integrations | 3                            | Number of integrations of detected virus in the tumor sample                                 |

### Molecular record

Overall, a molecular record belongs to a `sampleId` (which belongs to a `patientId`). The molecular record supports all fields from
molecular test, but
adds several additional fields which can be extracted from the comprehensive results created by Hartwig WGS and ORANGE.

### 1 molecular base data

| Field                | Example Value | Details                                                                                                                                    |
|----------------------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| refGenomeVersion     | V37           | The version of the reference genome used throughout the analysis, either `V37` or `V38`                                                    |
| externalTrialSource  | CKB_TRIAL     | The name of the provider of external trials (which are trials that may not be known in ACTIN trial database). Currently always `CKB_TRIAL` |
| containsTumorCells   | true          | If false, implies that the tumor cell percentage in the biopsy was lower than the lowest detectable threshold                              |
| hasSufficientQuality | true          | If false, implies that the quality of the sample was not sufficient (e.g. too much DNA damage)                                             |

#### N HLA alleles (with a single `isReliable` boolean indicating whether the HLA results are reliable)

| Field               | Example Value | Details                                                                              |
|---------------------|---------------|--------------------------------------------------------------------------------------|
| name                | A*02:01       | Name of the HLA allele                                                               |
| tumorCopyNumber     | 1.2           | The number of copies of this HLA allele in the tumor sample.                         |
| hasSomaticMutations | false         | A boolean indicating whether any mutations have occurred in this allele in the tumor |

#### N pharmaco

| Field             | Example Value   | Details                                             |
|-------------------|-----------------|-----------------------------------------------------|
| gene              | DPYD            | The gene for which the pharmaco entry is applicable |
| haplotype         | 1* HOM          | Haplotypes found for the gene                       |
| haplotypeFunction | Normal Function | Functional impact of corresponding haplotype        |

### Evidence assignment

Evidence is assigned to molecular driver events and characteristics using the following datamodel:

| Field                          | Example Value            | Details                                                                                                                         |
|--------------------------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| approvedTreatments             | Pembrolizumab, Nivolumab | A set of treatment names which are approved based on tumor type and mutation / characteristic                                   |
| externalEligibleTrials         | Trial A                  | A set of trials for which patient may be eligible based on tumor type and mutation / characteristic                             |
| onLabelExperimentalTreatments  | Olaparib                 | A set of treatment names which are considered on-label experimental based on tumor type and mutation /characteristic            |
| offLabelExperimentalTreatments | Olaparib                 | A set of treatment names which are considered off-label experimental for specific tumor type and mutation /characteristic       |
| preClinicalTreatments          | New Drug A               | A set of treatment names which are pre-clinical and have some supportive evidence for tumor type and mutation / characteristic  |
| knownResistantTreatments       | Erlotinib                | A set of treatment names which are known to be resisted by the mutation for the specific tumor type                             |
| suspectResistantTreatments     | Erlotinib                | A set of treatment names for which there is some evidence that they may be resisted by the mutation for the specific tumor type |

## Interpretation to ACTIN molecular datamodel

### ORANGE

The interpretation of ORANGE to the ACTIN datamodel consists of two parts:

1. Annotating all mutations and various characteristics in ORANGE with additional gene annotation and clinical evidence.
2. Mapping all fields, annotated mutations and annotated characteristics to the ACTIN datamodel.

#### 1. Annotation of mutations and characteristics

#### Additional gene annotation

Every variant, copy number and disruption is annotated with `geneRole`, `proteinEffect` and `isAssociatedWithDrugResistance`. Furthermore,
every fusion is annotated with `proteinEffect` and `isAssociatedWithDrugResistance`.

The annotation algo tries to find the best matching entry from SERVE's mapping of the `CKB_EVIDENCE` database as follows:

- For variants the algo searches in the following order:
    - Is there a hotspot match for the specific variant? If yes, use hotspot annotation.
    - Is there a codon match for the specific variant's mutation type? If yes, use codon annotation.
    - Is there an exon match for the specific variant's mutation type? If yes, use exon annotation.
    - Else, fall back to gene matching.
- For copy numbers the algo searches in the following order:
    - Is there a copy number specific match? If yes, use copy number specific annotation.
    - Else, fall back to gene matching.
- For homozygous disruptions:
    - Is there copy number loss specific match? If yes, use copy number loss annotation.
    - Else, fall back to gene matching.
- For disruptions, a gene match is performed.
- For fusions, the algo searches in the following order:
    - Is there a known fusion with an exon range that matches the specific fusion? If yes, use fusion annotation.
    - Else, fall back to known fusion match ignoring specific exon ranges.

Do note that gene matching only ever populates the `geneRole` field. Any gene-level annotation assumes that the `proteinEffect` is unknown.

#### Evidence annotation

Every (potential) molecular driver and characteristic is annotated with evidence from SERVE. In practice all evidence comes
from `CKB_EVIDENCE`
except for
external trials which is populated by `CKB_TRIAL`. The evidence annotations occur in the following order:

1. Collect all on-label and off-label applicable evidences that match with the driver / characteristic
2. Map the evidences to the ACTIN evidence datamodel (above).

Evidence is considered on-label in case the applicable evidence tumor DOID is equal to or a parent of the patient's tumor doids, and none of
the patient's tumor DOIDs (or parents thereof) is blacklisted by the evidence.

Evidence from SERVE is collected per driver / characteristic according as follows:

| Driver / Characteristic        | Evidence collected                                                                                                                                                                                                                        |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| microsatellite status          | All signature evidence of type `MICROSATELLITE_UNSTABLE` in case tumor has MSI                                                                                                                                                            |
| homologous repair status       | All signature evidence of type `HOMOLOUG_RECOMBINATION_DEFICIENT` in case tumor is HRD                                                                                                                                                    |
| tumor mutational burden status | All signature evidence of type `HIGH_TUMOR_MUTATIONAL_BURDEN` in case tumor has high TMB                                                                                                                                                  |
| tumor mutational load status   | All signature evidence of type `HIGH_TUMOR_MUTATIONAL_LOAD` in case tumor has high TML                                                                                                                                                    |
| variant                        | In case the variant has `HIGH` driver likelihood: the union of all evidence matching for exact hotspot, matching on range and mutation type, and matching on gene level for events of type `ACTIVATION`, `INACTIVATION` or `ANY_MUTATION` |
| copy number                    | In case of an amplification, all gene level events of type `AMPLIFICATION`. In case of a loss, all gene level events of type `DELETION`                                                                                                   |
| homozygous disruption          | All gene level evidence of type `DELETION`, `INACTIVATION` or `ANY_MUTATION`                                                                                                                                                              | 
| disruption                     | All gene level evidence of type `ANY_MUTATION` in case the disruption is reported                                                                                                                                                         | 
| fusion                         | In case the fusion is reported, the union of promiscuous matches (gene level events of type `FUSION`, `ACTIVATION` or `ANY_MUTATION`) with fusion matches (exact fusion with fused exons in the actionable exon range)                    | 
| virus                          | For any reported virus, evidence is matched for `HPV_POSITIVE` and `EBV_POSITIVE`                                                                                                                                                         | 

The evidences are then mapped to the ACTIN evidence model as follows:

| Type of CKB evidence                      | Mapping in ACTIN evidence datamodel |
|-------------------------------------------|-------------------------------------|
| On-Label, certain responsive, A level     | Approved treatment                  | 
| On-label, uncertain responsive, A-level   | On-label experimental treatment     | 
| On-label, certain responsive, B-level     | On-label experimental treatment     |
| On-label, uncertain responsive, B-level   | Pre-clinical treatment              |
| On-label, responsive, C-level or D-level  | Pre-clinical treatment              |
| Off-label, responsive, A-level            | Off-label experimental treatment    | 
| Off-label, certain responsive, B-level    | Off-label experimental treatment    |
| Off-label, uncertain responsive, B-level  | Pre-clinical treatment              |
| Off-label, responsive, C-level or D-level | Pre-clinical treatment              |
| Resistant, A-level                        | Known resistant treatment           |
| Certain resistant, B-level                | Known resistant treatment           |
| Uncertain resistant, B-level              | Suspect resistant treatment         |
| Resistant, C-level or D-level             | Suspect resistant treatment         |

Notes:

- All responsive on-label evidence from `CKB_TRIAL` is mapped to external trials in ACTIN datamodel
- Responsive treatments are cleaned according to their evidence level. The highest evidence levels for each treatment are kept (such that an
  approved treatment cannot also be a pre-clinical treatment)
- Resistant treatments are retained only in case responsive evidence for the same treatment is present as well (either approved or
  experimental).

#### 2. Mapping of all ORANGE fields to ACTIN molecular datamodel

The ACTIN datamodel is created from the ORANGE data according to below.

Molecular base data:

| Field                | Mapping                                        |
|----------------------|------------------------------------------------|
| sampleId             | The ORANGE field `sampleId`                    |
| type                 | Hard-coded to `WGS`                            |
| refGenomeVersion     | Extracted from ORANGE field `refGenomeVersion` | 
| date                 | The ORANGE field `experimentDate`              |
| evidenceSource       | Hard-coded to `CKB_EVIDENCE`                   |
| externalTrialSource  | Hard-coded to `CKB_TRIAL`                      |
| containsTumorCells   | The PURPLE field `containsTumorCells`          |
| hasSufficientQuality | The PURPLE field `hasSufficientQuality`        |

Molecular characteristics:

| Field                        | Mapping                                                         |
|------------------------------|-----------------------------------------------------------------|
| purity                       | The PURPLE field `purity`                                       |
| ploidy                       | The PURPLE field `ploidy`                                       | 
| predictedTumorOrigin         | The CUPPA best cancer-type prediction along with the likelihood |
| isMicrosatelliteUnstable     | The interpretation of PURPLE `microsatelliteStabilityStatus`    |
| isHomologousRepairDeficient  | The interpretation of CHORD `hrStatus`                          |
| tumorMutationalBurden        | The PURPLE field `tumorMutationalBurden`                        |
| hasHighTumorMutationalBurden | The interpretation of PURPLE `tumorMutationalBurdenStatus`      |
| tumorMutationalLoad          | The PURPLE field `tumorMutationalLoad`                          |
| hasHighTumorMutationalLoad   | The interpretation of PURPLE `tumorMutationalLoadStatus`        |

Driver events:

| Driver Type           | Algo             | Details                                                                                                                            |
|-----------------------|------------------|------------------------------------------------------------------------------------------------------------------------------------|
| variants              | PURPLE           | Union of all somatic variants affecting a known gene and either reported or having a coding effect, and reported germline variants |
| copyNumbers           | PURPLE           | All somatic amplifications and losses affecting a known gene                                                                       |
| homozygousDisruptions | LINX             | All somatic homozygous disruptions affecting a known gene                                                                          |
| disruptions           | LINX             | All somatic gene disruptions affecting a known gene that is not also lost                                                          |
| fusions               | LINX             | All fusions that have a known gene either as 5' or 3' partner                                                                      |
| viruses               | VirusInterpreter | All viruses.                                                                                                                       |

Note that all floating point numbers are rounded to 3 digits when ingesting data into ACTIN:

- variants: `variantCopyNumber`, `totalCopyNumber`, `clonalLikelihood`
- disruptions: `junctionCopyNumber`, `undisruptedCopyNumber`

Other data:

The HLA entries are extracted from LILAC as follows:

- `isReliable` is set to true in case the LILAC QC value equals `PASS`
- For each HLA allele, the field `hasSomaticVariants` is set to true in case any of `somaticMissense`, `somaticNonsenseOrFrameshift`
  , `somaticSplice` or `somaticInframeIndel` is non-zero

The pharmacogenomics entries are extracted from PEACH.

### Version History and Download Links

- Upcoming (first release) 
