# ROADMAP.md

# Medicine Scanner — Learning & Delivery Roadmap

> **Purpose:** Keep the project moving in a deliberate order, prevent scope creep, and make learning objectives explicit.  
> **Current stage:** Phase 1 — Recognition Feasibility  
> **Rule:** Do not advance because a feature “looks done.” Advance when the phase exit criteria are satisfied.

---

## 1. Roadmap philosophy

This roadmap is intentionally **evidence-driven**.

The project contains several uncertainties:

- real-world OCR quality;
- reflective blister-strip handling;
- candidate-matching reliability;
- medicine-data provenance;
- safe confidence/abstention behavior;
- clinical-content workflow.

We therefore build the risky core first.

The sequence is:

```text
Understand
   ↓
Measure baseline
   ↓
Find failure modes
   ↓
Improve only proven problems
   ↓
Validate data
   ↓
Build MVP architecture
   ↓
Add security/history only when needed
   ↓
Closed beta
   ↓
Production hardening
```

---

# Phase 0 — Product & Project Foundation

## Goal

Define the product, tutoring model, safety boundaries, architecture principles, and experimental process before implementation becomes large.

## Learning objectives

- understand why product scope affects technical architecture;
- learn the difference between product requirement, engineering decision, and experiment;
- learn lightweight documentation discipline.

## Deliverables

- [x] `AGENTS.md`
- [x] `docs/PROJECT.md`
- [x] `docs/ROADMAP.md`
- [x] `docs/ENGINEERING.md`
- [x] `docs/EXPERIMENTS.md`
- [x] Android-first decision
- [x] ML Kit baseline-before-OpenCV decision
- [x] safety/abstention principle
- [x] no live LLM as medicine-fact authority

## Exit criteria

- product scope is explicit;
- current experiment is clear;
- AI roles are explicit;
- no major ambiguity exists about what the first prototype is supposed to prove.

**Status:** Complete once these files are accepted into the repository.

---

# Phase 1 — Recognition Feasibility (Milestone 0)

> **This is the active phase.**

## Goal

Answer:

> **Can an Android device extract enough information from real medicine packaging to identify the correct product with high precision and safe abstention?**

This phase produces an engineering experiment, not a polished app.

---

## 1.1 CameraX fundamentals

### Learn

- camera lifecycle;
- `Preview`;
- `ImageCapture`;
- `ImageAnalysis`;
- image rotation;
- analyzer execution;
- backpressure;
- thread/executor choices;
- `ImageProxy` ownership and closing;
- permission handling.

### Build

A minimal Compose screen with:

- camera preview;
- permission flow;
- capture/analyze control;
- diagnostic status;
- no production navigation system.

### Definition of done

- camera preview works reliably;
- lifecycle survives basic background/foreground transitions;
- no obvious frame/resource leak;
- the human engineer can explain the three CameraX use cases and why this prototype chooses its current one(s).

---

## 1.2 Raw ML Kit OCR baseline

### Learn

- OCR pipeline;
- `InputImage`;
- text blocks / lines / elements;
- rotation metadata;
- asynchronous processing;
- latency;
- why OCR output is noisy data rather than a medicine identity.

### Build

Diagnostic result screen showing:

- raw recognized text;
- text blocks/lines;
- optional bounding boxes;
- processing duration;
- image metadata useful for debugging.

### Important constraint

**No OpenCV preprocessing yet.**

We need a baseline.

### Definition of done

- the app can capture/analyze a package and display OCR results;
- failure/error state is visible;
- repeated processing does not leak image frames;
- sample scans are saved only according to the research dataset protocol.

---

## 1.3 Barcode baseline

### Learn

- supported barcode types;
- barcode detection vs barcode-to-product mapping;
- on-device recognition;
- how barcode evidence differs from OCR evidence.

### Build

Record barcode value when present.

Do **not** assume a decoded number identifies a medicine unless a trusted mapping exists.

### Definition of done

- barcode presence/absence can be measured;
- useful barcode values can be attached to benchmark records.

---

## 1.4 Benchmark dataset v0

### Goal

Create a small but meaningful real-world dataset.

### Initial target

- roughly 100 distinct medicine products;
- roughly 400–600 photos;
- multiple packaging types and conditions.

These are planning targets, not requirements to hit an exact number.

### Variation to include

- box / blister / bottle;
- normal / low / harsh lighting;
- reflective foil;
- rotation;
- perspective;
- close/far framing;
- blur;
- partial/cut strips;
- English/bilingual packaging;
- same-brand different-strength variants;
- same-generic different-brand products.

