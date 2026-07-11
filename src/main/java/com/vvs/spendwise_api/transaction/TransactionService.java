package com.vvs.spendwise_api.transaction;

import com.vvs.spendwise_api.category.Category;
import com.vvs.spendwise_api.category.CategoryRepository;
import com.vvs.spendwise_api.common.exception.ResourceNotFoundException;
import com.vvs.spendwise_api.security.CurrentUser;
import com.vvs.spendwise_api.transaction.dto.TransactionRequest;
import com.vvs.spendwise_api.transaction.dto.TransactionResponse;
import com.vvs.spendwise_api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUser currentUser;

    public Page<TransactionResponse> list(LocalDate from, LocalDate to, Long categoryId, Pageable pageable) {
        User user = currentUser.get();
        return transactionRepository.search(user.getId(), from, to, categoryId, pageable)
                .map(TransactionResponse::from);
    }

    public TransactionResponse create(TransactionRequest request) {
        User user = currentUser.get();
        Category category = resolveOwnedCategory(user.getId(), request.categoryId());

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .amount(request.amount())
                .date(request.date())
                .note(request.note())
                .build();
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    public TransactionResponse update(Long id, TransactionRequest request) {
        User user = currentUser.get();
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        Category category = resolveOwnedCategory(user.getId(), request.categoryId());

        transaction.setCategory(category);
        transaction.setAmount(request.amount());
        transaction.setDate(request.date());
        transaction.setNote(request.note());
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    public void delete(Long id) {
        User user = currentUser.get();
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        transactionRepository.delete(transaction);
    }

    private Category resolveOwnedCategory(Long userId, Long categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}
