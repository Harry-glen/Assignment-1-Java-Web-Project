package com.harry.stt;

// One word and its timing, passed through from Whisper unchanged.
public record SubtitleWord(String word, double start, double end) {}