### Definition of done

Every benchmark image has ground-truth metadata and failure-condition tags.

---

## 1.5 OCR field extraction & normalization

### Learn

- normalization;
- tokenization;
- Unicode/case handling;
- dosage/strength parsing;
- OCR-confusion patterns;
- why domain-aware parsing beats raw string comparison.

### Build

Transform raw OCR into structured observations where possible:

```text
brand-like tokens
generic-like tokens
strength
unit
manufacturer-like tokens
dosage-form hints
barcode
```

### Definition of done

- normalization is covered by unit tests;
- extraction results are inspectable;
- raw OCR is never destroyed — keep both raw and normalized representations in the benchmark output.

---

## 1.6 Candidate matcher v0

### Learn

- exact matching;
- fuzzy matching;
- edit distance limitations;
- token similarity;
- field weighting;
- candidate generation vs candidate ranking;
- deterministic scoring;
- confidence vs “made-up percentage.”

### Build

Start with a small local catalog.

Input:

```text
noisy OCR observations
```

Output:

```text
ranked candidate list
evidence used
score
ambiguity flags
```

### Safety requirement

The matcher must support:

- multiple candidates;
- “insufficient evidence”;
- abstention.

### Definition of done

- top-1/top-3 metrics can be calculated;
- scoring reasons are inspectable;
- variants differing by strength/form are handled explicitly;
- tests include intentionally ambiguous inputs.

---

## 1.7 Feasibility report

At phase end, produce results for:

- OCR field extraction;
- top-1 accuracy;
- top-3 recall;
- high-confidence precision;
- false confident matches;
- abstention rate;
- latency;
- failure categories;
- barcode contribution;
- package-type differences.

### Exit decision

Choose one:

#### GREEN
Raw OCR + matching is strong enough to continue toward product architecture.

#### YELLOW
Feasible, but specific failure modes require targeted preprocessing/OCR fallback experiments.

#### RED
Core recognition is too unreliable; revise recognition strategy before building the app.

### Things explicitly NOT included in Phase 1

- production backend;
- user accounts;
- medication history;
- personalized medical advice;
- polished product UI;
- custom TensorFlow model;
- generalized loose-pill recognition;
- complex encryption.

---

# Phase 2 — Targeted Image/OCR Improvement

> Enter only if Phase 1 evidence justifies it.

## Goal

Improve specific measured recognition failures without creating an uncontrolled image-processing stack.

## Learning objectives

- OpenCV image matrices and color spaces;
- blur/contrast measures;
- CLAHE;
- thresholding;
- adaptive thresholding;
- morphology;
- deskew/perspective correction;
- ROI extraction;
- benchmarking an image transformation.

## Experiment candidates

- low-contrast foil → CLAHE;
- glare → glare detection / recapture UX before complex correction;
- rotation/perspective → deskew/crop;
- tiny text → higher-resolution capture or ROI guidance;
- blur → reject/recapture instead of “fixing” impossible input.

## Rule

Each transformation must be compared against the Phase 1 baseline.

## Exit criteria

- accepted preprocessing steps have measurable value;
- rejected steps are documented;
- the final recognition pipeline stays as simple as evidence allows.

---

# Phase 3 — Data Foundation

## Goal

Build a legitimate, maintainable medicine identity catalog and define the clinical-information source pipeline.

## Learning objectives

- data provenance;
- schema normalization;
- source licensing;
- canonical identifiers;
- entity resolution;
- versioning;
- import/update jobs;
- auditability.

## Workstreams

### 3.1 Identity-source validation

Evaluate:

- DGDA/national identifiers;
- official/open/licensed Bangladesh datasets;
- manufacturer data where legitimately usable;
- barcode mappings.

### 3.2 Canonical domain model

Model separately:

- product;
- generic/ingredient;
- strength;
- dosage form;
- manufacturer;
- package/alias;
- barcode;
- registration/source metadata.

### 3.3 Clinical content pipeline

Define:

- source hierarchy;
- localization;
- simplification;
- pharmacy/medical review;
- versioning;
- last-reviewed status;
- withdrawal/update process.

## Exit criteria

- no core feature depends on unauthorized scraping;
- product identity has a stable canonical model;
- clinical content provenance is explicit;
- update strategy is understood.

---

# Phase 4 — MVP Architecture & Backend

## Goal

Turn the proven recognition experiment into a maintainable application architecture.

