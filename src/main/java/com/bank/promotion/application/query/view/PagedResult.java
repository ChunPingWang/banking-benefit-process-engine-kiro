package com.bank.promotion.application.query.view;

import java.util.List;
import java.util.Objects;

/**
 * 分頁結果包裝器
 */
public final class PagedResult<T> {
    
    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;
    
    public PagedResult(List<T> content, int page, int size, long totalElements) {
        this.content = content != null ? List.copyOf(content) : List.of();
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.hasNext = page < totalPages - 1;
        this.hasPrevious = page > 0;
    }
    
    public List<T> getContent() {
        return content;
    }
    
    public int getPage() {
        return page;
    }
    
    public int getSize() {
        return size;
    }
    
    public long getTotalElements() {
        return totalElements;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PagedResult<?> that = (PagedResult<?>) o;
        return page == that.page &&
               size == that.size &&
               totalElements == that.totalElements &&
               totalPages == that.totalPages &&
               hasNext == that.hasNext &&
               hasPrevious == that.hasPrevious &&
               Objects.equals(content, that.content);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
    }
    
    @Override
    public String toString() {
        return "PagedResult{" +
               "content=" + content +
               ", page=" + page +
               ", size=" + size +
               ", totalElements=" + totalElements +
               ", totalPages=" + totalPages +
               ", hasNext=" + hasNext +
               ", hasPrevious=" + hasPrevious +
               '}';
    }
}