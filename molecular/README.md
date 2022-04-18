## ACTIN-Molecular

ACTIN-Molecular interprets molecular results and maps to the datamodel described below. This molecular model is written to a per-sample 
json file. Currently, ACTIN-Molecular only supports interpretation of [ORANGE](https://github.com/hartwigmedical/hmftools/tree/master/orange) 
results as produced by [HMF Platinum](https://github.com/hartwigmedical/platinum) 

The ORANGE interpreter application requires Java 11+ and can be run as follows: 

```
java -cp actin.jar com.hartwig.actin.molecular.orange.OrangeInterpreterApplication \
   -orange_json /path/to/orange.json \
   -serve_bridge_tsv /path/to/serve_bridge_output_tsv \
   -output_directory /path/to/where/molecular_json_file_is_written
```

The following assumptions are made about the inputs:
 - The ORANGE json is the json output from [ORANGE](https://github.com/hartwigmedical/hmftools/tree/master/orange)
 - The SERVE bridge TSV is the output of [ACTIN-SERVE-Bridge](../serve-bridge/README.md)

## Molecular Datamodel

Every sample, uniquely defined by their sample ID, has a molecular record with the following data:

Field | Example Value | Details
---|---|---
type | WGS | The type of molecular experiment done. Currently only 'WGS' is supported.
date | 2022-01-14 | The date on which the molecular results were obtained (optional field).
hasReliableQuality | 1 | Whether the molecular results have reliable quality. 
 
1 molecular characteristics

Field | Example Value | Details
---|---|---
purity | 78% | The percentage of cells in the sequenced biopsy that originated from the tumor.
hasReliablePurity | 1 | Indicates whether the purity estimate can be trusted.
predictedTumorOrigin | Melanoma (87%) | The tumor type of origin predicted based on the molecular data along with a likelihood. 
isMicrosatelliteUnstable | 0 | If 1, sample is considered microsatellite unstable. Can be left blank in case experiment does not determine MSI.
isHomologousRepairDeficient | 0 | If 1, sample is considered homologous repair deficient. Can be left blank in case experiment does not determine HRD.
tumorMutationalBurden | 14.2 | Number of mutations in the genome per Mb. Can be left blank in case experiment does not determine TMB.
tumorMutationalLoad | 115 | Number of missense mutations across the genome. Can be left blank in case experiment does not determine TML.

N variants  

Field | Example Value | Details
---|---|---
gene | APC | The gene impacted by the variant
impact | p.D1174fs | The impact of the variant on the gene 
variantCopyNumber | 3.8 | The number of copies of this variant in the tumor
totalCopyNumber | 4.0 | The total number of copies in the tumor on the mutated position
driverType | BIALLELIC | Either `HOTSPOT`, `BIALLELIC` or `VUS`
driverLikelihood | 93% | Likelihood that the combined set of variants on the impacted gene are considered a driver.
clonalLikelihood | 100% | Likelihood that the variant exists in every tumor cell (is clonal).

N amplifications

Field | Example Value | Details
---|---|---
gene | MYC | The gene that has been amplified
copies | 150 | Number of copies of this gene in the tumor
isPartial | 0 | Indicates whether the gene has been partially or fully amplified in the tumor

N losses

Field | Example Value | Details
---|---|---
gene | TP53 | The gene that has been lost in the tumor 
isPartial | 1 | Indicates whether the gene has been partially or fully lost in the tumor 

N disruptions

Field | Example Value | Details
---|---|---
gene | BRCA1 | The gene that has been disrupted.
isHomozygous | 1 | Indicates whether the disruption leads to no wildtypes present anymore.
details | Intron 12 downstream | Provides additional details about the disruption.

N fusions

Field | Example Value | Details
---|---|---
fiveGene | EML4 | The gene that makes up the 5' part of the fusion
threeGene | ALK | The gene that makes up the 3' part of the fusion
details | Exon 2 - Exon 5 | Additional details about the fusion
driverType | KNOWN | Either `KNOWN` or `PROMISCUOUS`
driverLikelihood | HIGH | Either `HIGH` or `LOW` 

N viruses

Field | Example Value | Details
---|---|---
name | Human papillomavirus type 16 | The name of the virus found in the tumor
details | 3 integrations detected | More details about the virus
driverLikelihood | HIGH | Either `HIGH` or `LOW`

N pharmaco

Field | Example Value | Details
---|---|---
gene | DPYD | The gene for which the pharmaco entry is applicable
haplotype | 1* HOM | The haplotype of the gene found in the germline data of the patient. 

1 molecular evidence

Field | Example Value | Details
---|---|---
actinTrials | BRAF V600E -> Trial A | A list of mutations along with the trial they are associated with within the ACTIN treatment database.
externalTrialSource | CKB | The name of the source that has been used for external trials
externalTrials | High TMB -> Trial B | A list of mutations along with the trial they are associated with.
evidenceSource | CKB | The name of the source used for general evidence
approvedEvidence | PIK3CA E545K -> Alpelisib | A list of mutations along with approved evidence for treatment based on data from `evidenceSource`
onLabelExperimentalEvidence | - | A list of mutations along with on-label experimental evidence for treatment based on data from `evidenceSource`
offLabelExperimentalEvidence | - | A list of mutations with evidence that is experimental for a different tumor type based on data from `evidenceSource`.
preClinicalEvidence | - | A list of mutations with evidence that is pre-clinical based on data from `evidenceSource`.
knownResistanceEvidence | KRAS amp -> Erlotinib | A list of mutations along with known resistance evidence for treatment based on data from `evidenceSource`.
suspectResistanceEvidence | - | A list of mutations along with suspect resistance evidence for treatment based on data from `evidenceSource`.

Finally, the following mapped events are used for actual matching against ACTIN's treatment eligibility criteria.
Do note that ACTIN itself does not make assumptions about the exact definition of the terms below. 
These decisions are all up to the algorithm interpreting the molecular data and mapping this to the datamodel that is described here.

Field | Example Value | Details
---|---|---
mutations | BRAF V600E, EGFR exon 19 deletion | A list of gene-specific mutations that should be matchable to the molecular rules as defined in the treatment database.
activatedGenes | KRAS, NRAS | A list of genes considered to be activated.
inactivatedGenes | TP53, RB1 | A list of genes considered to be inactivated (along with a boolean whether they have been deleted completely).
amplifiedGenes | MYC | A list of genes considered to be amplified.
wildtypeGenes | BRAF | A list of genes considered to be wildtype. 
fusions | EML4-ALK | A list of genes considered to be fused together.

### Interpretation of ORANGE results

The ORANGE interpreter application maps the ORANGE output to the molecular datamodel as follows:

Field | Mapping
---|---
sampleId | The ORANGE field `sampleId`
type | Hard-coded to WGS 
date | The ORANGE field `reportedDate`
hasReliableQuality | The PURPLE field `hasReliableQuality` 

The characteristics are extracted as follows:

Field | Mapping
---|---
purity | The PURPLE field `purity`
hasReliablePurity | The PURPLE field `hasReliablePurity`
predictedTumorOrigin | The CUPPA best cancer-type prediction along with the likelihood
isMicrosatelliteUnstable | The interpretation of PURPLE `microsatelliteStabilityStatus`
isHomologousRepairDeficient | The interpretation of CHORD `hrStatus`
tumorMutationalBurden | The PURPLE field `tumorMutationalBurden`
tumorMutationalLoad | The PURPLE field `tumorMutationalLoad`

The drivers are extracted from the following algorithms:
Driver Type | Algo | Details
---|---|---
variants | PURPLE | Union of reportable somatic variants and reportable germline variants.
amplifications | PURPLE | Union of reportable somatic full gains and reportable somatic partial gains.
losses | PURPLE | Union of reportable somatic full losses and reportable somatic partial losses.
disruptions | LINX | Union of reportable somatic gene disruptions and reportable somatic homozygous disruptions.
fusions | LINX | All reportable fusions with driver type mapped to either `KNOWN` or `PROMISCUOUS`.
viruses | VirusInterpreter | All reportable viruses.

The pharmaco entries are extracted from PEACH.

The evidence is extracted from PROTECT in the following steps:
 1. Evidence is filtered for applicability
 1. Reported evidence is used exclusively for all following steps
 1. Evidence is categorized using below table
 
Field | Source | Filter
---|---|---
actinTrials | ACTIN | Evidence is filtered that is used exclusively in exclusion rules within ACTIN  
externalTrials | ICLUSION | -
approvedEvidence | CKB | A-level on-label non-predicted responsive evidence.
onLabelExperimentalEvidence | CKB | A-level that is either predicted or off-label responsive and B-level on-label non-predicted responsive evidence.
offLabelExperimentalEvidence | CKB | B-level off-label non-predicted responsive evidence.
preClinicalEvidence | CKB | All responsive evidence that is not approved or experimental. 
knownResistanceEvidence | CKB | A or B-level non-predicted resistance evidence for a treatment for which non-preclinical evidence exists with equal or lower evidence level.  
suspectResistanceEvidence | CKB | Any not-known resistance for a treatment with evidence with equal or lower evidence level.    

The events that are used for ACTIN treatment matching are extracted from the PROTECT part of the ORANGE datamodel. It is assumed that PROTECT
has been run on a SERVE database that includes an ACTIN source generated from the ACTIN treatment database using [serve-bridge](../serve-bridge).
This setup allows PROTECT to determine classification for each of the relevant genes and mutations in the ACTIN treatment database.

The following classifications are extracted from the ACTIN-sourced evidence in PROTECT:
 - mutations: Any reported evidence of type `HOTSPOT_MUTATION`, `CODON_MUTATION` and `EXON_MUTATION` is included in the list of mutations.
 A mapping is performed from the PROTECT evidence to a mutation string that matches with the input mutation string in the ACTIN treatment 
 database. 
 - activated genes: Includes any gene with reported evidence of type `ACTIVATION`.
 - inactivated genes: Includes any gene with reported evidence of type `INACTIVATION`. Based on the actual PROTECT event it is 
 determined whether the gene has been deleted or not.
 - amplified genes: Includes any gene with reported evidence of type `AMPLIFICATION`
 - wildtype genes: Not implemented yet
 - fusions: Include any fusion with reported evidence of type `FUSION_PAIR` or `PROMISCUOUS_FUSION`
 

### Version History and Download Links
 - Upcoming (first release) 
