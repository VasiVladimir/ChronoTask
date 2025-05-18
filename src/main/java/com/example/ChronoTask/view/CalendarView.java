package com.example.ChronoTask.view;

import com.example.ChronoTask.model.Task;
import com.example.ChronoTask.model.NotificationEntity;
import com.example.ChronoTask.security.CustomUserDetails;
import com.example.ChronoTask.service.TaskService;
import com.example.ChronoTask.service.NotificationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route("calendar")
@AnonymousAllowed
@CssImport("./styles/calendar-view.css") // Ваши стили
public class CalendarView extends VerticalLayout {

    private final TaskService taskService;
    private final NotificationService notificationService;


    private YearMonth currentYearMonth;


    private VerticalLayout centerArea;
    private Div centerAreaDiv;
    private VerticalLayout rightPanel;
    private Span monthLabel;

    public CalendarView(TaskService taskService, NotificationService notificationService) {
        this.taskService = taskService;
        this.notificationService = notificationService;
        this.currentYearMonth = YearMonth.now();

        setSizeFull();
        setSpacing(false);
        setPadding(false);


        HorizontalLayout topBar = buildTopBar();


        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(false);

        VerticalLayout sidebar = buildSidebar();
        centerArea = buildCenterArea();
        rightPanel = buildRightPanel();

        mainLayout.add(sidebar, centerArea, rightPanel);
        mainLayout.setFlexGrow(1, centerArea);

        add(topBar, mainLayout);
        expand(mainLayout);
    }


    private HorizontalLayout buildTopBar() {
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.addClassName("calendar-header");
        topBar.setWidthFull();
        topBar.setPadding(true);
        topBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button prevButton = new Button("<<", e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            refreshView();
        });

        monthLabel = new Span(getRussianMonthName(currentYearMonth.getMonth()) + " " + currentYearMonth.getYear());

