package com.sprint.mission.discodeit.dto.response;

import java.util.List;

/**
 * 일관된 페이지네이션 응답을 위한 DTO
 *
 * @param <T> 페이지 목록에 담길 데이터 타입
 */
public class PageResponse<T> {

    private List<T> content;     // 실제 데이터 목록
    private int number;          // 현재 페이지 번호 (0-based)
    private int size;            // 페이지 크기
    private boolean hasNext;     // 다음 페이지가 있는지 여부
    private Long totalElements;  // 전체 데이터 개수 (null 가능)

    // 기본 생성자
    public PageResponse() {
    }

    // 모든 필드를 받는 생성자
    public PageResponse(List<T> content, int number, int size, boolean hasNext, Long totalElements) {
        this.content = content;
        this.number = number;
        this.size = size;
        this.hasNext = hasNext;
        this.totalElements = totalElements;
    }

    // Getter/Setter
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }
}
