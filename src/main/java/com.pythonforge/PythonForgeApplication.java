package com.pythonforge;

import com.pythonforge.service.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class PythonForgeApplication extends Application {

    private static final String APPLICATION_TITLE =
            "PythonForge-win";

    private static final double WINDOW_WIDTH =
            1200;

    private static final double WINDOW_HEIGHT =
            760;


    @Override
    public void start(
            Stage stage)
            throws IOException {

        FXMLLoader loader =
                new FXMLLoader(
                        PythonForgeApplication.class
                                .getResource(
                                        "/fxml/MainView.fxml"
                                )
                );


        Scene scene =
                new Scene(
                        loader.load(),
                        WINDOW_WIDTH,
                        WINDOW_HEIGHT
                );

        URL iconUrl = PythonForgeApplication.class.getResource(
                "/icon/PythonForge.png"
        );

        if (iconUrl != null){
            stage.getIcons().add(
                    new Image(
                            iconUrl.toExternalForm()
                    )
            );

        }


        /*
         * ========================================================
         * P3：初始化全局主题系统
         * ========================================================
         *
         * ThemeManager 持有当前 Scene。
         *
         * 后续 MainView、SettingsView、HistoryView 等页面
         * 都使用这个 Scene，因此主题可以统一应用到整个应用。
         */

        ThemeManager themeManager =
                ThemeManager.getInstance();

        themeManager.initialize(
                scene
        );


        /*
         * ========================================================
         * Stage
         * ========================================================
         */

        stage.setTitle(
                APPLICATION_TITLE
        );

        stage.setScene(
                scene
        );

        stage.setMinWidth(
                1000
        );

        stage.setMinHeight(
                650
        );

        stage.show();
    }




    @Override
    public void stop() {

        ThemeManager
                .getInstance()
                .shutdown();
    }


    public static void main(
            String[] args) {

        launch(args);
    }
}