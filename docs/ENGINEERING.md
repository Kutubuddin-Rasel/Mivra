# ENGINEERING.md

# Medicine Scanner — Engineering Principles, Architecture & Decision Log

> **Purpose:** Preserve the technical reasoning behind the system.  
> **Important:** This is a living design document, not a claim that every described component already exists.

---

# 1. Engineering objective

Build the simplest system that can safely:

1. capture useful evidence from medicine packaging;
2. extract machine-readable observations;
3. rank candidate medicine products;
4. express uncertainty and abstain;
5. let the user confirm identity;
6. retrieve traceable medicine information.

The architecture must optimize for:

- safety;
- explainability;
- testability;
- evidence-driven evolution;
- local performance/privacy where practical;
- maintainability by a small engineering team.

---

# 2. Architecture principle: separate the problems

Do not treat “scan medicine” as one AI call.

The system contains distinct problems:

```text
IMAGE ACQUISITION
CameraX
     ↓
OBSERVATION EXTRACTION
OCR + barcode
     ↓
NORMALIZATION / PARSING
brand / strength / generic / manufacturer / form hints
     ↓
CANDIDATE GENERATION
find plausible catalog products
     ↓
CANDIDATE RANKING
combine evidence
     ↓
CONFIDENCE / AMBIGUITY POLICY
accept / candidates / abstain
     ↓
USER CONFIRMATION
     ↓
PRODUCT IDENTITY
     ↓
CLINICAL CONTENT RETRIEVAL
```

Each layer must be testable independently where possible.

---

# 3. Milestone 0 stack

Use only what is necessary to answer the feasibility question.

### Android

- Kotlin
- Jetpack Compose
- CameraX
- ML Kit Text Recognition
- ML Kit Barcode Scanning
- Coroutines
- ViewModel + StateFlow

### Temporary catalog

- small local JSON, in-memory objects, or Room if persistence becomes useful;
- exact choice should remain lightweight.

### Deliberately absent initially

- production backend;
- authentication;
- custom ML model;
- OpenCV preprocessing;
- microservices;
- complex encryption;
- cloud OCR by default.

---

# 4. Camera architecture

## 4.1 Use-case responsibilities

### Preview
Shows camera frames to the user.

### ImageCapture
Produces a higher-quality still image when the experiment requires one.

### ImageAnalysis
Provides frames to analysis code.

Do not assume all three are always necessary.

The prototype should teach and measure whether continuous analysis or explicit capture provides the best combination of:

- OCR quality;
- latency;
- battery use;
- implementation complexity;
- user control.

---

## 4.2 Frame ownership

Camera analysis must obey the lifecycle of `ImageProxy`.

A critical rule:

> Every analysis path must eventually close the frame.

Failure/error/cancellation paths must be tested, not only successful OCR.

---

## 4.3 Backpressure

The camera may produce frames faster than OCR can process them.

We must explicitly understand and choose a strategy rather than allow unbounded work.

Likely baseline:

- process the most recent useful frame;
- avoid accumulating stale frames;
- prevent parallel OCR jobs from overwhelming the analyzer unless deliberately benchmarked.

Exact CameraX configuration should be chosen while implementing Phase 1 and recorded here once accepted.

---

# 5. OCR architecture

## 5.1 OCR output is evidence, not identity

Example OCR:

```text
NAPA EXTENO
PARACETAMO
66S MG
BEXIMCO PHARMACEUTICAIS
```

This is not an error state by itself. It is a noisy observation.

The next layer must normalize and interpret it.

---

## 5.2 Preserve raw data

For debugging/benchmarking:

```text
raw OCR
normalized OCR
parsed observations
candidate results
```

should remain separately inspectable.

Never overwrite raw text with normalized text and lose the evidence needed for failure analysis.

---

## 5.3 Script/language strategy

Initial local baseline is Latin-script OCR because current ML Kit Text Recognition models are most directly suited to the English/Latin text commonly found on packaging.

Bangla display content and Bangla OCR are separate concerns.

If benchmark data shows Bengali-script OCR is materially necessary, evaluate alternatives such as:

- cloud OCR;
- alternative local OCR engines;
- specialized/custom models.

Do not commit to a fallback before measuring the actual need.

---

# 6. Image-processing architecture