## Learning objectives

- modular application architecture;
- API design;
- PostgreSQL indexing/search;
- cache boundaries;
- synchronization;
- domain services;
- validation;
- observability;
- backend testing.

## Decisions deliberately deferred until this phase

- exact backend framework;
- production DI structure;
- server-side search technology;
- cache technology;
- cloud provider;
- deployment topology.

These should be chosen based on proven needs, not preference alone.

## Likely capabilities

- medicine identity API;
- candidate search/matching service;
- medicine detail API;
- catalog update/version endpoints;
- provenance/review metadata;
- offline-cache synchronization support.

## Explicit non-goal

No microservices unless a real operational boundary earns them.

---

# Phase 5 — Android MVP Productization

## Goal

Create the first coherent consumer product.

## Learning objectives

- production Compose navigation;
- state management;
- repository boundaries;
- Room caching;
- offline-first decisions;
- robust error/retry UX;
- accessibility;
- localization;
- performance profiling.

## MVP screens

Likely:

1. Scan
2. Candidate confirmation
3. Medicine details
4. Manual search
5. Basic settings/language
6. About/sources/safety information

Do not add account creation unless a feature genuinely needs identity/sync.

## Exit criteria

A user can:

- install app;
- scan supported packaging;
- resolve ambiguity;
- confirm medicine;
- read reviewed information;
- fall back to manual search;
- understand when the system is uncertain.

---

# Phase 6 — Security, Privacy & Sensitive Features

> Some security exists from day one. This phase refers to the deeper work required when sensitive persistent user data is added.

## Goal

Introduce accounts/history only with a documented threat model.

## Learning objectives

- threat modeling;
- Android Keystore;
- authenticated encryption;
- token storage;
- TLS/network security;
- server authorization;
- secrets management;
- data retention/deletion;
- audit logging;
- privacy-preserving telemetry.

## Candidate features

- saved medicines;
- scan history;
- family medication list;
- sync/account;
- reminders.

## Exit criteria

- sensitive data inventory exists;
- threat model exists;
- retention/deletion behavior exists;
- no secret embedded in APK;
- security tests/review completed.

---

# Phase 7 — Closed Beta

## Goal

Validate real user behavior and trust.

## Target participants

A small controlled group including:

- ordinary users;
- caregivers;
- older users where practical;
- pharmacists/health professionals for qualitative review.

## Measure

- successful identification;
- recapture;
- manual fallback;
- correction;
- comprehension;
- Bangla usefulness;
- dangerous misunderstandings;
- trust;
- unexpected use cases.

## Exit criteria

No known systematic safety failure remains hidden behind aggregate accuracy.

---

# Phase 8 — Production Hardening

## Goal

Prepare for public release only after the product and safety model are credible.

## Work

- current Play policy review;
- current Bangladesh regulatory review;
- privacy policy;
- medical disclaimer/positioning;
- source/update operations;
- crash/ANR monitoring;
- telemetry review;
- accessibility audit;
- device testing;
- security review;
- incident/update process.

---

# Learning map

| Area | First serious phase |
|---|---|
| CameraX | Phase 1 |
| ML Kit OCR | Phase 1 |
| Barcode scanning | Phase 1 |
| OCR parsing | Phase 1 |
| Fuzzy/entity matching | Phase 1 |
| Benchmarking | Phase 1 |
| OpenCV | Phase 2, if earned |
| Medicine data modeling | Phase 3 |
| Backend/API | Phase 4 |
| PostgreSQL/search | Phase 4 |
| Offline-first/Room | Phase 5 |
| Keystore/encryption | Phase 6 |
| Threat modeling | Phase 6 |
| Product analytics | Phase 7 |
| Release/regulatory hardening | Phase 8 |

---

# Current next actions

1. Create the Android experiment repository.
2. Add these five documentation files.
3. Create a minimal Android app.
4. Learn and implement CameraX preview correctly.
5. Do **not** integrate OCR until the camera/lifecycle behavior is understood.
6. Add ML Kit OCR.
7. Begin benchmark dataset protocol.
8. Record the first experiment in `EXPERIMENTS.md`.

---

# Roadmap change rule

A phase may be changed when:

- experiment evidence invalidates an assumption;
- a safety issue is discovered;
- a data/legal constraint appears;
- a simpler path becomes clearly superior;
- a new product insight is validated.

When the roadmap changes, record the reason in `ENGINEERING.md` or `EXPERIMENTS.md`, not only in chat.
