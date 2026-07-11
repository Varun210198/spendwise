package com.vvs.spendwise_api.report.dto;

import java.math.BigDecimal;

public record ReportBucket(String label, BigDecimal total) {
}