OpenCV is **not** part of the baseline pipeline.

Introduce preprocessing only after a measured failure category exists.

Example experimental pattern:

```text
Problem:
low contrast on reflective foil

Baseline:
raw image → OCR

Treatment:
raw image → CLAHE → OCR

Compare:
field extraction
top-1/top-3
latency
new failure modes
```

An image transformation is accepted only if net benefit is demonstrated on held-out data.

A “prettier” image is not evidence of better OCR.

---

# 7. Observation model

The scanner should produce a structured observation rather than one flat string.

Conceptual model:

```kotlin
ScanObservation(
    rawText,
    normalizedText,
    brandTokens,
    genericTokens,
    strengths,
    manufacturerTokens,
    dosageFormHints,
    barcodeValues,
    imageQualitySignals
)
```

This is conceptual, not a frozen Kotlin API.

Important property:

> Identity matching should know which evidence came from which field or signal.

---

# 8. Normalization principles

Normalization may include:

- case folding;
- whitespace cleanup;
- punctuation cleanup;
- unit normalization (`mg`, `mcg`, `ml`, etc.);
- number/OCR confusion handling;
- token normalization;
- manufacturer suffix normalization;
- Unicode normalization;
- alias expansion only where evidence supports it.

Do not aggressively “correct” OCR in a way that invents information.

For example, turning every `S` near a number into `5` can create dangerous strength errors.

Any correction rule involving dosage strength must be conservative and tested.

---

# 9. Candidate matching

Candidate matching is two problems:

## 9.1 Candidate generation

Find plausible products without scanning the entire domain inefficiently.

Signals may include:

- exact/normalized brand;
- brand aliases;
- generic;
- strength;
- manufacturer;
- dosage form;
- barcode;
- token/fuzzy similarity.

## 9.2 Candidate ranking

Rank plausible candidates using interpretable evidence.

Illustrative model:

```text
barcode exact match        → very strong
brand exact/near match     → strong
strength match             → strong
generic match              → strong
manufacturer match         → supporting
dosage form match          → supporting
conflicting strength       → strong negative
conflicting barcode        → strong negative
```

Weights must be tuned from data.

Do not label an arbitrary score as “98% confidence” unless it has been calibrated against outcomes.

---

# 10. Confidence and abstention

The system needs a policy layer separate from raw candidate score.

Possible decisions:

```text
CONFIRMED-CANDIDATE-PRESENTATION
MULTIPLE-CANDIDATES
NEED-MORE-EVIDENCE
ABSTAIN
```

Examples of abstention triggers:

- insufficient readable text;
- candidate scores too close;
- recognized brand but missing distinguishing strength;
- conflicting evidence;
- no plausible catalog candidate;
- poor image quality;
- unsupported loose-pill input.

The threshold policy must be measured and documented in `EXPERIMENTS.md`.

---

# 11. User confirmation

For the MVP, recognition is not complete until the user confirms the candidate.

Confirmation UI should expose safety-relevant distinctions:

- brand;
- generic;
- strength;
- dosage form;
- manufacturer where useful.

Do not show three visually identical candidate rows distinguished only by an internal ID.

---

# 12. Barcode handling

Barcode scanning answers:

> “What encoded value exists?”

It does not automatically answer:

> “What medicine is this?”

We need a trusted mapping between encoded identifiers and canonical product identity.

Barcode data is one evidence source, not magic.

---

# 13. Product identity domain

Conceptual entities:

```text
MedicineProduct
GenericDrug
ActiveIngredient
Manufacturer
DosageForm
Strength
ProductAlias
PackageVariant
BarcodeMapping
RegistrationRecord
SourceRecord
```

A later normalized schema may differ.

Key principle:

> A product/brand record must not be confused with the generic drug/ingredient.

---

# 14. Clinical-content domain

Clinical information is a separate bounded concern.

Conceptual records:

```text
ClinicalMonograph
Indication
SideEffect
Warning
Precaution
Contraindication
GeneralUsageAdvice
Source
ContentVersion
LocalizedContent
ReviewRecord
```

Important metadata:

- source;
- source version/date;
- content version;
- language;
- review status;
- reviewer/process;
- last reviewed date.

---

# 15. LLM policy

A generative model may assist internal work such as:

