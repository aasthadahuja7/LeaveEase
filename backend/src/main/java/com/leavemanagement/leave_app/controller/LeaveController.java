package com.leavemanagement.leave_app.controller;

import com.leavemanagement.leave_app.model.LeaveRequest;
import com.leavemanagement.leave_app.model.User;
import com.leavemanagement.leave_app.repository.LeaveRequestRepository;
import com.leavemanagement.leave_app.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/leaves")
public class LeaveController {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<LeaveRequest> createLeave(@Valid @RequestBody LeaveRequest leaveRequest) {
        LeaveRequest savedLeave = leaveRequestRepository.save(leaveRequest);
        URI location = URI.create("/leaves/" + savedLeave.getId());
        return ResponseEntity.created(location).body(savedLeave);
    }

    @GetMapping
    public List<LeaveRequest> getAllLeaves() {
        return leaveRequestRepository.findAll();
    }

    @GetMapping("/my-leaves")
    public ResponseEntity<List<LeaveRequest>> getMyLeaves(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        return userRepository.findByUsername(authentication.getName())
                .map(user -> ResponseEntity.ok(leaveRequestRepository.findByEmployeeName(user.getFullName())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequest> getLeaveById(@PathVariable String id) {
        return leaveRequestRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveRequest> updateLeave(@PathVariable String id, @Valid @RequestBody LeaveRequest updatedLeave) {
        return leaveRequestRepository.findById(id)
                .map(leave -> {
                    leave.setEmployeeName(updatedLeave.getEmployeeName());
                    leave.setStartDate(updatedLeave.getStartDate());
                    leave.setEndDate(updatedLeave.getEndDate());
                    leave.setReason(updatedLeave.getReason());
                    leave.setStatus(updatedLeave.getStatus());
                    return ResponseEntity.ok(leaveRequestRepository.save(leave));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequest> approveLeave(@PathVariable String id, Authentication authentication) {
        return leaveRequestRepository.findById(id)
                .map(leave -> {
                    leave.setStatus("Approved");
                    leave.setRejectionReason(null);
                    return ResponseEntity.ok(leaveRequestRepository.save(leave));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequest> rejectLeave(@PathVariable String id, @RequestBody(required = false) Map<String, String> requestBody, Authentication authentication) {
        return leaveRequestRepository.findById(id)
                .map(leave -> {
                    leave.setStatus("Rejected");
                    leave.setRejectionReason(requestBody != null ? requestBody.get("rejectionReason") : null);
                    return ResponseEntity.ok(leaveRequestRepository.save(leave));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/hr-action")
    public ResponseEntity<LeaveRequest> hrActionOnLeave(@PathVariable String id, @RequestBody Map<String, String> actionRequest) {
        String action = actionRequest.get("action");

        if (action == null) {
            return ResponseEntity.badRequest().build();
        }

        return leaveRequestRepository.findById(id)
                .map(leave -> {
                    if ("approve".equalsIgnoreCase(action)) {
                        leave.setStatus("Approved");
                        leave.setRejectionReason(null);
                    } else if ("reject".equalsIgnoreCase(action)) {
                        leave.setStatus("Rejected");
                        leave.setRejectionReason(actionRequest.get("reason"));
                    } else {
                        return ResponseEntity.badRequest().<LeaveRequest>build();
                    }

                    return ResponseEntity.ok(leaveRequestRepository.save(leave));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeave(@PathVariable String id) {
        if (leaveRequestRepository.existsById(id)) {
            leaveRequestRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
