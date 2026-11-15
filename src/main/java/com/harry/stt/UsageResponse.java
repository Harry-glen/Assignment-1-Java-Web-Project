package com.harry.stt;

// Reply of /api/v1/global/usage: total seconds of audio sent to Whisper.
public record UsageResponse(double seconds) {}