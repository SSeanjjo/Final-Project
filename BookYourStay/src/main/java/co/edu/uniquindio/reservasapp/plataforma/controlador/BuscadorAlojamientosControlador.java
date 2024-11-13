package co.edu.uniquindio.reservasapp.plataforma.controlador;

import co.edu.uniquindio.reservasapp.plataforma.AppReservasPrincipal;
import co.edu.uniquindio.reservasapp.plataforma.alojamiento.model.Alojamiento;
import co.edu.uniquindio.reservasapp.plataforma.alojamiento.model.enums.Ciudad;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import javafx.util.StringConverter;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class BuscadorAlojamientosControlador implements Initializable {
    @FXML
    private DatePicker dpDiasReservar;
    @FXML
    private ListView<Alojamiento> listAlojamientos;
    @FXML
    private ImageView imageAlojamiento;
    @FXML
    private TextArea txtDetallesAlojamiento;
    @FXML
    private ComboBox<String> cbCiudadSeleccionado;
    @FXML
    private TextArea txtInfoReserva; // Nuevo TextArea
    @FXML
    private TextField txtNumeroHuespedes;

    private LocalDate startDate;
    private LocalDate endDate;
    private ObservableList<Alojamiento> observableListaAlojamientos;

    private final AppReservasPrincipal appReservasPrincipal = AppReservasPrincipal.getInstance();

    @FXML
    private void mostrarDetallesAlojamiento() {
        Alojamiento selectedAlojamiento = listAlojamientos.getSelectionModel().getSelectedItem();
        if (selectedAlojamiento != null) {
            txtDetallesAlojamiento.setText(selectedAlojamiento.getDescripcion());
            String imagePath = selectedAlojamiento.getImagenURL();
            Image image = new Image(getClass().getResource(imagePath).toExternalForm());
            imageAlojamiento.setImage(image);
        }
    }

    private void setupDateRangeSelection() {
        dpDiasReservar.setDayCellFactory(createDayCellFactory());

        // Listener para detectar cada selección en el calendario
        dpDiasReservar.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;

            // Alternar entre la selección de startDate y endDate
            if (startDate == null || (startDate != null && endDate != null)) {
                startDate = newValue;
                endDate = null; // Reinicia endDate para un nuevo rango
            } else {
                endDate = newValue;
                dpDiasReservar.setValue(null); // Mantén el DatePicker abierto después de seleccionar ambas fechas
            }

            // Actualizar el TextArea con la información de la reserva
            txtInfoReserva.setText(generateDateRangeText());

            // Cierra y abre de nuevo el popup del DatePicker en cada selección
            dpDiasReservar.hide();
            dpDiasReservar.show();

            // Refrescar DayCellFactory para actualizar los destacados del rango
            dpDiasReservar.setDayCellFactory(createDayCellFactory());

            
        });

        // Convertidor personalizado para mantener el formato del DatePicker (opcional)
        dpDiasReservar.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return generateDateRangeText();
            }

            @Override
            public LocalDate fromString(String string) {
                return null;
            }
        });
    }

    // Método para generar el texto del rango de fechas y el número de días
