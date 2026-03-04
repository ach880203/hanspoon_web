package com.project.hanspoon.oneday.instructor.controller;

import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.oneday.instructor.dto.InstructorAdminRequest;
import com.project.hanspoon.oneday.instructor.dto.InstructorAdminResponse;
import com.project.hanspoon.oneday.instructor.dto.InstructorCandidateUserResponse;
import com.project.hanspoon.oneday.instructor.service.AdminInstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/oneday/instructors")
public class AdminInstructorController {

    private final AdminInstructorService adminInstructorService;

    @GetMapping
    public ApiResponse<List<InstructorAdminResponse>> list() {
        return ApiResponse.ok(adminInstructorService.getInstructors());
    }

    @GetMapping("/candidate-users")
    public ApiResponse<List<InstructorCandidateUserResponse>> candidateUsers(
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(adminInstructorService.getCandidateUsers(keyword));
    }

    @PostMapping
    public ApiResponse<InstructorAdminResponse> create(@RequestBody InstructorAdminRequest req) {
        return ApiResponse.ok("강사가 등록되었습니다.", adminInstructorService.createInstructor(req));
    }

    @PutMapping("/{instructorId}")
    public ApiResponse<InstructorAdminResponse> update(
            @PathVariable Long instructorId,
            @RequestBody InstructorAdminRequest req
    ) {
        return ApiResponse.ok("강사 정보가 수정되었습니다.", adminInstructorService.updateInstructor(instructorId, req));
    }

    @DeleteMapping("/{instructorId}")
    public ApiResponse<Void> delete(@PathVariable Long instructorId) {
        adminInstructorService.deleteInstructor(instructorId);
        return ApiResponse.ok("강사가 삭제되었습니다.", null);
    }
}

