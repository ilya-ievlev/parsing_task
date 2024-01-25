package com.ievlev.dataox.model;

import com.google.api.services.sheets.v4.Sheets;
import lombok.Data;

@Data
public class GoogleSheetModel {
    private String urlToGoogleSheet;
    private String spreadsheetId;
    private Sheets service;

}
