package com.example.moneymanager.controller;

import com.example.moneymanager.dto.ExpenseDto;
import com.example.moneymanager.dto.FilterDto;
import com.example.moneymanager.dto.IncomeDto;
import com.example.moneymanager.dto.RecentTransactionDto;
import com.example.moneymanager.service.ExpenseService;
import com.example.moneymanager.service.IncomeService;
import com.example.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        Integer p =  filterDto.getPage() != null ? filterDto.getPage() : 0;
        Integer pSize = filterDto.getPageSize() != null ? filterDto.getPageSize() : 10;
        Sort.Direction direction = "desc".equalsIgnoreCase(filterDto.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);

        Pageable pageable = PageRequest.of(p, pSize, sort);

        if ("income".equals(filterDto.getType())) {
            Page<IncomeDto> incomes = incomeService.filterIncomes(startDate, endDate, keyword, pageable);
            return ResponseEntity.ok().body(incomes);
        } else if ("expense".equals(filterDto.getType())) {
            Page<ExpenseDto> expense = expenseService.filterExpenses(startDate, endDate, keyword, pageable);
            return ResponseEntity.ok().body(expense);
        } else {
            // Fetch both income and expense data
            Page<IncomeDto> incomePage = incomeService.filterIncomes(startDate, endDate, keyword, Pageable.unpaged());
            Page<ExpenseDto> expensePage = expenseService.filterExpenses(startDate, endDate, keyword, Pageable.unpaged());

            // Convert both to a common DTO (RecentTransactionDto)
            List<RecentTransactionDto> combined = new ArrayList<>();

            incomePage.getContent().forEach(i -> combined.add(
                    RecentTransactionDto.builder()
                            .id(i.getId())
                            .profileId(null) // fill if needed
                            .icon(i.getIcon())
                            .name(i.getName())
                            .amount(i.getAmount())
                            .date(i.getDate())
                            .createdAt(i.getCreatedAt())
                            .updatedAt(i.getUpdatedAt())
                            .type("income")
                            .build()
            ));

            expensePage.getContent().forEach(e -> combined.add(
                    RecentTransactionDto.builder()
                            .id(e.getId())
                            .profileId(null) // fill if needed
                            .icon(e.getIcon())
                            .name(e.getName())
                            .amount(e.getAmount())
                            .date(e.getDate())
                            .createdAt(e.getCreatedAt())
                            .updatedAt(e.getUpdatedAt())
                            .type("expense")
                            .build()
            ));

            // sort by date DESC, then createdAt DESC
            combined.sort(
                    Comparator.comparing(RecentTransactionDto::getDate,
                                    Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(RecentTransactionDto::getCreatedAt,
                                    Comparator.nullsLast(Comparator.reverseOrder()))
            );

            int page = filterDto.getPage() != null ? filterDto.getPage() : 0;
            int pageSize = filterDto.getPageSize() != null ? filterDto.getPageSize() : 10;

            int start = Math.min(page * pageSize, combined.size());
            int end = Math.min(start + pageSize, combined.size());

            List<RecentTransactionDto> pagedContent = start < end
                    ? new ArrayList<>(combined.subList(start, end))
                    : Collections.emptyList();

            Page<RecentTransactionDto> resultPage = new PageImpl<>(
                    pagedContent,
                    PageRequest.of(page, pageSize),
                    combined.size()
            );

            return ResponseEntity.ok(resultPage);
        }
    }
}
