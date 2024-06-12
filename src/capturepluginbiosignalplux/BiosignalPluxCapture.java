/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capturepluginbiosignalplux;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Italo
 */


public class BiosignalPluxCapture {

    private BufferedReader lector;
    private String linea;
    private String[] datos;
    private int var = 0;

    public void ejecutaPython(String mac, int duration, int frequency, String code) {

        Runtime rt = Runtime.getRuntime();
        Process p = null;

        try {
            p = rt.exec("C:\\Users\\Italo\\Documents\\NetBeansProjects\\CapturePluginBiosignalPlux\\OneDeviceAcquisitionExample.exe --mac \"" + mac + "\" --duration " + duration + " --frequency " + frequency + " --code \"" + code + "\" ");

            // Leer la salida del proceso
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Leer la salida de error del proceso
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<List<String>> leerArchivo(String nombreArchivo) {

        List<List<String>> data = new ArrayList<>();

        try {
            lector = new BufferedReader(new FileReader(nombreArchivo));
            while ((linea = lector.readLine()) != null) {
                String[] values = linea.split(",");

                List<String> row = new ArrayList<>();
                for (String value : values) {
                    row.add(value);
                }
                if (var == 1) {
                    data.add(row);
                }
                var = 1;
            }
            lector.close();
            linea = null;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        return data;
    }

    public List<String> BiosignalPluxGetData(List<List<String>> data, int column) {
        //0: ID
        //1: microVolts
        //2: CaptureSingalTime

        List<String> BPData = new ArrayList<>();
        for (List<String> row : data) {

            switch (column) {
                case 0:
                    BPData.add(row.get(0));
                    break;
                case 1:
                    BPData.add(row.get(1));
                    break;
                default:
                    BPData.add(row.get(2));
                    break;
            }

            //microVolts += Integer.parseInt(row.get(1));
//            Id.add(row.get(0));
//            EMG_signal.add(row.get(1));
//            Time.add(row.get(2));
        }

//        String inicioCaptura = Time.get(0);
//        String finCaptura = Time.get(Time.size() - 1);
//
//        System.out.println("Hora Inicio: " + inicioCaptura
//                + "\nHora Termino: " + finCaptura + "\n");
        return BPData;
    }

    public void BiosignalPluxAnalysis(List<String> EMG_signal) {
        // Convertir la lista de Strings a un array de dobles
        double[] emgSignal = EMG_signal.stream().mapToDouble(Double::parseDouble).toArray();
        List<Double> emgSignalAbs = getAbsoluteValues(EMG_signal);
        double[] EMGSignalAbs = convertListToArray(emgSignalAbs);

        // Calcular las estadísticas básicas usando DescriptiveStatistics
        DescriptiveStatistics stats = new DescriptiveStatistics(emgSignal);
        DescriptiveStatistics statsAbs = new DescriptiveStatistics(EMGSignalAbs);

        double meanEmg = stats.getMean();
        double stdEmg = stats.getStandardDeviation();
        double cvPercentage = (stdEmg / meanEmg) * 100;
        double rmsValue = calculateRMS(emgSignal);
        double meanAbsValue = statsAbs.getMean(); // mean(abs(emgSignal))
        double peakValue = stats.getMax();
        double formFactor = rmsValue / meanAbsValue;
        double crestFactor = peakValue / rmsValue;

        // Mostrar resultados
//        System.out.println("Total registros: " + data.size());
        System.out.printf("mean_emg = %.2f\n", meanEmg);

        System.out.printf("std_emg = %.2f\n", stdEmg);
        System.out.printf("cv_percentage = %.2f\n", cvPercentage);
        System.out.printf("rms_value = %.2f\n", rmsValue);
        System.out.printf("mean_abs_value = %.2f\n", meanAbsValue);
        System.out.printf("peak_value = %.2f\n", peakValue);
        System.out.printf("form_factor = %.2f\n", formFactor);
        System.out.printf("crest_factor = %.2f\n", crestFactor);
//        
//
    }

    public static double[] convertListToArray(List<Double> doubleList) {
        double[] doubleArray = new double[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            doubleArray[i] = doubleList.get(i);
        }
        return doubleArray;
    }
// Método para calcular el RMS (Root Mean Square)

    public static double calculateRMS(double[] values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value * value;
        }
        return Math.sqrt(sum / values.length);
    }

    public static List<Double> getAbsoluteValues(List<String> stringList) {
        List<Double> absoluteValues = new ArrayList<>();
        for (String s : stringList) {
            try {
                double value = Double.parseDouble(s);
                absoluteValues.add(Math.abs(value));
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear el valor: " + s);
            }
        }
        return absoluteValues;
    }



}
