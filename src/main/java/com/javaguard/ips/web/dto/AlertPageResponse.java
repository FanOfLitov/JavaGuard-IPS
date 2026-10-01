package com.javaguard.ips.web.dto;
import com.javaguard.ips.detection.model.SecurityEvent;

import java.util.List;

public record AlertPageResponse (List<SecurityEvent>content, int page, int size, long totalElements, int totalPages, boolean first, boolean last){
}
