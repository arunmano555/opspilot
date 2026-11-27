What I built and why: Phase 0

Before using Spring AI, I called the Gemini API directly with Java's HTTP client, because I wanted to know exactly what a framework would be doing for me: the endpoint, the auth header, the JSON shape, and what comes back. Then I made the same call through Spring AI's ChatClient and compared the two responses field by field.

That comparison turned up a real gap. Spring AI's getCompletionTokens() reported 397 output tokens, but Gemini's native metadata showed another 878 thinking tokens, which are billed as output too. So about 69% of the output was missing from the normalized number. A cost dashboard built on that field would have undercounted our output bill roughly 3×, and nothing would have errored. It would just have been quietly wrong.

Now I log input and output tokens separately, and I compute output as total minus prompt, or read the provider's native fields. I also learned that an HTTP 200 isn't success. I check finishReason for truncation or safety blocks, and I want the actual outgoing request logged, because a request can be wrong and still return 200.