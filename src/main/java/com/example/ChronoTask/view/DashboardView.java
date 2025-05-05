package com.example.ChronoTask.view;


import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("dashboard")
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        add(new H1("Добро пожаловать в ChronoTask!"));
    }
}
