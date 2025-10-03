package com.example.moneymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoogleUser {
    String sub;
    String email;
    String name;
    String pictureUrl;
}