- converting validated source material into draft plain language;
- creating Bangla draft localization;
- structuring content;
- identifying areas requiring human review.

But the production app must not do:

```text
scan → LLM → unreviewed medical fact → user
```

The trusted output path should be closer to:

```text
authoritative source
      ↓
structured content
      ↓
optional assisted simplification
      ↓
review
      ↓
versioned approved content
      ↓
app
```

---

# 16. Local vs server responsibilities

## Prefer on-device initially for

- camera acquisition;
- OCR where supported;
- barcode detection;
- temporary scan state;
- possibly candidate matching against a compact local catalog/cache;
- privacy-sensitive preprocessing.

## Likely server responsibilities later

- canonical medicine catalog;
- catalog updates/versioning;
- authoritative identity/search service;
- clinical content;
- provenance;
- review/version data;
- analytics aggregation where lawful/appropriate.

Exact split should be benchmarked.

---

# 17. Offline-first direction

The product should remain usable for basic recognition in poor connectivity where practical.

Potential eventual model:

```text
device
├── OCR
├── barcode
├── cached identity catalog/index
├── candidate matching
└── cached recent medicine content

server
├── canonical catalog
├── latest content
├── updates
└── review/provenance
```

This is a direction, not a Phase 1 requirement.

---

# 18. Backend principles

Backend technology is intentionally deferred until the recognition experiment proves the product core.

When chosen, the backend should emphasize:

- clear domain boundaries;
- PostgreSQL as likely canonical relational store;
- deterministic validation;
- idempotent data import/update jobs;
- search/indexing appropriate to normalized medicine names;
- structured logs;
- metrics;
- rate limiting;
- API versioning strategy where needed;
- tests for catalog matching and content provenance.

Do not start with microservices.

A modular monolith is the default unless evidence proves separate deployable services are needed.

---

# 19. Search/database direction

Possible later PostgreSQL features to evaluate:

- normalized columns;
- indexes for exact lookups;
- trigram similarity for candidate generation;
- full-text search where it adds value;
- immutable canonical IDs;
- alias tables;
- source/version tables.

Do not choose Elasticsearch or a specialized vector database until search requirements show PostgreSQL is insufficient.

---

# 20. Security architecture

Security is proportional to the data we actually store.

## Phase 1

- normal Android app sandbox;
- no user medication history;
- no embedded API secrets;
- no unnecessary image upload.

## When network/backend arrives

- TLS only;
- validate server certificates using standard platform behavior;
- disable accidental cleartext where appropriate;
- protect server secrets outside the app;
- authorization on the server, never only in UI;
- rate limiting and request validation.

## When sensitive local user data arrives

Perform a threat model before choosing encryption.

Likely concepts:

- Android Keystore for long-term cryptographic key protection;
- authenticated encryption such as AES-GCM where application-level encryption is justified;
- safe token storage;
- database/file protection strategy;
- data deletion/retention rules.

Do not implement homemade cryptography.

---

# 21. Privacy architecture

Default posture:

- process image locally where practical;
- do not keep a scan photo after the operation unless the user/research workflow explicitly requires it;
- separate benchmark/research images from production;
- minimize logs;
- do not log medication history or raw image content casually;
- give users clear control over saved history when that feature exists;
- collect only analytics necessary to improve safety/product quality.

---

# 22. Testing strategy

## Unit tests

High priority:

- text normalization;
- strength parsing;
- unit parsing;
- alias handling;
- OCR confusion rules;
- candidate generation;
- candidate ranking;
- ambiguity policy;
- abstention policy.

## Golden/benchmark tests

Run the same labeled dataset through:

- OCR versions;
- preprocessing variants;
- matcher versions.

This protects against “improvements” that silently worsen another packaging category.

## Android/instrumentation tests

Use for:

- permission flow;
- lifecycle-sensitive UI;
- navigation;
- camera integration where practical.

## Backend tests later

- domain rules;
- import jobs;
- API validation;
- search;
- authorization;
- provenance/version rules.

---

# 23. Performance

Important early metrics:

- OCR processing latency;
- end-to-end scan latency;
- analyzer frame rate;
- memory pressure;
- battery/thermal behavior during prolonged camera use;
- app startup;
- local catalog search latency.

Do not optimize without measurements.

---

# 24. Observability

The experiment should make invisible failures visible.

