package com.vvs.spendwise_api.category;

import com.vvs.spendwise_api.category.dto.CategoryRequest;
import com.vvs.spendwise_api.category.dto.CategoryResponse;
import com.vvs.spendwise_api.common.exception.CategoryInUseException;
import com.vvs.spendwise_api.common.exception.DuplicateCategoryException;
import com.vvs.spendwise_api.common.exception.ResourceNotFoundException;
import com.vvs.spendwise_api.security.CurrentUser;
import com.vvs.spendwise_api.transaction.TransactionRepository;
import com.vvs.spendwise_api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUser currentUser;

    public List<CategoryResponse> list() {
        User user = currentUser.get();
        return categoryRepository.findByUserIdOrderByNameAsc(user.getId()).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse create(CategoryRequest request) {
        User user = currentUser.get();
        ensureNameAvailable(user.getId(), request.name());

        Category category = Category.builder()
                .user(user)
                .name(request.name())
                .build();
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        User user = currentUser.get();
        Category category = findOwned(id, user.getId());

        if (!category.getName().equalsIgnoreCase(request.name())) {
            ensureNameAvailable(user.getId(), request.name());
        }
        category.setName(request.name());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public void delete(Long id) {
        User user = currentUser.get();
        Category category = findOwned(id, user.getId());

        if (transactionRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException(category.getName());
        }
        categoryRepository.delete(category);
    }

    private Category findOwned(Long id, Long userId) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private void ensureNameAvailable(Long userId, String name) {
        if (categoryRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new DuplicateCategoryException(name);
        }
    }
}
