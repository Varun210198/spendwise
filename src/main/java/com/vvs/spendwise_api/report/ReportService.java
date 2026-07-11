package com.vvs.spendwise_api.report;

import com.vvs.spendwise_api.report.dto.ReportBucket;
import com.vvs.spendwise_api.report.dto.ReportSummaryResponse;
import com.vvs.spendwise_api.security.CurrentUser;
import com.vvs.spendwise_api.transaction.Transaction;
import com.vvs.spendwise_api.transaction.TransactionRepository;
import com.vvs.spendwise_api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final CurrentUser currentUser;

    public ReportSummaryResponse summary(LocalDate from, LocalDate to, String groupByParam) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'");
        }
        ReportGroupBy groupBy = parseGroupBy(groupByParam);
        User user = currentUser.get();
        List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(user.getId(), from, to);

        // TreeMap keeps buckets in a naturally meaningful order: chronological for
        // day/week/month labels (they sort lexicographically the same as by date),
        // alphabetical for category names.
        Map<String, BigDecimal> totalsByBucket = new TreeMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            String label = bucketLabel(transaction, groupBy);
            totalsByBucket.merge(label, transaction.getAmount(), BigDecimal::add);
            total = total.add(transaction.getAmount());
        }

        List<ReportBucket> buckets = totalsByBucket.entrySet().stream()
                .map(entry -> new ReportBucket(entry.getKey(), entry.getValue()))
                .toList();

        return new ReportSummaryResponse(from, to, groupBy.name().toLowerCase(), total, buckets);
    }

    private String bucketLabel(Transaction transaction, ReportGroupBy groupBy) {
        LocalDate date = transaction.getDate();
        return switch (groupBy) {
            case DAY -> date.toString();
            case WEEK -> {
                WeekFields weekFields = WeekFields.ISO;
                int week = date.get(weekFields.weekOfWeekBasedYear());
                int weekYear = date.get(weekFields.weekBasedYear());
                yield "%d-W%02d".formatted(weekYear, week);
            }
            case MONTH -> YearMonth.from(date).toString();
            case CATEGORY -> transaction.getCategory().getName();
        };
    }

    private ReportGroupBy parseGroupBy(String value) {
        try {
            return ReportGroupBy.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException("groupBy must be one of: day, week, month, category");
        }
    }
}