        Button nextButton = new Button(">>", e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            refreshView();
        });

        HorizontalLayout monthSwitch = new HorizontalLayout(prevButton, monthLabel, nextButton);

        Button notificationBell = new Button("Уведомления", e -> showNotificationsDialog());
        if (hasPendingNotifications()) {
            notificationBell.addClassName("unread-bell");
        } else {
            notificationBell.removeClassName("unread-bell");
        }

        topBar.add(monthSwitch, notificationBell);
        topBar.expand(monthSwitch);
        return topBar;
    }


    private VerticalLayout buildSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.addClassName("sidebar");
        sidebar.setWidth("220px");
        sidebar.setHeightFull();
        sidebar.setPadding(true);
        sidebar.setSpacing(true);

        H3 sideTitle = new H3("Меню");
        Button calendarButton = new Button("Календарь", e -> UI.getCurrent().navigate("calendar"));
        Button archiveButton = new Button("Архив", e -> UI.getCurrent().navigate("archive"));

        sidebar.add(sideTitle, calendarButton, archiveButton);
        return sidebar;
    }


    private VerticalLayout buildCenterArea() {
        VerticalLayout center = new VerticalLayout();
        center.addClassName("calendar-center");
        center.setSizeFull();
        center.setPadding(true);
        center.setSpacing(false);


        HorizontalLayout daysOfWeekHeader = new HorizontalLayout();
        daysOfWeekHeader.setWidthFull();
        daysOfWeekHeader.add(
                new Span("Пн"), new Span("Вт"), new Span("Ср"),
                new Span("Чт"), new Span("Пт"), new Span("Сб"), new Span("Вс")
        );

        centerAreaDiv = new Div();
        centerAreaDiv.addClassName("calendar-grid");
        centerAreaDiv.setWidthFull();

        buildCalendarGrid(centerAreaDiv);

        center.add(daysOfWeekHeader, centerAreaDiv);
        center.setFlexGrow(1, centerAreaDiv);
        return center;
    }


    private void buildCalendarGrid(Div grid) {
        grid.getStyle().set("display", "grid");
        grid.getStyle().set("grid-template-columns", "repeat(7, 1fr)");
        grid.getStyle().set("grid-template-rows", "repeat(6, auto)");
        grid.getStyle().set("gap", "10px");

        LocalDate firstDay = currentYearMonth.atDay(1);
        int lengthOfMonth = currentYearMonth.lengthOfMonth();
        DayOfWeek firstDow = firstDay.getDayOfWeek();
        int shift = (firstDow.getValue() + 6) % 7;

        int cellIndex = 0;


        for (int i = 0; i < shift; i++) {
            Div emptyCell = createEmptyCell();
            placeCellInGrid(emptyCell, cellIndex);
            grid.add(emptyCell);
            cellIndex++;
        }


        for (int day = 1; day <= lengthOfMonth; day++) {
            LocalDate currentDay = firstDay.withDayOfMonth(day);
            Div dayCell = new Div();
            dayCell.addClassName("calendar-day-cell");

            Span dayNum = new Span(String.valueOf(day));
            dayNum.addClassName("day-number");
            dayCell.add(dayNum);


            List<Task> tasksForDay = taskService.findTasksByDateAndUser(currentDay, getCurrentUserId());

            if (!tasksForDay.isEmpty()) {
                for (Task t : tasksForDay) {
                    Span marker = new Span(t.getTitle());
                    marker.addClassName("event-label");


                    if ("COMPLETED".equalsIgnoreCase(t.getStatus()) || "ARCHIVED".equalsIgnoreCase(t.getStatus())) {
                        marker.getStyle().set("background-color", "blue");
                    }

                    else if ("HIGH".equalsIgnoreCase(t.getPriority())) {
                        marker.getStyle().set("background-color", "#FF4D4F");
                    }

                    else {
                        marker.getStyle().set("background-color", "#42b883");
                    }

                    dayCell.add(marker);
                }
            }

            dayCell.getElement().addEventListener("click", e -> openCreateTaskDialog(currentDay));

            placeCellInGrid(dayCell, cellIndex);
            grid.add(dayCell);
            cellIndex++;
        }


        while (cellIndex < 42) {
            Div emptyCell = createEmptyCell();
            placeCellInGrid(emptyCell, cellIndex);
            grid.add(emptyCell);
            cellIndex++;
        }
    }

    private Div createEmptyCell() {
        Div cell = new Div();
        cell.addClassName("calendar-day-cell-empty");
        return cell;
    }

    private void placeCellInGrid(Div cell, int cellIndex) {
        int col = cellIndex % 7;
        int row = cellIndex / 7;
        cell.getStyle().set("grid-column", String.valueOf(col + 1));
        cell.getStyle().set("grid-row", String.valueOf(row + 1));
    }


    private VerticalLayout buildRightPanel() {
        rightPanel = new VerticalLayout();
        rightPanel.addClassName("right-panel");
        rightPanel.setWidth("300px");
        rightPanel.setHeightFull();
        rightPanel.setPadding(true);
        rightPanel.setSpacing(true);

        updateTasksList(rightPanel);
        return rightPanel;
    }

    private void updateTasksList(VerticalLayout panel) {
        panel.removeAll();

        H3 title = new H3("Задачи на " + getRussianMonthName(currentYearMonth.getMonth()) + " " + currentYearMonth.getYear());
        panel.add(title);

        LocalDate start = currentYearMonth.atDay(1);
        LocalDate end = currentYearMonth.atEndOfMonth();
        List<Task> tasks = taskService.findTasksByPeriodAndUser(start, end, getCurrentUserId());


        tasks = tasks.stream()
                .filter(t -> !"COMPLETED".equalsIgnoreCase(t.getStatus()))
                .filter(t -> !"ARCHIVED".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());

        if (tasks.isEmpty()) {
            panel.add(new Span("Нет задач на этот месяц."));
        } else {
            for (Task t : tasks) {
                HorizontalLayout item = new HorizontalLayout();
                item.setWidthFull();
                item.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

                Span circle = new Span("●");
                circle.getStyle().set("font-size", "1.5em");

                if ("HIGH".equalsIgnoreCase(t.getPriority())) {
                    circle.getStyle().set("color", "red");
                } else {
                    circle.getStyle().set("color", "green");
                }

                String dateStr = t.getTaskDate() != null ? t.getTaskDate().toString() : "";
                String timeStr = t.getTaskTime() != null ? t.getTaskTime().toLocalDateTime().toLocalTime().toString() : "";
                String text = t.getTitle() + " (" + dateStr + (timeStr.isEmpty() ? "" : " " + timeStr) + ")";

                Button taskButton = new Button(text, event -> openTaskDetailsDialog(t));
                taskButton.setWidthFull();

                item.add(circle, taskButton);
                panel.add(item);
            }
        }
    }


    private void openCreateTaskDialog(LocalDate date) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(null);
        dialog.addClassName("task-dialog");

        H2 formTitle = new H2("Новая задача на " + date);
        formTitle.addClassName("task-dialog-title");

        TextField titleField = new TextField("Название задачи");
        titleField.setWidthFull();

        TextArea descriptionField = new TextArea("Описание задачи");
        descriptionField.setWidthFull();
        descriptionField.setMinHeight("100px");

        RadioButtonGroup<String> priorityGroup = new RadioButtonGroup<>();
        priorityGroup.setLabel("Приоритет");
        priorityGroup.setItems("HIGH", "LOW");
        priorityGroup.setValue("HIGH");

        TimePicker timePicker = new TimePicker("Время задачи");

        HorizontalLayout priorityTimeLayout = new HorizontalLayout(priorityGroup, timePicker);
        priorityTimeLayout.setWidthFull();
        priorityTimeLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        priorityTimeLayout.setSpacing(true);
        priorityTimeLayout.addClassName("priority-time-layout");

        VerticalLayout rescheduleBlock = new VerticalLayout();
        rescheduleBlock.setVisible(false);
        rescheduleBlock.addClassName("reschedule-block");

        Span rescheduleLabel = new Span("Возможные даты/время для переноса:");
        rescheduleLabel.addClassName("reschedule-label");

        Button addRescheduleBtn = new Button("Добавить дату переноса");
        addRescheduleBtn.addClassName("add-reschedule-btn");

        VerticalLayout rescheduleDatesLayout = new VerticalLayout();
        rescheduleDatesLayout.setSpacing(false);
        rescheduleDatesLayout.setPadding(false);

        addRescheduleBtn.addClickListener(e -> {
            HorizontalLayout row = new HorizontalLayout();
            DatePicker dp = new DatePicker();
            dp.setMin(LocalDate.now());
            TimePicker tp = new TimePicker();
            Button removeBtn = new Button("X", ev -> rescheduleDatesLayout.remove(row));
            row.setSpacing(true);
            row.add(dp, tp, removeBtn);
            rescheduleDatesLayout.add(row);
        });

        rescheduleBlock.add(rescheduleLabel, addRescheduleBtn, rescheduleDatesLayout);

        priorityGroup.addValueChangeListener(e -> {
            if ("LOW".equalsIgnoreCase(e.getValue())) {
                rescheduleBlock.setVisible(true);
            } else {
                rescheduleBlock.setVisible(false);
            }
        });


        VerticalLayout recommendationBlock = new VerticalLayout();
        recommendationBlock.setVisible(false);
        recommendationBlock.addClassName("recommendation-block");

        Button calculatePriorityBtn = new Button("Рассчитать приоритеты", ev -> {
            String title = titleField.getValue();
            String desc = descriptionField.getValue();
            recommendationBlock.removeAll();

            if (title == null || title.isBlank() || desc == null || desc.isBlank()) {
                recommendationBlock.add(new Span("Введите название и описание задачи."));
            } else {
                String text = (title + " " + desc).toLowerCase();

                boolean isUrgent = Stream.of(
                        "срочно", "важно", "очень важно", "немедленно", "как можно скорее",
                        "до завтра", "до вечера", "до конца дня", "сегодня", "в ближайшее время",
                        "дедлайн", "срок", "сдать", "успеть", "не пропустить", "не забыть",
                        "экзамен", "тест", "зачет", "контрольная", "собеседование",
                        "встреча", "переговоры", "звонок", "брифинг", "созвон",
                        "совещание", "презентация", "отчет", "доклад", "вебинар",
                        "интервью", "проект", "финал", "представление",
                        "необходимо", "обязательно", "критично", "на контроле",
                        "оплата", "документы", "подпись", "отправить", "ответить",
                        "почта", "контракт", "сделка", "счет", "подготовка",
                        "проверка", "отчётность", "регистрация", "напоминание",
                        "решение", "ошибка", "проблема", "критическая ошибка",
                        "исправить", "устранить", "по требованию", "директива",
                        "запрос от клиента", "жалоба", "обращение", "поддержка"
                ).anyMatch(text::contains);


                if (isUrgent) {
                    recommendationBlock.add(new Span("🔴 Приоритет задачи — ВЫСОКИЙ. Укажите точное время."));
                    priorityGroup.setValue("HIGH");
                    rescheduleBlock.setVisible(false);
                } else {
                    recommendationBlock.add(new Span("🟢 Приоритет задачи — НИЗКИЙ. Задачу можно перенести."));
                    priorityGroup.setValue("LOW");
                    rescheduleBlock.setVisible(true);
                }
            }

            recommendationBlock.setVisible(true);
        });


        Button saveButton = new Button("Сохранить");
        Button cancelButton = new Button("Отмена", ev -> dialog.close());

        saveButton.addClickListener(ev -> {
            String title = titleField.getValue();
            String desc = descriptionField.getValue();
            LocalTime time = timePicker.getValue();
            String priority = priorityGroup.getValue();

            Task newTask = taskService.createTask(
                    getCurrentUserId(),
                    title,
                    desc,
                    date,
                    (time != null ? time : LocalTime.MIDNIGHT),
                    priority
            );

            if ("LOW".equalsIgnoreCase(priority)) {
                rescheduleDatesLayout.getChildren().forEach(child -> {
                    if (child instanceof HorizontalLayout hl) {
                        DatePicker dp = null;
                        TimePicker tp = null;
                        for (var c : hl.getChildren().toList()) {
                            if (c instanceof DatePicker dpc) dp = dpc;
                            if (c instanceof TimePicker tpc) tp = tpc;
                        }
                        if (dp != null && dp.getValue() != null) {
                            LocalDate ld = dp.getValue();
                            LocalTime lt = (tp != null && tp.getValue() != null) ? tp.getValue() : LocalTime.MIDNIGHT;
                            taskService.addPossibleRescheduleDate(newTask, ld, lt, "Пользователь указал дату переноса");
                        }
                    }
                });
            }

            dialog.close();
            refreshView();
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonsLayout.setWidthFull();
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonsLayout.addClassName("dialog-buttons");

        VerticalLayout formLayout = new VerticalLayout(
                formTitle,
                titleField,
                descriptionField,
                priorityTimeLayout,
                calculatePriorityBtn,
                recommendationBlock,
                rescheduleBlock,
                buttonsLayout
        );
        formLayout.addClassName("task-dialog-form");
        formLayout.setSpacing(true);
        formLayout.setPadding(false);

        dialog.add(formLayout);
        dialog.setWidth("600px");
        dialog.setHeight("auto");
        dialog.open();
    }



    private void openTaskDetailsDialog(Task task) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Задача: " + task.getTitle());

        VerticalLayout layout = new VerticalLayout();
        layout.add(new Span("Описание: " + task.getDescription()));
        layout.add(new Span("Дата: " + (task.getTaskDate() != null ? task.getTaskDate().toString() : "")));
        layout.add(new Span("Время: " + (task.getTaskTime() != null
                ? task.getTaskTime().toLocalDateTime().toLocalTime().toString()
                : "")));
        layout.add(new Span("Приоритет: " + task.getPriority()));
        layout.add(new Span("Статус: " + task.getStatus()));

        HorizontalLayout buttonLayout = new HorizontalLayout();

        if ("HIGH".equalsIgnoreCase(task.getPriority())) {

            Button confirmButton = new Button("Подтвердить выполнение", e -> {
                taskService.confirmTask(task);
                dialog.close();
                refreshView();
            });
            buttonLayout.add(confirmButton);
        } else {

            Button rescheduleButton = new Button("Перенести", e -> {
                dialog.close();
                openRescheduleDialog(task);
            });
            buttonLayout.add(rescheduleButton);

            Button confirmButton = new Button("Подтвердить", e -> {
                taskService.confirmTask(task);
                dialog.close();
                refreshView();
            });
            buttonLayout.add(confirmButton);
        }

        Button closeButton = new Button("Закрыть", e -> dialog.close());
        buttonLayout.add(closeButton);

        layout.add(buttonLayout);
        dialog.add(layout);
        dialog.open();
    }

    private void openRescheduleDialog(Task task) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Перенос задачи: " + task.getTitle());

        VerticalLayout layout = new VerticalLayout();

        DatePicker newDateField = new DatePicker("Новая дата");
        newDateField.setMin(LocalDate.now());

        TimePicker newTimeField = new TimePicker("Новое время");

        TextField reasonField = new TextField("Причина переноса");
        reasonField.setWidthFull();

        Button saveButton = new Button("Сохранить перенос", e -> {
            LocalDate newDate = newDateField.getValue();
            LocalTime newTime = newTimeField.getValue();
            String reason = reasonField.getValue();

            if (newDate == null) {
                reasonField.setErrorMessage("Дата не может быть пустой");
                reasonField.setInvalid(true);
                return;
            }

            taskService.rescheduleTask(task, newDate, (newTime != null ? newTime : LocalTime.MIDNIGHT), reason);
            dialog.close();
            refreshView();
        });

        Button cancelButton = new Button("Отмена", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        layout.add(newDateField, newTimeField, reasonField, buttons);
        dialog.add(layout);
        dialog.open();
    }

    private void showNotificationsDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Уведомления");

        List<NotificationEntity> notifications = notificationService.findAllPendingByUser(getCurrentUserId());
        if (notifications.isEmpty()) {
            dialog.add(new Span("Нет новых уведомлений"));
        } else {
            for (NotificationEntity n : notifications) {
                VerticalLayout notifLayout = new VerticalLayout();
                notifLayout.add(new Span(n.getMessage()));
                Button markReadBtn = new Button("Прочитано", e -> {
                    notificationService.markAsRead(n);
                    dialog.close();
                    refreshView();
                });
                notifLayout.add(markReadBtn);
                dialog.add(notifLayout);
            }
        }

        Button closeAll = new Button("Закрыть", e -> dialog.close());
        dialog.getFooter().add(closeAll);

        dialog.open();
    }



    private boolean hasPendingNotifications() {
        return !notificationService.findAllPendingByUser(getCurrentUserId()).isEmpty();
    }


    private void refreshView() {

        monthLabel.setText(getRussianMonthName(currentYearMonth.getMonth()) + " " + currentYearMonth.getYear());


        centerAreaDiv.removeAll();
        buildCalendarGrid(centerAreaDiv);


        updateTasksList(rightPanel);
    }

    private Long getCurrentUserId() {
        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");
        if (userId == null) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
                userId = ((CustomUserDetails) auth.getPrincipal()).getId();
                VaadinSession.getCurrent().setAttribute("userId", userId);
            }
        }
        if (userId == null) {
            UI.getCurrent().navigate("login");
            throw new IllegalStateException("Пользователь не аутентифицирован!");
        }
        return userId;
    }

    private String getRussianMonthName(Month month) {
        return switch (month) {
            case JANUARY   -> "Январь";
            case FEBRUARY  -> "Февраль";
            case MARCH     -> "Март";
            case APRIL     -> "Апрель";
            case MAY       -> "Май";
            case JUNE      -> "Июнь";
            case JULY      -> "Июль";
            case AUGUST    -> "Август";
            case SEPTEMBER -> "Сентябрь";
            case OCTOBER   -> "Октябрь";
            case NOVEMBER  -> "Ноябрь";
            case DECEMBER  -> "Декабрь";
        };
    }
}
