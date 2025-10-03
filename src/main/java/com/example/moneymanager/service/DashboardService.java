package com.example.moneymanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class DashboardService {
    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    public Map<String, Object> getDashBoardData() {
        Map<String, Object> dashBoardData = new LinkedHashMap<>();

        BigDecimal totalIncome = incomeService.getTotalIncome();
        BigDecimal totalExpense = expenseService.getTotalExpense();

        dashBoardData.put("totalBalance", totalIncome.subtract(totalExpense));
        dashBoardData.put("totalIncome", totalIncome);
        dashBoardData.put("totalExpense", totalExpense);
        return dashBoardData;
    }
}
