package co.edu.uniquindio.reservasapp.plataforma.controlador;

import co.edu.uniquindio.reservasapp.ObserverPatern.Observable;
import co.edu.uniquindio.reservasapp.ObserverPatern.Observer;
import co.edu.uniquindio.reservasapp.plataforma.modelo.Persona;
import co.edu.uniquindio.reservasapp.plataforma.AppReservasPrincipal;
import co.edu.uniquindio.reservasapp.plataforma.modelo.resevacion.Reserva;
import co.edu.uniquindio.reservasapp.plataforma.servicio.ServiciosApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ControladorPrincipal implements ServiciosApp {

    private static ControladorPrincipal INSTANCIA;
    AppReservasPrincipal  appReservasPrincipal = AppReservasPrincipal.getInstance();

    private ControladorPrincipal() {
    }

    public static ControladorPrincipal getInstancia() {
        if (INSTANCIA == null) {INSTANCIA = new ControladorPrincipal();}
        return INSTANCIA;
    }
    @Override
    public Persona login(String correo, String contrasena) throws Exception {
        return appReservasPrincipal.login(correo, contrasena);
    }

    @Override
    public void registrarPersona(String cedula, String nombres, String apellidos, String email, String contrasena) throws Exception {

    }


    @Override
    public Reserva crearReserva(String idReserva, String ciudadAlojamiento, String nombreHospedaje, String cedulaReservante, LocalDate diaInicioReserva, LocalDate diaFinReserva, String horaInicioReserva, String horaFinReserva, double costo, int capacidadMaxima)throws Exception{
        return appReservasPrincipal.crearReserva(idReserva, ciudadAlojamiento, nombreHospedaje, cedulaReservante, diaInicioReserva, diaFinReserva, horaInicioReserva, horaFinReserva, costo, capacidadMaxima);
    }

    @Override
    public void generarFactura(Reserva reserva) throws Exception {
        appReservasPrincipal.generarFactura(reserva);
    }


    @Override
    public void generarCodigoQR() throws Exception {

    }




    @Override
    public Optional<Persona> obtenerPersona(String cedula) {
        return appReservasPrincipal.getListaClientes().stream()
                .filter(persona -> persona.getCedula().equals(cedula))
                .findFirst();
    }

    @Override
    public List<Reserva> listarTodasReservas() {
        return List.of();
    }


    //TODO Completar con el resto de métodos necesarios para la aplicación
//
//    public void mostrarAlerta(String mensaje, String titulo, Alert.AlertType tipo) {
//        Alert alert = new Alert(tipo);
//        alert.setTitle(titulo);
//        alert.setHeaderText(null);
//        alert.setContentText(mensaje);
//        alert.showAndWait();
//    }

    public  void navegarVentana(String nombreArchivoFxml, String tituloVentana) {
        try {
            // Cargar la vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource(nombreArchivoFxml));
            Parent root = loader.load();
            // Crear la escena
            Scene scene = new Scene(root);
            // Crear un nuevo escenario (ventana)
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setTitle(tituloVentana);
            // Mostrar la nueva ventana
            stage.show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void cerrarVentana(Node node){
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }

    public void mostrarAlerta(String mensaje, Alert.AlertType tipoAlerta) {
        Alert alerta = new Alert(tipoAlerta);
        alerta.setTitle("Mensaje");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
    public void cargarFXMLEnPanel(Pane panelDestino, String rutaFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Node contenido = loader.load();

            panelDestino.getChildren().clear();

            panelDestino.getChildren().add(contenido);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al cargar el archivo FXML: " + rutaFXML);
        }
    }
    public void cargarFXMLEnPanelcoObserver(Pane panelDestino, String rutaFXML, Observer observer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Node contenido = loader.load();

            Observable observable = loader.getController();
            observable.addObserver(observer);
            panelDestino.getChildren().clear();
            panelDestino.getChildren().add(contenido);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al cargar el archivo FXML: " + rutaFXML);
        }
    }
    public  void navegarVentanaObservable(String nombreArchivoFxml, String tituloVentana, Observer observer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(nombreArchivoFxml));
            Parent root = loader.load();
            Observable observable = loader.getController();
            observable.addObserver(observer);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setTitle(tituloVentana);
            stage.show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
