package com.harry.stt;

import java.util.List;

// Reply of /api/v1/subtitles: phrases and word timings, in seconds from the start of the chunk.
public record SubtitleResponse(List<SubtitleSegment> segments, List<SubtitleWord> words){}
