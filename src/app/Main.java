package app;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {

    static final int CAPACIDAD_TOTAL = 10;
    static final double TARIFA_HORA_CARRO = 4_000;
    static final double TARIFA_DIA_CARRO = 30_000;
    static final double TARIFA_HORA_MOTO = 2_000;
    static final double TARIFA_DIA_MOTO = 15_000;
    static final int UMBRAL_HORAS_DIA = 8;
    static final int HORA_APERTURA = 8;
    static final int HORA_CIERRE = 22;
    static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    static final Scanner SC = new Scanner(System.in);

    static String[] tipoVehiculo = new String[CAPACIDAD_TOTAL];
    static LocalDateTime[] fechaHoraEntrada = new LocalDateTime[CAPACIDAD_TOTAL];

    static final String TIPO_CARRO = "Carro";   // constantes nuevas
    static final String TIPO_MOTO = "Moto";

    static int celdasOcupadas = 0;
    static int celdasLibres = 0;
    static double recaudoCarros = 0;
    static double recaudoMotos = 0;
    static boolean open = true;


    public static void main(String[] args) {
        imprimir("SISTEMA PARQUEADERO");
        imprimir("     BIENVENIDO    ");

        String opcion;
        do {
            mostrarMenu();
            System.out.print("Elija una opción: ");
            opcion = SC.nextLine().trim();
            switch (opcion) {
                case "1":
                    registrarEntrada();
                    break;
                case "2":
                    imprimir("Registrar salida");
                    break;
                case "3":
                    imprimir("Reporte de ocupacion");
                    break;
                case "4":
                    imprimir("Reporte financiero");
                    break;
                case "5":
                    imprimir("Hasta luego.");
                    break;
                default:
                    imprimir("Opción no disponible todavía.");
                    break;
            }
        } while (!opcion.equals("5"));
        SC.close();
    }

    static void mostrarMenu(){
        imprimir("1. Registrar entrada");
        imprimir("2. Registrar salida");
        imprimir("3. Reporte de ocupación");
        imprimir("4. Reporte financiero");
        imprimir("5. Salir");
    }
    static void registrarEntrada() {
        // 1. Tipo de vehículo
        String tipo = null;
        while (tipo == null) {
            System.out.print("Tipo de vehículo (1. Carro, 2. Moto): ");
            String texto = SC.nextLine().trim();
            if (texto.equals("1")) {
                tipo = TIPO_CARRO;
            } else if (texto.equals("2")) {
                tipo = TIPO_MOTO;
            } else {
                imprimir("Opción inválida. Digite 1 o 2.");
            }
        }

        // 2. Fecha y hora de ingreso
        LocalDateTime ingreso = leerFechaHora("Fecha y hora de ingreso (yyyy-MM-dd HH:mm): ");

        // 3. Buscar una celda libre
        int indice = -1;
        for (int i = 0; i < CAPACIDAD_TOTAL; i++) {
            if (tipoVehiculo[i] == null) {
                indice = i;
                break;
            }
        }
        if (indice == -1) {
            imprimir("El parqueadero está lleno. No se puede registrar el ingreso.");
            return;
        }

        // 4. Guardar en los dos arreglos, en el MISMO índice
        tipoVehiculo[indice] = tipo;
        fechaHoraEntrada[indice] = ingreso;
        celdasOcupadas++;
        celdasLibres--;

        imprimir("Vehículo registrado en la celda " + (indice + 1) + ".");
    }

    static LocalDateTime leerFechaHora(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = SC.nextLine().trim();
            if (!formatoFechaHoraValido(texto)) {
                imprimir("Fecha u hora inválidas. Use yyyy-MM-dd HH:mm y el horario 08:00-22:00.");
                continue;
            }

            return LocalDateTime.parse(texto, FORMATO_FECHA_HORA);
        }
    }

    static boolean formatoFechaHoraValido(String texto) {
        if (texto.length() != 16
                || texto.charAt(4) != '-'
                || texto.charAt(7) != '-'
                || texto.charAt(10) != ' '
                || texto.charAt(13) != ':') {
            return false;
        }

        int anio = leerDigitos(texto, 0, 4);
        int mes = leerDigitos(texto, 5, 2);
        int dia = leerDigitos(texto, 8, 2);
        int hora = leerDigitos(texto, 11, 2);
        int minuto = leerDigitos(texto, 14, 2);

        return anio >= 1 && mes >= 1 && mes <= 12 && dia >= 1 && dia <= 31
                && hora >= HORA_APERTURA && hora <= HORA_CIERRE
                && (hora != HORA_CIERRE || minuto <= 0)
                && minuto >= 0 && minuto <= 59;
    }

    static int leerDigitos(String texto, int inicio, int cantidad) {
        int valor = 0;
        for (int i = inicio; i < inicio + cantidad; i++) {
            char caracter = texto.charAt(i);
            if (caracter < '0' || caracter > '9') {
                return -1;
            }
            valor = valor * 10 + caracter - '0';
        }
        return valor;
    }

    static void imprimir(String mensaje){
        System.out.println(mensaje);
    }
}
