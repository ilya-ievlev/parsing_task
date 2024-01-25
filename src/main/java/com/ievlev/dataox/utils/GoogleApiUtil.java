package com.ievlev.dataox.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.ievlev.dataox.model.GoogleSheetModel;
import com.ievlev.dataox.model.Job;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class GoogleApiUtil {
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE);
    private static final String TOKENS_DIRECTORY_PATH = "tokens/path";
    private static final String APPLICATION_NAME = "Google Sheets API";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GoogleApiUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    private Sheets getSheetService() {
        try {
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }catch (GeneralSecurityException | IOException e){
            e.printStackTrace();
        }
        return null;
    }


    public GoogleSheetModel createGoogleSheet() {
        GoogleSheetModel googleSheetModel = new GoogleSheetModel();
        Sheets service = getSheetService();
        SpreadsheetProperties spreadsheetProperties = new SpreadsheetProperties();
        spreadsheetProperties.setTitle("results from scraping");
        SheetProperties sheetProperties = new SheetProperties();
        sheetProperties.setTitle("results from scraping");
        Sheet sheet = new Sheet().setProperties(sheetProperties);
        Spreadsheet spreadsheet = new Spreadsheet().setProperties(spreadsheetProperties).setSheets(Collections.singletonList(sheet));
        googleSheetModel.setService(service);
        Spreadsheet createdResponse = null;
        try {
            createdResponse = service.spreadsheets().create(spreadsheet).execute();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        googleSheetModel.setUrlToGoogleSheet(createdResponse.getSpreadsheetUrl());
        googleSheetModel.setSpreadsheetId(createdResponse.getSpreadsheetId());
        return googleSheetModel;
    }


    public void addJobToSheet(Job job, int numberOfRaw, GoogleSheetModel googleSheetModel) {
        ValueRange valueRange = new ValueRange().setValues(JobConvertor.convertJobToListOfList(job)).setMajorDimension("COLUMNS");
        try {
            googleSheetModel.getService().spreadsheets().values()
                    .update(googleSheetModel.getSpreadsheetId(), "A" + numberOfRaw, valueRange)
                    .setValueInputOption("RAW").execute();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
