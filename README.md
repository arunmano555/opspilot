## Phase 0 — Hello LLM

### What it does
- `RawGeminiCall`: sends one prompt to the Gemini REST API using plain `java.net.http`
  (no framework), and prints the raw JSON response.
- `HelloChatClient`: sends the same prompt through Spring AI's `ChatClient` and prints the
  text, `finishReason`, model, Spring's normalized `Usage`, and Gemini's native usage metadata.

### How to run
1. `export GEMINI_API_KEY=<your key>`
2. `./mvnw spring-boot:run`

Model: `gemini-3.5-flash-lite` (set via `spring.ai.google.genai.chat.model`).

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