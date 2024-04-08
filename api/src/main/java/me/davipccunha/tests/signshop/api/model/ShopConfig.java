package me.davipccunha.tests.signshop.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ShopConfig {
    private boolean partialSellingAllowed;
    private boolean notificationsEnabled;
}
