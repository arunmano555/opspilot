# OpsPilot

An AI assistant for backend operations, built step by step with Java 25, Spring Boot 4.1 and Spring AI 2.0:
- **Docs assistant** — answers questions over engineering docs and runbooks, with cited sources (Phase 3).
- **Ops agent** — inspects a demo service's health and logs, diagnoses planted bugs, proposes a fix for human approval (Phase 5).

## Project layout

| Path | What lives there |
|---|---|
| `src/main/java/com/arunmano/opspilot/<feature>/` | Product code, one package per feature (`playground`, later `tools`, `rag`, `agent`) |
| `src/main/java/com/arunmano/opspilot/experiments/phaseN/` | Learning experiments per phase. Profile-gated or `main()`-only, so they never run by default |
| `docs/phase-N.md` | "What I built and why" per phase: findings with numbers |

## Running

```bash
export GEMINI_API_KEY=<your key>
./mvnw spring-boot:run                                         # the app (no experiments run)
./mvnw spring-boot:run -Dspring-boot.run.profiles=<profile>    # one experiment
```

| Experiment | Phase | How to run | Finding |
|---|---|---|---|
| `RawGeminiCall` | 0 | `main()` | What the raw REST call looks like |
| `HelloChatClient` | 0 | profile `hello` | What ChatClient hides; thinking tokens missing from `getCompletionTokens()` |
| `TokenCounter` | 1 | profile `tokens` | Token cost of UUIDs, stack traces, missing spaces |
| `MaxOutputTokensExperiment` | 1 | profile `maxtokens` | Cap covers thinking + answer → empty 200 with `MAX_TOKENS` |
| `SamplingExperiment` | 1 | profile `sampling` (+ `--sampling.mode=temperature`) | T=0 / topK=1 still not deterministic |
| `SamplingExperimentRawHTTP` | 1 | `main()` | Same without Spring AI → framework ruled out |

Model: `gemini-3.5-flash-lite` (set via `spring.ai.google.genai.chat.model`).

## Phase 0 — Hello LLM

Details: [docs/phase-0.md](docs/phase-0.md)

### What it does
- `RawGeminiCall`: sends one prompt to the Gemini REST API using plain `java.net.http`
  (no framework), and prints the raw JSON response.
- `HelloChatClient`: sends the same prompt through Spring AI's `ChatClient` and prints the
  text, `finishReason`, model, Spring's normalized `Usage`, and Gemini's native usage metadata.

### What ChatClient hides
- Endpoint URL, with the model name in the path
- Headers: `x-goog-api-key`, `Content-Type`
- HTTP method (POST)
- JSON payload: `contents`/`parts`/`role`, `systemInstruction`, `generationConfig` (incl. thinking config)
- Timeout (the default isn't obvious)
- The exact request on the wire. A 200 can hide a wrong request (dropped system prompt, option not applied)

### Finding: Spring AI under-reports Gemini output tokens
With thinking enabled (level HIGH), one call reported:

| Source | Prompt | Output (visible) | Thinking | Total |
|---|---|---|---|---|
| Gemini native | 22 | 397 | 878 | 1297 |
| Spring AI `Usage` | 22 | 397 (`getCompletionTokens`) | not shown | 1297 |

`getCompletionTokens()` leaves out thinking tokens, but they are billed as output (here 878 of
1275 output tokens, ~69%). Cost logging therefore uses `output = total − prompt` (or native
`candidates + thoughts`), with input and output logged separately because they are priced differently.
(Observed on Spring AI 2.0.2; re-verify after upgrades.)