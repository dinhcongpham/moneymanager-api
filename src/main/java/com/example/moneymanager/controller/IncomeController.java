package com.example.moneymanager.controller;

import com.example.moneymanager.dto.ExpenseDto;
import com.example.moneymanager.dto.IncomeDto;
import com.example.moneymanager.service.ExpenseService;
import com.example.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/incomes")
@RequiredArgsConstructor
public class IncomeController {
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeDto> addIncome(@RequestBody IncomeDto incomeDto) {
        IncomeDto saved =  incomeService.addIncome(incomeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/month")
    public ResponseEntity<List<IncomeDto>> getCurrentMonthIncome() {
        List<IncomeDto> expenses = incomeService.getCurrentMonthIncome();
        return ResponseEntity.status(HttpStatus.OK).body(expenses);
    }

    @GetMapping
    public ResponseEntity<List<IncomeDto>> getAllIncome() {
        List<IncomeDto> expenses = incomeService.getAllIncome();
        return ResponseEntity.status(HttpStatus.OK).body(expenses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpenseById(@PathVariable Long id) {
        incomeService.deleteIncomeById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
