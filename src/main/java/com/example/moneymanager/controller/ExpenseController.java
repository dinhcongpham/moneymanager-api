package com.example.moneymanager.controller;

import com.example.moneymanager.dto.ExpenseDto;
import com.example.moneymanager.entity.ExpenseEntity;
import com.example.moneymanager.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDto> addExpense(@RequestBody ExpenseDto expenseDto) {
        ExpenseDto saved =  expenseService.addExpense(expenseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/month")
    public ResponseEntity<List<ExpenseDto>> getCurrentMonthExpense() {
        List<ExpenseDto> expenses = expenseService.getCurrentMonthExpense();
        return ResponseEntity.status(HttpStatus.OK).body(expenses);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getAllExpense() {
        List<ExpenseDto> expenses = expenseService.getAllExpense();
        return ResponseEntity.status(HttpStatus.OK).body(expenses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpenseById(@PathVariable Long id) {
        expenseService.deleteExpenseById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
