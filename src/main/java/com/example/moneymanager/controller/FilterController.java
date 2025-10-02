package com.example.moneymanager.controller;

import com.example.moneymanager.dto.ExpenseDto;
import com.example.moneymanager.dto.FilterDto;
import com.example.moneymanager.dto.IncomeDto;
import com.example.moneymanager.service.ExpenseService;
import com.example.moneymanager.service.IncomeService;
import com.example.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        LocalDate startDate = filterDto.getStartDate() != null ? filterDto.getStartDate() : LocalDate.of(1970, 1, 1);
        LocalDate endDate = filterDto.getEndDate() != null ? filterDto.getEndDate() : LocalDate.now();
        String keyword = filterDto.getKeyword() != null ? filterDto.getKeyword() : "";
        String sortField = filterDto.getSortField() != null ? filterDto.getSortField() : "date";
        Integer page =  filterDto.getPage() != null ? filterDto.getPage() : 0;
        Integer pageSize = filterDto.getPageSize() != null ? filterDto.getPageSize() : 10;
        Sort.Direction direction = "desc".equalsIgnoreCase(filterDto.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);

        Pageable pageable = PageRequest.of(page, pageSize, sort);

        if ("income".equals(filterDto.getType())) {
            Page<IncomeDto> incomes = incomeService.filterIncomes(startDate, endDate, keyword, pageable);
            return ResponseEntity.ok().body(incomes);
        } else if ("expense".equals(filterDto.getType())) {
            Page<ExpenseDto> expense = expenseService.filterExpenses(startDate, endDate, keyword, pageable);
            return ResponseEntity.ok().body(expense);
        } else {
            return ResponseEntity.badRequest().body("Invalid type. Must be 'income' or 'expense'");
        }
    }
}