Diagnostic data may include:

- OCR duration;
- raw extracted text;
- normalized text;
- matched candidate scores;
- ambiguity reason;
- image-quality tags;
- failure category.

Production observability must be more privacy-restricted than the research build.

---

# 25. Dependency policy

Before adding a library, record:

- problem solved;
- why standard platform tools are insufficient;
- maintenance health;
- license;
- binary/app-size cost where relevant;
- security implications.

Avoid large libraries for trivial utility functions.

---

# 26. Code-organization principle

Do not create “Clean Architecture theatre.”

Use boundaries that correspond to real responsibilities.

A likely evolution:

```text
ui/
camera/
recognition/
normalization/
matching/
catalog/
medicine/
data/
```

Exact modules/packages should emerge as complexity appears.

Do not create 10 Gradle modules in Milestone 0.

---

# 27. Failure-handling principle

Every external or uncertain subsystem needs a user-safe failure:

- camera unavailable;
- permission denied;
- OCR failure;
- unreadable image;
- no catalog match;
- multiple close candidates;
- network offline;
- content unavailable;
- stale catalog.

“No result” is a valid state.

---

# 28. Decision log

## D-001 — Android first
**Status:** Accepted  
**Reason:** Bangladesh-first target, existing native Android experience, strong native camera/ML ecosystem, and learning value.

## D-002 — ChatGPT as primary tutor
**Status:** Accepted  
**Reason:** Project optimizes for learning and reasoning continuity, not only repository-local code generation.

## D-003 — Antigravity as repo-local workbench
**Status:** Accepted  
**Reason:** Strong repository visibility/debugging while avoiding multiple competing project leads.

## D-004 — Codex as optional independent reviewer
**Status:** Accepted  
**Reason:** Valuable second opinion without making daily progress dependent on usage availability.

## D-005 — Documentation is the long-term source of truth
**Status:** Accepted  
**Reason:** Chat context and AI memory are not reliable project-state storage.

## D-006 — ML Kit OCR before OpenCV
**Status:** Accepted  
**Reason:** Need a measurable raw baseline before evaluating preprocessing.

## D-007 — No custom ML model initially
**Status:** Accepted  
**Reason:** OCR + structured matching may solve the problem with lower complexity; custom model must be earned by evidence.

## D-008 — No production backend during initial OCR feasibility
**Status:** Accepted  
**Reason:** Isolate camera/OCR/matcher uncertainty before introducing network/database complexity.

## D-009 — Packaging-first recognition
**Status:** Accepted  
**Reason:** Boxes, bottles, and printed strips contain identity signals; arbitrary loose pills may not.

## D-010 — Abstention is a first-class output
**Status:** Accepted  
**Reason:** False confident identification is more dangerous than refusing an uncertain scan.

## D-011 — User confirmation in MVP
**Status:** Accepted  
**Reason:** Adds a human verification checkpoint before medicine information is treated as selected.

## D-012 — Product identity and clinical content are separate domains
**Status:** Accepted  
**Reason:** Identity data and medical knowledge have different sources, update cycles, safety requirements, and structure.

## D-013 — No live LLM as clinical-fact authority
**Status:** Accepted  
**Reason:** Trusted medicine information must be source-grounded, versioned, and reviewable.

## D-014 — Backend framework deferred
**Status:** Accepted  
**Reason:** Recognition/data requirements should shape backend architecture; choosing too early creates technology-led design.

## D-015 — Security complexity follows the data threat model
**Status:** Accepted  
**Reason:** Learn and use Keystore/encryption when sensitive persisted user data justifies it, not as résumé decoration.

---

# 29. Open decisions

These remain intentionally unresolved:

- continuous `ImageAnalysis` vs explicit high-quality `ImageCapture` as primary OCR interaction;
- exact local test-catalog persistence approach;
- exact candidate-scoring formula;
- confidence calibration method;
- whether OpenCV is needed;
- whether Bengali OCR fallback is needed;
- backend framework;
- exact database schema;
- local catalog size/offline strategy;
- account requirement;
- cloud provider;
- production telemetry provider.

Resolve them through implementation evidence, not preference alone.

---

# 30. Engineering rule

> **Keep the recognition pipeline observable, deterministic where possible, and capable of saying “I do not know.”**
