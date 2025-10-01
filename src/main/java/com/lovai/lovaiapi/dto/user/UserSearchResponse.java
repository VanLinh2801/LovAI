package com.lovai.lovaiapi.dto.user;

import lombok.Data;
import java.util.List;

@Data
public class UserSearchResponse {
    private List<UserResponse> users;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
}
