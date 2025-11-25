package com.FindMyService.controller;

import com.FindMyService.model.dto.CheckoutItem;
import com.FindMyService.service.CheckoutService;
import com.FindMyService.utils.OwnerCheck;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/checkout")
@RestController
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final OwnerCheck ownerCheck;

    public CheckoutController(CheckoutService checkoutService, OwnerCheck ownerCheck) {
        this.checkoutService = checkoutService;
        this.ownerCheck = ownerCheck;
    }

    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @PostMapping("/user/{userId}")
    public ResponseEntity<List<?>> checkout(@PathVariable Long userId,
                                            @RequestBody List<CheckoutItem> items) {
        ownerCheck.verifyOwner(userId);
        List<ResponseEntity<?>> results = checkoutService.checkout(userId, items);
        return ResponseEntity.ok(results);
    }
}
