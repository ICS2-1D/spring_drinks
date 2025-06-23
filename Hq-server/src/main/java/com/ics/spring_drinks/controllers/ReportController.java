package com.ics.spring_drinks.controllers;

import com.ics.dtos.ConsolidatedSalesReportDto;
import com.ics.dtos.SalesReportDto;
import com.ics.models.Branch;
import com.ics.spring_drinks.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports") // Using a common base path like /api is good practice
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Endpoint to get the full, consolidated sales report for all branches.
     * Accessible via: GET /api/reports/consolidated
     *
     * @return A ConsolidatedSalesReportDto containing data from all branches.
     */
    @GetMapping("/consolidated")
    public ResponseEntity<ConsolidatedSalesReportDto> getConsolidatedReport() {
        ConsolidatedSalesReportDto report = reportService.buildConsolidatedReport();
        return ResponseEntity.ok(report);
    }

    /**
     * Endpoint to get a sales report for a specific branch.
     * Accessible via: GET /api/reports/branch/{branchName}
     * Example: /api/reports/branch/MOMBASA
     *
     * @param branchName The name of the branch (e.g., "NAIROBI", "MOMBASA").
     * @return A SalesReportDto for the specified branch.
     */
    @GetMapping("/branch/{branchName}")
    public ResponseEntity<SalesReportDto> getSalesReportForBranch(@PathVariable String branchName) {
        try {
            // Convert the string from the path into a Branch enum
            Branch branch = Branch.valueOf(branchName.toUpperCase());
            SalesReportDto report = reportService.buildSalesReportForBranch(branch);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            // Handle cases where the branch name is invalid
            return ResponseEntity.badRequest().body(new SalesReportDto(-1, null)); // Or a custom error response
        }
    }
}
