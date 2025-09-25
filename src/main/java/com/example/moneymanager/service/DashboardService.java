package com.example.moneymanager.service;

import com.example.moneymanager.dto.ExpenseDto;
import com.example.moneymanager.dto.IncomeDto;
import com.example.moneymanager.dto.RecentTransactionDto;
import com.example.moneymanager.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Stream.concat;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;

    public Map<String, Object> getDashBoardData() {
        ProfileEntity profile = profileService.getCurrentProfile();
        Map<String, Object> dashBoardData = new LinkedHashMap<>();
        List<IncomeDto> incomes = incomeService.getLatest5Incomes();
        List<ExpenseDto> expenses = expenseService.getLatest5Expenses();

        List<RecentTransactionDto> transactions = concat(
                incomes.stream().map(income ->
                        RecentTransactionDto.builder()
                                .id(income.getId())
                                .name(income.getName())
                                .profileId(profile.getId())
                                .icon(income.getIcon())
                                .amount(income.getAmount())
                                .date(income.getDate())
                                .createdAt(income.getCreatedAt())
                                .updatedAt(income.getUpdatedAt())
                                .type("income")
                                .build()
                ),
                expenses.stream().map(expense ->
                        RecentTransactionDto.builder()
                                .id(expense.getId())
                                .name(expense.getName())
                                .profileId(profile.getId())
                                .icon(expense.getIcon())
                                .amount(expense.getAmount())
                                .date(expense.getDate())
                                .createdAt(expense.getCreatedAt())
                                .updatedAt(expense.getUpdatedAt())
                                .type("expense")
                                .build()))
                .sorted((a, b) -> {
                    int cmp = b.getDate().compareTo(a.getDate());
                    if (cmp == 0 && b.getCreatedAt() != null && a.getCreatedAt() == null) {
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return cmp;
                }).toList();

        BigDecimal totalIncome = incomeService.getTotalIncome();
        BigDecimal totalExpense = expenseService.getTotalExpense();

        dashBoardData.put("totalBalance", totalIncome.subtract(totalExpense));
        dashBoardData.put("totalIncome", totalIncome);
        dashBoardData.put("totalExpense", totalExpense);
        dashBoardData.put("recent5Incomes", incomes);
        dashBoardData.put("recent5Expenses", expenses);
        dashBoardData.put("recentTransactions", transactions);

        return dashBoardData;
    }
}
