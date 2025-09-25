package com.example.moneymanager.controller;

import com.example.moneymanager.dto.ExpenseDto;
import com.example.moneymanager.dto.FilterDto;
import com.example.moneymanager.dto.IncomeDto;
import com.example.moneymanager.service.ExpenseService;
import com.example.moneymanager.service.IncomeService;
import com.example.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/filter")
@RequiredArgsConstructor
public class FilterController {
    private final IncomeService incomeService;
    private final ProfileService profileService;
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDto filterDto) {
        LocalDate startDate = filterDto.getStartDate() != null ? filterDto.getStartDate() : LocalDate.MIN;
        LocalDate endDate = filterDto.getEndDate() != null ? filterDto.getEndDate() : LocalDate.now();
        String keyword = filterDto.getKeyword() != null ? filterDto.getKeyword() : "";
        String sortField = filterDto.getSortField() != null ? filterDto.getSortField() : "date";
        Sort.Direction direction = "desc".equalsIgnoreCase(filterDto.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);

        if ("income".equals(filterDto.getType())) {
            List<IncomeDto> incomes = incomeService.filterIncomes(startDate, endDate, keyword, sort);
            return ResponseEntity.ok().body(incomes);
        } else if ("expense".equals(filterDto.getType())) {
            List<ExpenseDto> expense = expenseService.filterExpenses(startDate, endDate, keyword, sort);
            return ResponseEntity.ok().body(expense);
        } else {
            return ResponseEntity.badRequest().body("Invalid type. Must be 'income' or 'expense'");
        }
    }
}
