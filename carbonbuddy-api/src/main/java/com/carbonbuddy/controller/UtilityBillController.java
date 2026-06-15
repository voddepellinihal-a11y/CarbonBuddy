package com.carbonbuddy.controller;

import com.carbonbuddy.dto.request.UtilityBillRequest;
import com.carbonbuddy.model.UtilityBill;
import com.carbonbuddy.service.UtilityBillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utility-bills")
public class UtilityBillController {

    private final UtilityBillService utilityBillService;

    public UtilityBillController(UtilityBillService utilityBillService) {
        this.utilityBillService = utilityBillService;
    }

    @PostMapping
    public ResponseEntity<UtilityBill> createUtilityBill(
            Authentication auth,
            @Valid @RequestBody UtilityBillRequest request) {
        Long userId = (Long) auth.getPrincipal();
        UtilityBill bill = utilityBillService.createUtilityBill(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bill);
    }
}