//    private String generateDateRangeText() {
//        StringBuilder sb = new StringBuilder();
//        if (startDate != null) {
//            sb.append("Inicio de Reservación: ").append(startDate.toString()).append("\n");
//        }
//        if (endDate != null) {
//            sb.append("Fin de Reservación: ").append(endDate.toString()).append("\n");
//        }
//        if (startDate != null && endDate != null) {
//            long days = calculateReservationDays();
//            sb.append("Número de días: ").append(days);
//        }
//        return sb.toString();
//    }

    private String generateDateRangeText() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder();
        if (startDate != null) {
            sb.append("Inicio de Reservación: ").append(startDate.format(formatter)).append("\n");
        }
        if (endDate != null) {
            sb.append("Fin de Reservación: ").append(endDate.format(formatter)).append("\n");
        }
        if (startDate != null && endDate != null) {
            long days = calculateReservationDays();
            sb.append("Número de días: ").append(days);
        }
        return sb.toString();
    }


    // Método auxiliar para cerrar y abrir el DatePicker
    private void toggleDatePickerPopup() {
        if (dpDiasReservar.isShowing()) {
            dpDiasReservar.hide(); // Cierra si está abierto
        }
        dpDiasReservar.show(); // Abre de nuevo
    }

    // DayCellFactory para resaltar el rango de fechas
    private Callback<DatePicker, DateCell> createDayCellFactory() {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #d3d3d3;");
                } else if (startDate != null && endDate != null) {
                    if (item.equals(startDate)) {
                        setStyle("-fx-background-color: #add8e6;"); // Azul claro para startDate
                    } else if (item.equals(endDate)) {
                        setStyle("-fx-background-color: #ffa07a;"); // Salmón claro para endDate
                    } else if (!item.isBefore(startDate) && !item.isAfter(endDate)) {
                        setStyle("-fx-background-color: #90ee90;"); // Verde claro para fechas dentro del rango
                    } else {
                        setStyle(""); // Restablece estilo para fechas fuera del rango
                    }
                }
            }
        };
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDateRangeSelection();
        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));

        // Limpia el texto del editor del DatePicker cuando se muestra o selecciona una fecha
        dpDiasReservar.setOnShowing(event -> dpDiasReservar.getEditor().clear());
        dpDiasReservar.setOnAction(event -> dpDiasReservar.getEditor().clear());

        // Opcional: Inicializar el TextArea con un mensaje predeterminado
        txtInfoReserva.setText("Seleccione las fechas de reservación.");
    }

    private long calculateReservationDays() {
        if (startDate != null && endDate != null) {
            return ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
        return 0;
    }

    public List<String> cargarCiudades() {
        return Arrays.stream(Ciudad.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
/*public class BuscadorAlojamientosControlador implements Initializable {
    @FXML
    private DatePicker dpDiasReservar;
//    @FXML
//    private TextArea txtDiasReservar;
    @FXML
    private TextArea txtInfoReserva;
    @FXML
    private ListView<Alojamiento> listAlojamientos;
    @FXML
    private ImageView imageAlojamiento;
    @FXML
    private TextArea txtDetallesAlojamiento;
    @FXML
    private ComboBox<String> cbCiudadSeleccionado;

    private LocalDate startDate;
    private LocalDate endDate;
    private ObservableList<Alojamiento> observableListaAlojamientos;

    private final AppReservasPrincipal appReservasPrincipal = AppReservasPrincipal.getInstance();

    @FXML
    private void mostrarDetallesAlojamiento() {
        Alojamiento selectedAlojamiento = listAlojamientos.getSelectionModel().getSelectedItem();
        if (selectedAlojamiento != null) {
            txtDetallesAlojamiento.setText(selectedAlojamiento.getDescripcion());
            String imagePath = selectedAlojamiento.getImagenURL();
            Image image = new Image(getClass().getResource(imagePath).toExternalForm());
            imageAlojamiento.setImage(image);
        }
    }
/*
    private void setupDateRangeSelection() {
        dpDiasReservar.setDayCellFactory(createDayCellFactory());

        // Add listener to value property to detect each click on the calendar
        dpDiasReservar.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;

            // Toggle between startDate and endDate selection
            if (startDate == null || (startDate != null && endDate != null)) {
                startDate = newValue;
                endDate = null; // Reset end date for a new range
            } else {
                endDate = newValue;
                dpDiasReservar.setValue(null); // Keep DatePicker open after selecting both dates
            }

            // Refresh display text to show the selected range
            dpDiasReservar.getEditor().clear();
            dpDiasReservar.getEditor().setText(dpDiasReservar.getConverter().toString(newValue));

            // Close and reopen the DatePicker popup on each click
            dpDiasReservar.hide();
            dpDiasReservar.show();

            // Refresh the DayCellFactory to update the highlighted date range
            dpDiasReservar.setDayCellFactory(createDayCellFactory());
        });

        // Custom converter for displaying the selected date range in DatePicker's editor
        dpDiasReservar.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (startDate != null && endDate != null) {
                    return "From: " + startDate + " To: " + endDate + " (" + calculateReservationDays() + " days)";
                }
                return (date != null) ? date.toString() : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return null;
            }
        });
    }
//    ---------------------------------------------------------
/*private void setupDateRangeSelection() {
    dpDiasReservar.setDayCellFactory(createDayCellFactory());

    // Listener para detectar cada selección en el calendario
    dpDiasReservar.valueProperty().addListener((obs, oldValue, newValue) -> {
        if (newValue == null) return;

        // Alternar entre la selección de startDate y endDate
        if (startDate == null || (startDate != null && endDate != null)) {
            startDate = newValue;
            endDate = null; // Reinicia endDate para un nuevo rango
        } else {
            endDate = newValue;
            dpDiasReservar.setValue(null); // Mantén el DatePicker abierto después de seleccionar ambas fechas
        }

        // Actualiza el contenido del TextArea para mostrar el rango de fechas seleccionado
        updateDateRangeText();

        // Cierra y abre de nuevo el popup del DatePicker en cada selección
        dpDiasReservar.hide();
        dpDiasReservar.show();

        // Refrescar DayCellFactory para actualizar los destacados del rango
        dpDiasReservar.setDayCellFactory(createDayCellFactory());
    });
}

    // Método para actualizar el contenido del TextArea con el rango de fechas y número de días
    private void updateDateRangeText() {
        if (startDate != null && endDate != null) {
            long days = calculateReservationDays();
            txtDiasReservar.setText("Inicio de Reservación: " + startDate +
                    "\nFin de Reservación: " + endDate +
                    "\nNúmero de días: " + days);
        } else if (startDate != null) {
            txtDiasReservar.setText("Inicio de Reservación: " + startDate +
                    "\nFin de Reservación: --" +
                    "\nNúmero de días: --");
        }
    }
//    --------------------------------------------------------
private void setupDateRangeSelection() {
    dpDiasReservar.setDayCellFactory(createDayCellFactory());

    // Listener para detectar cada selección en el calendario
    dpDiasReservar.valueProperty().addListener((obs, oldValue, newValue) -> {
        if (newValue == null) return;

        // Alternar entre la selección de startDate y endDate
        if (startDate == null || (startDate != null && endDate != null)) {
            startDate = newValue;
            endDate = null; // Reinicia endDate para un nuevo rango
        } else {
            endDate = newValue;
            dpDiasReservar.setValue(null); // Mantén el DatePicker abierto después de seleccionar ambas fechas
        }

        // Actualizar el TextArea con la información de la reserva
        txtInfoReserva.setText(generateDateRangeText());

        // Cierra y abre de nuevo el popup del DatePicker en cada selección
        dpDiasReservar.hide();
        dpDiasReservar.show();

        // Refrescar DayCellFactory para actualizar los destacados del rango
        dpDiasReservar.setDayCellFactory(createDayCellFactory());
    });

    // Convertidor personalizado para mantener el formato del DatePicker (opcional)
    dpDiasReservar.setConverter(new StringConverter<>() {
        @Override
        public String toString(LocalDate date) {
            return generateDateRangeText();
        }

        @Override
        public LocalDate fromString(String string) {
            return null;
        }
    });
}

    // Método para generar el texto del rango de fechas y el número de días
    private String generateDateRangeText() {
        StringBuilder sb = new StringBuilder();
        if (startDate != null) {
            sb.append("Inicio de Reservación: ").append(startDate.toString()).append("\n");
        }
        if (endDate != null) {
            sb.append("Fin de Reservación: ").append(endDate.toString()).append("\n");
        }
        if (startDate != null && endDate != null) {
            long days = calculateReservationDays();
            sb.append("Número de días: ").append(days);
        }
        return sb.toString();
    }




//-------------------------------------------------------------------
    // DayCellFactory para resaltar el rango de fechas
    private Callback<DatePicker, DateCell> createDayCellFactory() {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #d3d3d3;");
                } else if (startDate != null && endDate != null) {
                    if (item.equals(startDate)) {
                        setStyle("-fx-background-color: #add8e6;"); // Azul claro para startDate
                    } else if (item.equals(endDate)) {
                        setStyle("-fx-background-color: #ffa07a;"); // Salmón claro para endDate
                    } else if (!item.isBefore(startDate) && !item.isAfter(endDate)) {
                        setStyle("-fx-background-color: #90ee90;"); // Verde claro para fechas dentro del rango
                    } else {
                        setStyle(""); // Restablece estilo para fechas fuera del rango
                    }
                }
            }
        };
    }
/*
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDateRangeSelection();
        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));

        // Limpia el texto del editor del DatePicker cuando se muestra o selecciona una fecha
        dpDiasReservar.setOnShowing(event -> dpDiasReservar.getEditor().clear());
        dpDiasReservar.setOnAction(event -> dpDiasReservar.getEditor().clear());
    }

@Override
public void initialize(URL location, ResourceBundle resources) {
    setupDateRangeSelection();
    cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));

    // Limpia el TextArea y el editor del DatePicker cuando se muestra o selecciona una fecha
    txtDiasReservar.clear();
    dpDiasReservar.setOnShowing(event -> dpDiasReservar.getEditor().clear());
    dpDiasReservar.setOnAction(event -> dpDiasReservar.getEditor().clear());
}

//------------------------------------------------------------
    private long calculateReservationDays() {
        if (startDate != null && endDate != null) {
            return ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
        return 0;
    }

    public List<String> cargarCiudades() {
        return Arrays.stream(Ciudad.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}*/
//-------------------------------------------------------------
/*
private void setupDateRangeSelection() {
    dpDiasReservar.setDayCellFactory(createDayCellFactory());

    // Listener para detectar cada selección en el calendario
    dpDiasReservar.valueProperty().addListener((obs, oldValue, newValue) -> {
        if (newValue == null) return;

        // Alternar entre la selección de startDate y endDate
        if (startDate == null || (startDate != null && endDate != null)) {
            startDate = newValue;
            endDate = null; // Reinicia endDate para un nuevo rango
        } else {
            endDate = newValue;
            dpDiasReservar.setValue(null); // Mantén el DatePicker abierto después de seleccionar ambas fechas
        }

        // Actualizar el texto en el editor para mostrar el rango de fechas seleccionado
        dpDiasReservar.getEditor().clear();
        dpDiasReservar.getEditor().setText(generateDateRangeText());

        // Cierra y abre de nuevo el popup del DatePicker en cada selección
        dpDiasReservar.hide();
        dpDiasReservar.show();

        // Refrescar DayCellFactory para actualizar los destacados del rango
        dpDiasReservar.setDayCellFactory(createDayCellFactory());
    });

    // Convertidor personalizado para mostrar el rango de fechas y días en el editor del DatePicker
    dpDiasReservar.setConverter(new StringConverter<>() {
        @Override
        public String toString(LocalDate date) {
            return generateDateRangeText();
        }

        @Override
        public LocalDate fromString(String string) {
            return null;
        }
    });
}

    // Método para generar el texto del rango de fechas y el número de días
    private String generateDateRangeText() {
        if (startDate != null && endDate != null) {
            long days = calculateReservationDays();
            return "Desde: " + startDate + " Hasta: " + endDate + " (" + days + " días)";
        }
        return (startDate != null && endDate != null) ? ("Fecha seleccionada:, \nInicio Reservacion, Fin Reservacion" : "", startDate, endDate);
    }*/
// Método auxiliar para cerrar y abrir el DatePicker
//    private void toggleDatePickerPopup() {
//        if (dpDiasReservar.isShowing()) {
//            dpDiasReservar.hide(); // Cierra si está abierto
//        }
//        dpDiasReservar.show(); // Abre de nuevo
//    }

//-------------------------------------------------------------
/*private void setupDateRangeSelection() {
        dpDiasReservar.setDayCellFactory(createDayCellFactory());

        dpDiasReservar.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;

            // Determine whether to set start or end date
            if (startDate == null || (startDate != null && endDate != null)) {
                startDate = newValue;
                endDate = null; // Reset end date
            } else {
                endDate = newValue;
                dpDiasReservar.setValue(null); // Keep DatePicker open after selecting both dates
            }

            // Refresh display text to show the selected range
            dpDiasReservar.getEditor().clear();
            dpDiasReservar.getEditor().setText(dpDiasReservar.getConverter().toString(newValue));

            // Force a refresh of the DayCellFactory to update cell highlights
            dpDiasReservar.setDayCellFactory(createDayCellFactory());
        });

        // Custom converter for displaying selected date range in DatePicker's editor
        dpDiasReservar.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (startDate != null && endDate != null) {
                    return "From: " + startDate + " To: " + endDate + " (" + calculateReservationDays() + " days)";
                }
                return (date != null) ? date.toString() : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return null;
            }
        });
    }


    // Method for creating a DayCellFactory to highlight date range
    private Callback<DatePicker, DateCell> createDayCellFactory() {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                // Disable past dates
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #d3d3d3;");
                } else if (startDate != null && endDate != null) {
                    if (item.equals(startDate)) {
                        setStyle("-fx-background-color: #add8e6;"); // Light blue for start date
                    } else if (item.equals(endDate)) {
                        setStyle("-fx-background-color: #ffa07a;"); // Light salmon for end date
                    } else if (!item.isBefore(startDate) && !item.isAfter(endDate)) {
                        setStyle("-fx-background-color: #90ee90;"); // Light green for dates within range
                    } else {
                        setStyle(""); // Reset style for non-range dates
                    }
                }
            }
        };
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDateRangeSelection();
        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));

        // Ensure DatePicker editor clears display when showing or selecting a date
        dpDiasReservar.setOnShowing(event -> dpDiasReservar.getEditor().clear());
        dpDiasReservar.setOnAction(event -> dpDiasReservar.getEditor().clear());
    }

    private long calculateReservationDays() {
        if (startDate != null && endDate != null) {
            return ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
        return 0;
    }

    public List<String> cargarCiudades() {
        return Arrays.stream(Ciudad.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}*/
//-------------------------------------------------------
/*private void setupDateRangeSelection() {
//-------------------------------------------------------
        dpDiasReservar.setDayCellFactory(createDayCellFactory());

        dpDiasReservar.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;

            // Determine whether to set start or end date
            if (startDate == null || (startDate != null && endDate != null)) {
                startDate = newValue;
                endDate = null; // Reset end date
            } else {
                endDate = newValue;
                dpDiasReservar.setValue(null); // Keep DatePicker open after selecting both dates
            }

            // Refresh display text to show the selected range
            dpDiasReservar.getEditor().clear();

            // Force a refresh of the DayCellFactory to update cell highlights
            dpDiasReservar.setDayCellFactory(createDayCellFactory());
        });

        // Custom converter for displaying selected date range in DatePicker's editor
        dpDiasReservar.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (startDate != null && endDate != null) {
                    return "From: " + startDate + " To: " + endDate + " (" + calculateReservationDays() + " days)";
                }
                return (date != null) ? date.toString() : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return null;
            }
        });
    }*/
/*
    private void setupDateRangeSelection() {
        dpDiasReservar.setDayCellFactory(createDayCellFactory());

        // Listen to each date selection in the DatePicker
        dpDiasReservar.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;

            // Handle start and end date logic
            if (startDate == null || (startDate != null && endDate != null)) {
                startDate = newValue;
                endDate = null;
            } else {
                endDate = newValue;
                dpDiasReservar.setValue(null); // Clear to keep DatePicker open
            }

            // Force display text update
            dpDiasReservar.getEditor().clear();

            // Refresh day cells to reflect updated range highlights
            dpDiasReservar.setDayCellFactory(createDayCellFactory());
        });

        // Custom converter to display the selected date range
        dpDiasReservar.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (startDate != null && endDate != null) {
                    return "From: " + startDate + " To: " + endDate + " (" + calculateReservationDays() + " days)";
                }
                return (date != null) ? date.toString() : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return null; // No need for reverse conversion
            }
        });
    }

    // Custom DayCellFactory for highlighting date ranges in DatePicker
    private Callback<DatePicker, DateCell> createDayCellFactory() {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #d3d3d3;"); // Light gray for past dates
                } else if (startDate != null && endDate != null) {
                    if (item.equals(startDate)) {
                        setStyle("-fx-background-color: #add8e6;"); // Light blue for start date
                    } else if (item.equals(endDate)) {
                        setStyle("-fx-background-color: #ffa07a;"); // Light salmon for end date
                    } else if (!item.isBefore(startDate) && !item.isAfter(endDate)) {
                        setStyle("-fx-background-color: #90ee90;"); // Light green for dates within range
                    }
                }
            }
        };
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDateRangeSelection();
        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));

        // Ensure DatePicker stays open and synchronizes with selected range
        dpDiasReservar.setOnShowing(event -> dpDiasReservar.getEditor().clear());
        dpDiasReservar.setOnAction(event -> dpDiasReservar.getEditor().clear());
    }

    private long calculateReservationDays() {
        if (startDate != null && endDate != null) {
            return ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
        return 0;
    }

    public List<String> cargarCiudades() {
        return Arrays.stream(Ciudad.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
/*
    private void setupDateRangeSelection() {
        dpDiasReservar.setDayCellFactory(createDayCellFactory());

        dpDiasReservar.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                return; // Do nothing if newValue is null
            }

            // Logic to set start and end dates
            if (startDate == null || (startDate != null && endDate != null)) {
                startDate = newValue;
                endDate = null;
            } else {
                endDate = newValue;
                dpDiasReservar.setValue(null); // Clear to keep DatePicker open

                // Force UI refresh by clearing the display text
                dpDiasReservar.getEditor().clear();
            }

            // Refresh to update highlights on the DatePicker calendar
            dpDiasReservar.setDayCellFactory(createDayCellFactory());
        });

        // Converter for displaying selected range in the DatePicker's text field
        dpDiasReservar.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (startDate != null && endDate != null) {
                    return "From: " + startDate + " To: " + endDate + " (" + calculateReservationDays() + " days)";
                }
                return (date != null) ? date.toString() : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return null; // No need for reverse conversion
            }
        });
    }

    // Factory to highlight dates in the selected range
    private Callback<DatePicker, DateCell> createDayCellFactory() {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #d3d3d3;"); // Light gray for past dates
                } else if (startDate != null && endDate != null) {
                    if (item.equals(startDate)) {
                        setStyle("-fx-background-color: #add8e6;"); // Light blue for start date
                    } else if (item.equals(endDate)) {
                        setStyle("-fx-background-color: #ffa07a;"); // Light salmon for end date
                    } else if (!item.isBefore(startDate) && !item.isAfter(endDate)) {
                        setStyle("-fx-background-color: #90ee90;"); // Light green for dates within range
                    }
                }
            }
        };
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDateRangeSelection();
        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));

        // Handle DatePicker actions to clear text without closing it
        dpDiasReservar.setOnShowing(event -> {
            if (dpDiasReservar.getEditor() != null) {
                dpDiasReservar.getEditor().clear();
            }
        });

        dpDiasReservar.setOnAction(event -> {
            if (dpDiasReservar.getEditor() != null) {
                dpDiasReservar.getEditor().clear();
            }
        });
    }

/*
    private void setupDateRangeSelection() {
        dpDiasReservar.setDayCellFactory(createDayCellFactory());

        dpDiasReservar.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                // Handle the case where the new value is null
                return;
            }

            if (startDate == null || (startDate != null && endDate != null)) {
                startDate = newValue;
                endDate = null;
            } else {
                endDate = newValue;
                dpDiasReservar.setValue(null); // Clear to keep DatePicker open
            }
            dpDiasReservar.setDayCellFactory(createDayCellFactory()); // Refresh to update highlights
        });

        dpDiasReservar.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (startDate != null && endDate != null) {
                    return "From: " + startDate + " To: " + endDate + " (" + calculateReservationDays() + " days)";
                }
                return (date != null) ? date.toString() : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return null; // No need for reverse conversion
            }
        });
    }



    private Callback<DatePicker, DateCell> createDayCellFactory() {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                // Disable past dates
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #d3d3d3;"); // Light gray for disabled past dates
                } else if (startDate != null && endDate != null) {
                    // Different color for start date
                    if (item.equals(startDate)) {
                        setStyle("-fx-background-color: #add8e6;"); // Light blue for start date
                    }
                    // Different color for end date
                    else if (item.equals(endDate)) {
                        setStyle("-fx-background-color: #ffa07a;"); // Light salmon for end date
                    }
                    // Color for dates within the range
                    else if (!item.isBefore(startDate) && !item.isAfter(endDate)) {
                        setStyle("-fx-background-color: #90ee90;"); // Light green for range
                    }
                }
            }
        };
    }



    @FXML
    private void onCiudadSeleccionada() {
        String ciudadSeleccionada = cbCiudadSeleccionado.getValue();
        if (ciudadSeleccionada != null) {
            List<Alojamiento> alojamientosFiltrados = appReservasPrincipal.getListaAlojamientos().stream()
                    .filter(aloj -> aloj.getCiudad().equals(ciudadSeleccionada))
                    .collect(Collectors.toList());
            observableListaAlojamientos = FXCollections.observableArrayList(alojamientosFiltrados);
            listAlojamientos.setItems(observableListaAlojamientos);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDateRangeSelection();
        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));
//        ----------------------------------------------
        // Add an event handler for the DatePicker showing event
        dpDiasReservar.setOnShowing(event -> {
            if (dpDiasReservar.getEditor() != null) {
                dpDiasReservar.getEditor().clear();
            }
        });
        // Add an event handler for the DatePicker action event
        dpDiasReservar.setOnAction(event -> {
            if (dpDiasReservar.getEditor() != null) {
                dpDiasReservar.getEditor().clear();
            }
        });



    }*/
/*@FXML
    private DatePicker dpDiasReservar; // This will be used as startDatePicker
    @FXML
    private DatePicker dpRangoReserva; // This will be used as endDatePicker
    @FXML
    private ListView<Alojamiento> listAlojamientos;
    @FXML
    private ImageView imageAlojamiento;
    @FXML
    private TextArea txtDetallesAlojamiento;
    @FXML
    private ComboBox<String> cbCiudadSeleccionado;

    private LocalDate startDate;
    private LocalDate endDate;
    private ObservableList<Alojamiento> observableListaAlojamientos;
    private final AppReservasPrincipal appReservasPrincipal = AppReservasPrincipal.getInstance();

    @FXML
    private void mostrarDetallesAlojamiento() {
        Alojamiento selectedAlojamiento = listAlojamientos.getSelectionModel().getSelectedItem();
        if (selectedAlojamiento != null) {
            txtDetallesAlojamiento.setText(selectedAlojamiento.getDescripcion());
            String imagePath = selectedAlojamiento.getImagenURL();
            Image image = new Image(getClass().getResource(imagePath).toExternalForm());
            imageAlojamiento.setImage(image);
        }
    }

    private void setupDateRangeSelection() {
        // Using dpDiasReservar as the start date picker and dpRangoReserva as the end date picker
        dpDiasReservar.setOnAction(e -> {
            startDate = dpDiasReservar.getValue();
            if (startDate != null && endDate != null) {
                highlightDateRange(startDate, endDate);
            }
        });

        dpRangoReserva.setOnAction(e -> {
            endDate = dpRangoReserva.getValue();
            if (startDate != null && endDate != null) {
                highlightDateRange(startDate, endDate);
            }
        });
    }

    private void highlightDateRange(LocalDate start, LocalDate end) {
        Callback<DatePicker, DateCell> dayCellFactory = dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                // Highlight range between start and end dates
                if (item != null && (item.isAfter(start) && item.isBefore(end))) {
                    setStyle("-fx-background-color: #C3FDB8;");  // Highlight color
                }
                // Disable dates before today
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #d3d3d3;");  // Gray color for disabled dates
                }
            }
        };

        dpDiasReservar.setDayCellFactory(dayCellFactory); // Apply to start date picker
        dpRangoReserva.setDayCellFactory(dayCellFactory); // Apply to end date picker
    }

    @FXML
    private void onCiudadSeleccionada() {
        String ciudadSeleccionada = cbCiudadSeleccionado.getValue();
        if (ciudadSeleccionada != null) {
            List<Alojamiento> alojamientosFiltrados = appReservasPrincipal.getListaAlojamientos().stream()
                    .filter(aloj -> aloj.getCiudad().equals(ciudadSeleccionada))
                    .collect(Collectors.toList());
            observableListaAlojamientos = FXCollections.observableArrayList(alojamientosFiltrados);
            listAlojamientos.setItems(observableListaAlojamientos);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDateRangeSelection();
        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));
    }

    private long calculateReservationDays() {
        if (startDate != null && endDate != null) {
            return ChronoUnit.DAYS.between(startDate, endDate) + 1; // Include both start and end
        }
        return 0;
    }

    public List<String> cargarCiudades() {
        // Convert the Ciudad enum values to a List<String>
        return Arrays.stream(Ciudad.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

}*/
//-------------------------------------------------------------
/*
    private void setupDateRangeSelection() {
        // Custom cell factory for DatePicker to highlight the range
        dpDiasReservar.setDayCellFactory(createDayCellFactory());

        dpDiasReservar.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (startDate == null || (startDate != null && endDate != null)) {
                startDate = newValue;
                endDate = null;
            } else {
                endDate = newValue;
                dpDiasReservar.setValue(null); // Clear to keep DatePicker open
            }
            dpDiasReservar.setDayCellFactory(createDayCellFactory()); // Refresh to update highlights
        });

        dpDiasReservar.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (startDate != null && endDate != null) {
                    return "From: " + startDate + " To: " + endDate + " (" + calculateReservationDays() + " days)";
                }
                return (date != null) ? date.toString() : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return null; // No need for reverse conversion
            }
        });
    }

    private Callback<DatePicker, DateCell> createDayCellFactory() {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                // Highlight dates between startDate and endDate
                if (startDate != null && endDate != null && !item.isBefore(startDate) && !item.isAfter(endDate)) {
                    setStyle("-fx-background-color: #90ee90;"); // Light green for range
                }

                // Disable past dates
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #d3d3d3;"); // Light gray for disabled past dates
                }
            }
        };
    }

    private long calculateReservationDays() {
        if (startDate != null && endDate != null) {
            return ChronoUnit.DAYS.between(startDate, endDate) + 1; // Include both start and end
        }
        return 0;
    }






    @FXML
    private void onCiudadSeleccionada() {
        String ciudadSeleccionada = cbCiudadSeleccionado.getValue();
        if (ciudadSeleccionada != null) {
            List<Alojamiento> alojamientosFiltrados = appReservasPrincipal.getListaAlojamientos().stream()
                    .filter(aloj -> aloj.getCiudad().equals(ciudadSeleccionada))
                    .collect(Collectors.toList());
            observableListaAlojamientos = FXCollections.observableArrayList(alojamientosFiltrados);
            listAlojamientos.setItems(observableListaAlojamientos);
        }
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDateRangeSelection();
        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));
    }
/*
    private Callback<DatePicker, DateCell> createDayCellFactory() {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (startDate != null && (item.isBefore(startDate) || (endDate != null && item.isAfter(endDate)))) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;"); // Light pink for disabled dates
                } else if (item.equals(startDate) || (endDate != null && (item.isAfter(startDate) && item.isBefore(endDate)))) {
                    setStyle("-fx-background-color: #90ee90;"); // Light green for selected range
                }

//                // Highlight dates between firstDate and secondDate
//                if (firstDate != null && secondDate != null) {
//                    if ((item.isAfter(firstDate) || item.equals(firstDate)) &&
//                            (item.isBefore(secondDate) || item.equals(secondDate))) {
//                        setStyle("-fx-background-color: #90ee90;"); // Light green
//                    }
//                }

                // Optionally disable past or future dates if needed
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #d3d3d3;"); // Light gray for disabled past dates
                }
            }
        };
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDateRangeSelection();

        // Custom cell factory to highlight the selected range
        dpDiasReservar.setDayCellFactory(createDayCellFactory());

        dpDiasReservar.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (startDate == null || (startDate != null && endDate != null)) {
                startDate = newDate;
                endDate = null;
            } else {
                endDate = newDate;
                dpDiasReservar.setValue(null); // Clear the DatePicker value
            }
            dpDiasReservar.setDayCellFactory(createDayCellFactory()); // Refresh cells to update highlights
        });

        dpDiasReservar.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (startDate != null && endDate != null) {
                    return "From: " + startDate + " To: " + endDate;
                }
                return (date != null) ? date.toString() : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return null;  // No conversion needed from string to date
            }
        });


        dpDiasReservar.setOnAction(event -> {
            LocalDate selectedDate = dpDiasReservar.getValue();
            if (selectedDate == null) {
                return;
            }
            if (startDate == null || (startDate != null && endDate != null)) {
                startDate = selectedDate;
                endDate = null;
            } else {
                endDate = selectedDate;
                dpDiasReservar.setValue(null); // Clear the DatePicker value
            }
            dpDiasReservar.setDayCellFactory(createDayCellFactory()); // Refresh cells to update highlights
        });


        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));

    }

    */
/*dpDiasReservar.setOnAction(event -> {
            LocalDate selectedDate = dpDiasReservar.getValue();
            if (selectedDate == null) {
                return;
            }
            if (firstDate == null) {
                // First date selected
                firstDate = selectedDate;
                secondDate = null; // Reset second date
            } else if (secondDate == null) {
                // Second date selected
                secondDate = selectedDate.isAfter(firstDate) ? selectedDate : firstDate;
                firstDate = selectedDate.isAfter(firstDate) ? firstDate : selectedDate;
                dpDiasReservar.setValue(null); // Clear selection to keep DatePicker open
                dpDiasReservar.show(); // Keep the DatePicker open
            } else {
                // Reset if both dates are already selected
                firstDate = selectedDate;
                secondDate = null;
            }
            dpDiasReservar.setDayCellFactory(createDayCellFactory()); // Refresh cells to update highlights
        });

 /* // Custom string converter to keep the DatePicker open until the range is selected
        dpDiasReservar.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (firstDate != null && secondDate != null) {
                    return "From: " + firstDate + " To: " + secondDate;
                }
                return (date != null) ? date.toString() : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return null;  // No conversion needed from string to date
            }
        });
//    @FXML
//    private DatePicker dpDiaInicioReserva;
//    @FXML
//    private DatePicker dpDiaFinReserva;
//    @FXML
//    private ComboBox<String> cbhoraInicioReserva;
//    @FXML
//    private ComboBox<String> cbhoraFinReserva;



//    @FXML
//    private void onDateRangeSelected() {
//        dpDiaInicioReserva.setOnAction(event -> updateEndDate());
//        dpDiaFinReserva.setOnAction(event -> validateEndDate());
//    }

//    private void updateEndDate() {
//        LocalDate startDate = dpDiaInicioReserva.getValue();
//        if (startDate != null) {
//            dpDiaFinReserva.setDayCellFactory(datePicker -> new DateCell() {
//                @Override
//                public void updateItem(LocalDate item, boolean empty) {
//                    super.updateItem(item, empty);
//                    setDisable(empty || item.isBefore(startDate));
//                }
//            });
//        }
//    }

//    private void validateEndDate() {
//        if (dpDiaFinReserva.getValue().isBefore(dpDiaInicioReserva.getValue())) {
//            dpDiaFinReserva.setValue(dpDiaInicioReserva.getValue());
//        }
//    }

//    @FXML
//    private void cargarAlojamientos() {
//        String selectedCity = cbCiudadSeleccionado.getValue();
//        if (selectedCity != null) {
//            List<Alojamiento> alojamientos = appReservasPrincipal.getAlojamientosPorCiudad(selectedCity);
//            listAlojamientos.setItems(FXCollections.observableArrayList(alojamientos));
//        }
//    }
//


   /* private Callback<DatePicker, DateCell> createDayCellFactory() {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {

                super.updateItem(item, empty);

                if (startDate != null && (item.isBefore(startDate) || (endDate != null && item.isAfter(endDate)))) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;"); // Light pink for disabled dates
                } else if (item.equals(startDate) || (endDate != null && (item.isAfter(startDate) && item.isBefore(endDate)))) {
                    setStyle("-fx-background-color: #90ee90;"); // Light green for selected range
                }


                // Highlight dates between firstDate and secondDate
                if (firstDate != null && secondDate != null) {
                    if ((item.isAfter(firstDate) || item.equals(firstDate)) &&
                            (item.isBefore(secondDate) || item.equals(secondDate))) {
                        setStyle("-fx-background-color: #90ee90;"); // Light green
                    }
                }

                // Optionally disable past or future dates if needed
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #d3d3d3;"); // Light gray for disabled past dates
                }
            }
        };
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Custom cell factory to highlight the selected range
        dpDiasReservar.setDayCellFactory(createDayCellFactory());

        dpDiasReservar.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (startDate == null || (startDate != null && endDate != null)) {
                startDate = newDate;
                endDate = null;
            } else {
                endDate = newDate;
                dpDiasReservar.setValue(null); // Clear the DatePicker value
            }
            dpDiasReservar.setDayCellFactory(createDayCellFactory()); // Refresh cells to update highlights
        });


        // Custom string converter to keep the DatePicker open until the range is selected
        dpDiasReservar.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (firstDate != null && secondDate != null) {
                    return "From: " + firstDate + " To: " + secondDate;
                }
                return (date != null) ? date.toString() : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return null;  // No conversion needed from string to date
            }
        });
        dpDiasReservar.setOnAction(event -> {
            LocalDate selectedDate = dpDiasReservar.getValue();
            if (firstDate == null) {
                // First date selected
                firstDate = selectedDate;
                secondDate = null; // Reset second date
            } else if (secondDate == null) {
                // Second date selected
                secondDate = selectedDate.isAfter(firstDate) ? selectedDate : firstDate;
                firstDate = selectedDate.isAfter(firstDate) ? firstDate : selectedDate;
                dpDiasReservar.setValue(null); // Clear selection to keep DatePicker open
                dpDiasReservar.show(); // Keep the DatePicker open
            } else {
                // Reset if both dates are already selected
                firstDate = selectedDate;
                secondDate = null;
            }
            dpDiasReservar.setDayCellFactory(createDayCellFactory()); // Refresh cells to update highlights
        });

        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));

//        dpDiaInicioReserva.setOnAction(event -> updateEndDate());
//        dpDiaFinReserva.setOnAction(event -> validateEndDate());
    }





//cbhoraReserva.setItems(FXCollections.observableArrayList(new Horario().generarHorarios()));
//    @Override
    public void initialize(URL location, ResourceBundle resources) {

        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));
        setupDateRangeSelection();

        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));
        dpDiaInicioReserva.setOnAction(event -> updateEndDate());
        dpDiaFinReserva.setOnAction(event -> validateEndDate());

        // Initialize observableListReservas
        observableListReservas = FXCollections.observableArrayList();
        observableListaAlojamientos = FXCollections.observableArrayList();

        actualizarTabla();

        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));
        observableListReservas = FXCollections.observableArrayList(appReservasPrincipal.listarTodasReservas());
        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(
                appReservasPrincipal.getListaAlojamientos().stream()
                        .map(Alojamiento::getNombre)
                        .collect(Collectors.toList())
        ));
        cbCiudadSeleccionado.valueProperty().addListener((obs, oldVal, newVal) -> actualizarCosto());
        txtCedulaReservante.textProperty().addListener((obs, oldVal, newVal) -> actualizarCosto());
        actualizarTabla();


        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(cargarCiudades()));
//        cargarCiudadesDisponibles();
        observableListReservas = FXCollections.observableArrayList(appReservasPrincipal.listarTodasReservas());
//        tablaReservas.setItems(observableListReservas);

        observableListReservas = FXCollections.observableArrayList(appReservasPrincipal.listarTodasReservas());
//        tablaReservas.setItems(observableListReservas);

        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(
                appReservasPrincipal.getListaAlojamientos().stream()
                        .map(Alojamiento::getNombre)
                        .collect(Collectors.toList())
        ));
        // Initialize reservations and installations
        observableListReservas = FXCollections.observableArrayList(appReservasPrincipal.listarTodasReservas());
        observableListaAlojamientos = FXCollections.observableArrayList(appReservasPrincipal.getListaAlojamientos());


        // Event handler for selecting an instalacion
        cbCiudadSeleccionado.setOnAction(event -> {
            cargarHorasDisponibles();
            String selectedInstalacion = cbCiudadSeleccionado.getValue();
            Alojamiento instalacion = appReservasPrincipal.getListaAlojamientos().stream()
                    .filter(inst -> inst.getNombre().equals(selectedInstalacion))
                    .findFirst()
                    .orElse(null);
            if (instalacion != null) {
                txtNombreHospedaje.setText(instalacion.getNombre());
            }
        });
        // Listener for automatic cost calculation when cedula changes
        txtCedulaReservante.textProperty().addListener((observable, oldValue, newValue) -> {
            calcularYMostrarCosto();
        });

        cbCiudadSeleccionado.setItems(FXCollections.observableArrayList(
                appReservasPrincipal.getListaAlojamientos().stream()
                        .map(Alojamiento::getNombre)
                        .collect(Collectors.toList())
        ));
        cbCiudadSeleccionado.valueProperty().addListener((obs, oldVal, newVal) -> actualizarCosto());
        txtCedulaReservante.textProperty().addListener((obs, oldVal, newVal) -> actualizarCosto());
        actualizarTabla();
    }*/



