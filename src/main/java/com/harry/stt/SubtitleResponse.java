package com.harry.stt;

import java.util.List;

public record SubtitleResponse(List<SubtitleSegment> segments, List<SubtitleWord> words){}
