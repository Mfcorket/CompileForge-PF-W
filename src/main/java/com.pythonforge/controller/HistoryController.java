package com.pythonforge.controller;


import com.pythonforge.history.BuildHistoryRecord;
import com.pythonforge.history.BuildHistoryService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.collections.transformation.FilteredList;
import javafx.scene.input.MouseButton;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.awt.Desktop;
import java.io.File;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * 构建历史控制器。
 */
public final class HistoryController {


    @FXML
    private TableView<BuildHistoryRecord> historyTable;


    @FXML
    private TableColumn<BuildHistoryRecord,String> timeColumn;


    @FXML
    private TableColumn<BuildHistoryRecord,String> nameColumn;


    @FXML
    private TableColumn<BuildHistoryRecord,String> pythonColumn;


    @FXML
    private TableColumn<BuildHistoryRecord,String> statusColumn;


    @FXML
    private TableColumn<BuildHistoryRecord,String> outputColumn;

    @FXML
    private TextField searchField;


    private FilteredList<BuildHistoryRecord> filteredRecords;


    private final BuildHistoryService historyService =
            new BuildHistoryService();

    @FXML
    private TableColumn<BuildHistoryRecord,String> logColumn;



    @FXML
    private void initialize(){

        timeColumn.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue()
                                        .getTime()
                                        .format(
                                                DateTimeFormatter
                                                        .ofPattern(
                                                                "yyyy-MM-dd HH:mm:ss"
                                                        )
                                        )
                        )
        );

        logColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()
                                        .getLogFile()
                                        ==null
                                        ?
                                        ""
                                        :
                                        "查看"
                        )
        );


        nameColumn.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue()
                                        .getApplicationName()
                        )
        );


        pythonColumn.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue()
                                        .getPythonVersion()
                        )
        );


        statusColumn.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue()
                                        .getStatus()
                                        .name()
                        )
        );


        outputColumn.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue()
                                        .getOutputFile()
                                        == null
                                        ?
                                        ""
                                        :
                                        data.getValue()
                                                .getOutputFile()
                                                .toString()
                        )
        );


        loadHistory();

        historyTable.setOnMouseClicked(
                event -> {


                    if(event.getClickCount()==2
                            &&
                            event.getButton()
                                    == MouseButton.PRIMARY){


                        BuildHistoryRecord record =
                                historyTable
                                        .getSelectionModel()
                                        .getSelectedItem();


                        if(record!=null){

                            openOutputFile(record);
                        }
                    }
                }
        );

        ContextMenu menu =
                new ContextMenu();


        MenuItem open =
                new MenuItem(
                        "打开文件位置"
                );


        MenuItem delete =
                new MenuItem(
                        "删除记录"
                );


        MenuItem copy =
                new MenuItem(
                        "复制路径"
                );



        open.setOnAction(
                e -> {


                    BuildHistoryRecord record =
                            historyTable
                                    .getSelectionModel()
                                    .getSelectedItem();


                    openOutputFile(record);

                }
        );



        delete.setOnAction(
                e -> {


                    BuildHistoryRecord record =
                            historyTable
                                    .getSelectionModel()
                                    .getSelectedItem();


                    if(record!=null){

                        historyService.remove(
                                record
                        );

                        loadHistory();

                    }

                }
        );



        copy.setOnAction(
                e -> {


                    BuildHistoryRecord record =
                            historyTable
                                    .getSelectionModel()
                                    .getSelectedItem();


                    if(record!=null
                            &&
                            record.getOutputFile()!=null){


                        javafx.scene.input.ClipboardContent content =
                                new javafx.scene.input.ClipboardContent();


                        content.putString(
                                record.getOutputFile()
                                        .toString()
                        );


                        javafx.scene.input.Clipboard
                                .getSystemClipboard()
                                .setContent(content);
                    }

                }
        );



        menu.getItems()
                .addAll(
                        open,
                        delete,
                        copy
                );


        historyTable.setContextMenu(
                menu
        );


    }



    private void loadHistory(){

        List<BuildHistoryRecord> records =
                historyService.getRecords();


        filteredRecords =
                new FilteredList<>(
                        FXCollections.observableArrayList(records),
                        p -> true
                );


        historyTable.setItems(
                filteredRecords
        );


        searchField.textProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue)->{


                            filteredRecords.setPredicate(
                                    record -> {


                                        if(newValue==null
                                                ||
                                                newValue.isBlank()){

                                            return true;
                                        }


                                        String keyword =
                                                newValue
                                                        .toLowerCase();


                                        return
                                                contains(
                                                        record.getApplicationName(),
                                                        keyword
                                                )
                                                        ||
                                                        contains(
                                                                record.getOutputFile()
                                                                        ==null
                                                                        ?
                                                                        ""
                                                                        :
                                                                        record.getOutputFile()
                                                                                .toString(),
                                                                keyword
                                                        );

                                    }
                            );

                        }
                );
    }



    @FXML
    private void handleClear(){

        historyService.clear();

        loadHistory();
    }



    @FXML
    private void handleClose(){

        Stage stage =
                (Stage)
                        historyTable
                                .getScene()
                                .getWindow();

        stage.close();
    }

    private boolean contains(
            String value,
            String keyword){

        return value != null
                &&
                value.toLowerCase()
                        .contains(keyword);
    }

    private void openOutputFile(
            BuildHistoryRecord record){


        try {


            if(record.getOutputFile()==null){

                return;
            }


            Desktop.getDesktop()
                    .open(
                            record.getOutputFile()
                                    .toFile()
                    );


        }catch(Exception ignored){

        }

    }

}