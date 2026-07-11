package com.vvs.spendwise_api.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportSummaryResponse(
        LocalDate from,
        LocalDate to,
        String groupBy,
        BigDecimal total,
        List<ReportBucket> buckets
) {
}
