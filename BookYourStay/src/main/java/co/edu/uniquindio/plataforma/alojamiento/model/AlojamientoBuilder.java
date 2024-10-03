package co.edu.uniquindio.plataforma.alojamiento.model;

import co.edu.uniquindio.plataforma.alojamiento.model.hotel.HotelBuilder;
//import lombok.ToString;
//
//@ToString
public abstract class AlojamientoBuilder<T extends AlojamientoBuilder<T>> {
    protected String nombre;
    protected Ciudad ciudad;
    protected String descripcion;
    protected String imagen;
    protected double precioNoche;
    protected int capacidadMaxima;

    public T nombre(String nombre) {
        this.nombre = nombre;
        return (T) this;
    }

    public T ciudad(Ciudad ciudad) {
        this.ciudad = ciudad;
        return (T) this;
    }

    public T descripcion(String descripcion) {
        this.descripcion = descripcion;
        return (T) this;
    }

    public T imagen(String imagen) {
        this.imagen = imagen;
        return (T) this;
    }

    public T precioNoche(double precioNoche) {
        this.precioNoche = precioNoche;
        return (T) this;
    }

    public T capacidadMaxima(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
        return (T) this;
    }

    public abstract Alojamiento build();

    //    """
//       @Override
//    public String toString() {
//        final StringBuffer sb = new StringBuffer("Casa{");
//        sb.append("\ntieneCocina = ").append(tieneCocina);
//        sb.append("\n\t tieneJardin = ").append(tieneJardin);
//        sb.append("\n\t nombre = '").append(nombre).append('\'');
//        sb.append("\n\t ciudad = ").append(ciudad);
//        sb.append("\n\t descripcion = '").append(descripcion).append('\'');
//        sb.append("\n\t imagen = '").append(imagen).append('\'');
//        sb.append("\n\t precioNoche = ").append(precioNoche);
//        sb.append("\n\t capacidadMaxima = ").append(capacidadMaxima);
//        sb.append("\n\t servicios =").append(servicios);
//        sb.append('}');
//        return sb.toString();
//    }
//    """
}

