package app;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

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
    static int celdasLibres = CAPACIDAD_TOTAL;
    static double recaudoCarros = 0;
    static double recaudoMotos = 0;


    public static void main(String[] args) {
        imprimir("SISTEMA PARQUEADERO");
        imprimir("     BIENVENIDO    ");

        int opcion;
        do {
            mostrarMenu();
            System.out.print("Elija una opción: ");
            opcion = leerEnteroValidado("Elija una opción: ", 1, 5);
            switch (opcion) {
                case 1:
                    registrarEntrada();
                    break;
                case 2:
                    registrarSalida();
                    break;
                case 3:
                    imprimir("Reporte de ocupacion");
                    break;
                case 4:
                    imprimir("Reporte financiero");
                    break;
                case 5:
                    imprimir("Hasta luego.");
                    break;
                default:
                    imprimir("Opción no disponible todavía.");
                    break;
            }
        } while (opcion != 5);
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

        int opcionTipo = leerEnteroValidado("Tipo de vehículo (1. Carro, 2. Moto): ", 1, 2);
        String tipo = (opcionTipo == 1) ? TIPO_CARRO : TIPO_MOTO;

        LocalDateTime ingreso = leerFechaHora("Fecha y hora de ingreso (yyyy-MM-dd HH:mm): ");

        int indice = celdaDisponible();
        if (indice == -1) {
            imprimir("El parqueadero está lleno. No se puede registrar el ingreso.");
            return;
        }

        tipoVehiculo[indice] = tipo;
        fechaHoraEntrada[indice] = ingreso;
        celdasOcupadas++;
        celdasLibres--;

        imprimir("Vehículo registrado en la celda " + (indice + 1) + ".");
    }

    static void registrarSalida() {
        if (celdasOcupadas == 0) {
            imprimir("No hay vehículos en el parqueadero.");
            return;
        }

        // 1. Celda: dentro del rango y ocupada
        int indice;
        while (true) {
            int celda = leerEnteroValidado("Número de celda (1-" + CAPACIDAD_TOTAL + "): ", 1, CAPACIDAD_TOTAL);
            indice = celda - 1;
            if (tipoVehiculo[indice] != null) {
                break;
            }
            imprimir("La celda " + celda + " está libre. Elija una celda ocupada.");
        }

        // 2. Fecha de salida: no puede ser anterior al ingreso
        LocalDateTime salida;
        while (true) {
            salida = leerFechaHora("Fecha y hora de salida (yyyy-MM-dd HH:mm): ");
            if (!salida.isBefore(fechaHoraEntrada[indice])) {
                break;
            }
            imprimir("La salida no puede ser anterior al ingreso ("
                    + fechaHoraEntrada[indice].format(FORMATO_FECHA_HORA) + ").");
        }

        // 3. Liquidar y acumular (el tipo se guarda ANTES de liberar la celda)
        boolean esCarro = tipoVehiculo[indice].equals(TIPO_CARRO);
        double total = liquidar(indice, salida);
        if (esCarro) {
            recaudoCarros += total;
        } else {
            recaudoMotos += total;
        }

        // 4. Liberar la celda
        tipoVehiculo[indice] = null;
        fechaHoraEntrada[indice] = null;
        celdasOcupadas--;
        celdasLibres++;

        imprimir("Celda " + (indice + 1) + " liberada.");
    }

    static double liquidar(int indice, LocalDateTime salida) {
        LocalDateTime ingreso = fechaHoraEntrada[indice];
        String tipo = tipoVehiculo[indice];
        boolean esCarro = tipo.equals(TIPO_CARRO);
        double tarifaHora = esCarro ? TARIFA_HORA_CARRO : TARIFA_HORA_MOTO;
        double tarifaDia = esCarro ? TARIFA_DIA_CARRO : TARIFA_DIA_MOTO;

        long diasACobrar = contarDiasCompletos(ingreso, salida);
        LocalDateTime cursor = ingreso.plusDays(diasACobrar);
        long horasACobrar = calcularHorasRestantes(cursor, salida);

        if (horasACobrar >= UMBRAL_HORAS_DIA) {
            diasACobrar++;
            horasACobrar = 0;
        }

        double total = diasACobrar * tarifaDia + horasACobrar * tarifaHora;
        imprimirFactura(indice, tipo, ingreso, salida,
                diasACobrar, tarifaDia, horasACobrar, tarifaHora, total);
        return total;
    }

    static void imprimirFactura(int indice, String tipo, LocalDateTime ingreso,
                                LocalDateTime salida, long dias, double tarifaDia,
                                long horas, double tarifaHora, double total) {
        imprimir("\n----------- FACTURA -----------");
        imprimir("Celda liberada: " + (indice + 1));
        imprimir("Tipo de vehículo: " + tipo);
        imprimir("Ingreso: " + ingreso.format(FORMATO_FECHA_HORA));
        imprimir("Salida:  " + salida.format(FORMATO_FECHA_HORA));
        if (dias > 0) {
            imprimir(dias + " día(s) x " + dinero(tarifaDia) + " = " + dinero(dias * tarifaDia));
        }
        if (horas > 0) {
            imprimir(horas + " hora(s) x " + dinero(tarifaHora) + " = " + dinero(horas * tarifaHora));
        }
        imprimir("TOTAL A PAGAR: " + dinero(total));
        imprimir("-------------------------------");
    }

    static String dinero(double valor) {
        return String.format("$%,.0f", valor);
    }

    static int celdaDisponible() {
        for (int i = 0; i < CAPACIDAD_TOTAL; i++) {
            if (tipoVehiculo[i] == null) {
                return i;
            }
        }
        return -1;
    }

    static int leerEnteroValidado(String mensaje, int minimo, int maximo) {
        while (true) {
            System.out.print(mensaje);
            String texto = SC.nextLine().trim();
            int valor = (texto.isEmpty() || texto.length() > 9)
                    ? -1
                    : leerDigitos(texto, 0, texto.length());
            if (valor >= minimo && valor <= maximo) {
                return valor;
            }
            imprimir("Dato inválido. Ingrese un número entre " + minimo + " y " + maximo + ".");
        }
    }

    static LocalDateTime leerFechaHora(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = SC.nextLine().trim();
            if (!formatoFechaHoraValido(texto)) {
                imprimir("Fecha u hora inválidas. Use yyyy-MM-dd HH:mm y el horario 08:00-22:00.");
                continue;
            }

            LocalDateTime fecha = LocalDateTime.parse(texto, FORMATO_FECHA_HORA);
            if (!fecha.format(FORMATO_FECHA_HORA).equals(texto)) {
                imprimir("Esa fecha no existe en el calendario.");
                continue;
            }
            return fecha;
        }
    }

    static long contarDiasCompletos(LocalDateTime ingreso, LocalDateTime salida) {
        return ChronoUnit.DAYS.between(ingreso, salida);
    }

    static long calcularHorasRestantes(LocalDateTime cursor, LocalDateTime salida) {
        long minutosRestantes = Duration.between(cursor, salida).toMinutes();
        return (minutosRestantes + 59) / 60;
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
