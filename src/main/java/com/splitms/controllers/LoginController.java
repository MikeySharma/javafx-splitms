package com.splitms.controllers;

import com.splitms.pages.ViewNavigator;
import javafx.fxml.FXML;

public class LoginController implements NavigatorAware {

    private ViewNavigator navigator;

    @Override
    public void setNavigator(ViewNavigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void onBack() {
        navigator.showIndex();
    }
}