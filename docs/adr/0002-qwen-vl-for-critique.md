# Qwen-VL via DashScope for post-capture Critique

The Guidance Orchestrator calls Alibaba's Qwen-VL model (via DashScope API) to generate post-capture Critiques. This is the MVP's sole multimodal LLM integration.

We chose Qwen-VL because the product targets the Chinese domestic market, and the PRD mandates a compliant, domestically hosted model. Qwen-VL offers competitive vision-language capabilities among domestic options and DashScope provides a straightforward API with Alibaba Cloud's compliance posture.

## Considered Options

- **GLM-4V (Zhipu)**: Viable alternative with similar capabilities. No strong differentiator over Qwen-VL for this use case; Qwen-VL's DashScope ecosystem was preferred for easier integration with Alibaba Cloud infrastructure.
- **OpenAI GPT-4o / Anthropic Claude**: Superior vision-language performance but data residency and compliance concerns for the China market make them unsuitable as the default.
- **Abstract interface, decide later**: Rejected for MVP — we need a concrete integration to validate the Critique experience end-to-end. The orchestrator code should still use a clean interface boundary to allow swapping later.
