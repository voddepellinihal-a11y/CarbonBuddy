package com.carbonbuddy.controller;

import com.carbonbuddy.dto.ApiResponse;
import com.carbonbuddy.dto.request.UtilityBillRequest;
import com.carbonbuddy.model.UtilityBill;
import com.carbonbuddy.security.SecurityUtil;
import com.carbonbuddy.service.UtilityBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/utility-bills")
@Validated
@Tag(name = "Utility Bills", description = "Utility bill management and carbon computation")
public class UtilityBillController {

    private final UtilityBillService utilityBillService;

    public UtilityBillController(UtilityBillService utilityBillService) {
        this.utilityBillService = utilityBillService;
    }

    @Operation(summary = "Create utility bill", description = "Creates a utility bill with carbon emission computation")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Utility bill created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<UtilityBill>> createUtilityBill(
            Authentication auth,
            @Valid @RequestBody UtilityBillRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(auth);
        UtilityBill bill = utilityBillService.createUtilityBill(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Utility bill created", bill));
    }
}
