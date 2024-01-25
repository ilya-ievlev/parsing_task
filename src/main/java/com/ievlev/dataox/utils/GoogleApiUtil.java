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
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
// TODO: 25-Jan-24 наверное переименовать это в сервис (разобраться с иерархией)
public class GoogleApiUtil {
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE);
    private static final String TOKENS_DIRECTORY_PATH = "tokens/path";
    private static final String APPLICATION_NAME = "Google Sheets API"; // TODO: 25-Jan-24 изучить на что влияет

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
//        InputStream in = SheetsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH); // TODO: 25-Jan-24 вот тут прям не факт, разобраться для чего это берется и откуда
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

    @SneakyThrows
    private Sheets getSheetService() {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport(); // TODO: 25-Jan-24 сделать что-то с исключениями
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @SneakyThrows
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
        Spreadsheet createdResponse = service.spreadsheets().create(spreadsheet).execute();
//        googleSheetModel.setUrlToGoogleSheet(service.spreadsheets().create(spreadsheet).execute().getSpreadsheetUrl()); // TODO: 25-Jan-24 с каждым запросом возвращать ссылку в json ответе, потом просто модифицировать ту же таблицу
        googleSheetModel.setUrlToGoogleSheet(createdResponse.getSpreadsheetUrl());
        googleSheetModel.setSpreadsheetId(createdResponse.getSpreadsheetId());
        return googleSheetModel;
    }

    @SneakyThrows
    public void addJobToSheet(Job job, int numberOfRaw, GoogleSheetModel googleSheetModel) {
        ValueRange valueRange = new ValueRange().setValues(convertJobToListOfList(job));
        googleSheetModel.getService().spreadsheets().values()
                .update(googleSheetModel.getSpreadsheetId(), "A" + numberOfRaw, valueRange)
                .setValueInputOption("RAW").execute();
    }

    private List<List<Object>> convertJobToListOfList(Job job) {
        List<List<Object>> dataToBeInserted = new ArrayList<>();
        dataToBeInserted.add(Collections.singletonList(job.getJobPageUrl()));
        dataToBeInserted.add(Collections.singletonList(job.getPositionName()));
        dataToBeInserted.add(Collections.singletonList(job.getOrganizationUrl()));
        dataToBeInserted.add(Collections.singletonList(job.getLaborFunction()));
        dataToBeInserted.add(Collections.singletonList(job.getLocation()));
        dataToBeInserted.add(Collections.singletonList(String.valueOf(job.getPostedDate()))); // TODO: 25-Jan-24 тут дату нужно перевести в читаемый вид для таблицы
        dataToBeInserted.add(Collections.singletonList(job.getDescription()));
        dataToBeInserted.add(Collections.singletonList(job.getTagNames()));
        return dataToBeInserted;
    }
}
