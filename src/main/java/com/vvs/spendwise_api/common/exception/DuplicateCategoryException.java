package com.vvs.spendwise_api.common.exception;

public class DuplicateCategoryException extends RuntimeException {

    public DuplicateCategoryException(String name) {
        super("A category named '" + name + "' already exists");
    }
}
