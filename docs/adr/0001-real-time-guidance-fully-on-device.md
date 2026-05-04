# Real-time guidance is fully on-device

Real-time viewfinder guidance (~2 Hz composition analysis, parameter suggestions, and tips) runs entirely on the Android device using the Kotlin `core-analysis` rule engine. No server calls are made during live viewfinding in the MVP.

The `StreamGuidance` RPC in `guidance.proto` exists as a forward-compatible hook for a future hybrid mode, but is intentionally unused. We chose on-device over server-streamed analysis to eliminate network latency in the feedback loop, avoid battery drain from continuous uploads, and respect the privacy constraint that live viewfinder frames never leave the device by default.

## Considered Options

- **Server-streamed**: Android sends frames to the orchestrator at ~2 Hz via `StreamGuidance`. Rejected because round-trip latency (50–200 ms even on good networks) breaks the real-time feel, and uploading viewfinder frames conflicts with the "frames never leave the device" privacy default.
- **Hybrid (device + opt-in server)**: On-device by default, with an opt-in toggle for server-enhanced analysis. Deferred — adds complexity without clear user value until the on-device engine's limitations are understood in practice.
