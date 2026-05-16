package org.User.query.queries;

import lombok.RequiredArgsConstructor;
import org.User.query.model.response.AdminUserResponse;
import org.User.query.model.response.PageResponse;
import org.User.query.repository.AdminUserNativeQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAdminUsersQueryHandler {
    private final AdminUserNativeQueryRepository repository;
    public PageResponse<AdminUserResponse> handle(GetAdminUsersQuery query) {
        int page = Math.max(query.getPage(), 0);
        // Nếu size <= 0 thì default = 10
        int size = query.getSize() <= 0 ? 10 : query.getSize();
        //Convert enum UserType -> String
        String userType = query.getUserType() == null
                ? null
                : query.getUserType().name();
        // Query danh sách users theo filter + pagination
        List<AdminUserResponse> users = repository.searchUsers(
                query.getEmail(),
                userType,
                query.getIsActive(),
                query.getRoleName(),
                page,
                size
        );
        // Query tổng số users thỏa điều kiện filter
        long totalElements = repository.countUsers(
                query.getEmail(),
                userType,
                query.getIsActive(),
                query.getRoleName()
        );
        // Tính tổng số trang
        int totalPages = (int) Math.ceil((double) totalElements / size);
        // Build response trả về cho API
        return PageResponse.<AdminUserResponse>builder()
                .content(users)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(totalPages == 0 || page + 1 >= totalPages)
                .build();
    }
}
