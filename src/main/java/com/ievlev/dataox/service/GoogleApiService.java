package com.ievlev.dataox.service;

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
import com.ievlev.dataox.exception.GoogleSheetException;
import com.ievlev.dataox.model.GoogleSheetModel;
import com.ievlev.dataox.model.Job;
import com.ievlev.dataox.utils.JobConvertor;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleApiService {
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE);
    private static final String TOKENS_DIRECTORY_PATH = "tokens/path";
    private static final String APPLICATION_NAME = "Google Sheets API";
    private GoogleSheetModel googleSheetModel;

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GoogleApiService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSheetException(e);
        }
    }


    public String createGoogleSheet() {
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
            throw new GoogleSheetException(ioException);
        }
        googleSheetModel.setUrlToGoogleSheet(createdResponse.getSpreadsheetUrl());
        googleSheetModel.setSpreadsheetId(createdResponse.getSpreadsheetId());
        this.googleSheetModel = googleSheetModel;
        return googleSheetModel.getUrlToGoogleSheet();
    }


    public void addJobToSheet(Job job, int numberOfRaw) {
        ValueRange valueRange = new ValueRange().setValues(JobConvertor.convertJobToListOfListOfObject(job)).setMajorDimension("COLUMNS");
        try {
            googleSheetModel.getService().spreadsheets().values()
                    .update(googleSheetModel.getSpreadsheetId(), "A" + numberOfRaw, valueRange)
                    .setValueInputOption("RAW").execute();
        } catch (IOException ioException) {
            throw new GoogleSheetException(ioException);
        }
    }

    public GoogleSheetModel getGoogleSheetModel() {
        return googleSheetModel;
    }

    public String getGoogleSheetUrl() {
        if(googleSheetModel!=null){
            return googleSheetModel.getUrlToGoogleSheet();
        }
        // TODO: 30-Jan-24 спросить что возвращать тут, исключение или что-то другое по типу строки "не найдено"
        return "table not found. you can create a new one or specify an existing one";
    }
}
