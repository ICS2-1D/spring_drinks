package com.ics.spring_drinks.controllers;


import com.ics.dtos.SalesReportDto;
import com.ics.spring_drinks.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/report")
    public ResponseEntity<SalesReportDto> getSalesReport() {
        return ResponseEntity.ok(reportService.buildSalesReport());
    }
}