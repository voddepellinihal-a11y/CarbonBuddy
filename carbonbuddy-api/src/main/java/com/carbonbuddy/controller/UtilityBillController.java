package com.carbonbuddy.controller;

import com.carbonbuddy.dto.request.UtilityBillRequest;
import com.carbonbuddy.model.UtilityBill;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.UtilityBillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for utility bill management.
 * Supports creating utility bills with carbon emission computation.
 */
@RestController
@RequestMapping("/api/utility-bills")
@Validated
public class UtilityBillController {

    private final UtilityBillService utilityBillService;

    /**
     * Constructs UtilityBillController with the utility bill service.
     *
     * @param utilityBillService the service handling utility bill logic
     */
    public UtilityBillController(UtilityBillService utilityBillService) {
        this.utilityBillService = utilityBillService;
    }

    /**
     * Creates a new utility bill for the authenticated user.
     *
     * @param auth    the authentication principal
     * @param request the validated utility bill request
     * @return 201 Created with the persisted utility bill
     */
    @PostMapping
    public ResponseEntity<UtilityBill> createUtilityBill(
            Authentication auth,
            @Valid @RequestBody UtilityBillRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        UtilityBill bill = utilityBillService.createUtilityBill(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bill);
    }
}
