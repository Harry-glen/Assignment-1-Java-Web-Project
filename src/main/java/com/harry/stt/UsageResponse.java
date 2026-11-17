package com.harry.stt;

// Reply of /api/v1/global/usage, as the spec requires: { "usage": s } where s is total seconds of audio sent to Whisper.
public record UsageResponse(double usage) {}