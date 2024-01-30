package com.ievlev.dataox.controller;

import com.ievlev.dataox.service.GoogleApiService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GoogleSheetStorageController {
    private final GoogleApiService googleApiService;

    @PostMapping("api/v1/sheet")
    public String createNewGoogleSheet() {
        return googleApiService.createGoogleSheet();
    }

    @PostMapping("api/v1/sheet/{id}")
    public void setGoogleSheet(@PathVariable String id) {

    }

    @GetMapping("api/v1/sheet")
    public String getGoogleSheetUrl(){
        return googleApiService.getGoogleSheetUrl();
    }

}
