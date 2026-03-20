# valence-be

Backend service for Valence, a mood-based music recommendation platform.

## What more can be worked on next?

### 1) Reliability and quality
- Add focused unit tests for `NrcVadAnalyzer`, `MoodService`, and `RecommendService` path interpolation logic.
- Add integration tests for auth-protected endpoints using test profiles.
- Add CI checks that run tests with a safe in-memory/local test database profile.

### 2) Recommendation quality
- Improve recommendation path scoring with diversity controls (avoid same artist repetition).
- Add feedback loop support (like/dislike/skip) to personalize future recommendations.
- Add fallback recommendation strategy when Spotify audio features are unavailable.

### 3) Performance and scalability
- Improve cache policies for `song_cache` (TTL/eviction strategy and hit-rate metrics).
- Batch external API requests where possible and add retry/backoff policies.
- Add pagination/filtering options to history and recommendation retrieval endpoints.

### 4) Security and operations
- Add refresh-token flow and token revocation support.
- Harden rate limiting for auth and recommendation endpoints.
- Add production observability: structured logs, metrics dashboards, and health checks.

### 5) Product-facing improvements
- Expose richer mood-session insights (trend summaries, streaks, and movement on VA plane).
- Add curated “mood journey presets” (e.g., anxious → calm, low-energy → motivated).
- Add admin tools for monitoring failed external API calls and cache behavior.
