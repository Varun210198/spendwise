package com.vvs.spendwise_api.common.exception;

public class CategoryInUseException extends RuntimeException {

    public CategoryInUseException(String categoryName) {
        super("Category '" + categoryName + "' has existing transactions and cannot be deleted");
    }
}
