package com.sunrisedental.dental_clinic.controller;

import com.sunrisedental.dental_clinic.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("currentUser")
    public UserPrincipal getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userPrincipal;
    }
}
