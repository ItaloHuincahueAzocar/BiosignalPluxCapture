package capturepluginbiosignalplux;

import com.biosignalplux.comm.BiosignalPluxException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger; 
import javax.swing.JOptionPane;
import mo.organization.FileDescription;
import mo.organization.Participant;
import mo.organization.ProjectOrganization;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class BiosignalPluxRecorder {

    Participant participant;
    ProjectOrganization org;
    private BiosignalPluxCaptureConfiguration config;
    private File output;
    private String path;
    private String file_name;
    private FileOutputStream outputStream;
    private FileDescription desc;
    // validate MAC address
    private final String MAC = "98:D3:31:B2:11:33";
    
    int duration=5; 
    int frequency=100; 
    String code="0x01";
    
    private int samplerate = 100;
    
    //canales que lee
    final int[] analogs = {0};
    long resume = 0;
    long pause;
    
    private int sensor_op;

    private static final Logger logger = Logger.getLogger(BiosignalPluxRecorder.class.getName());

    public int sw = 1;

    private BufferedReader lector;
    private String linea;
    private String[] datos;
    private int var = 0;
    
    
    
    
    public BiosignalPluxRecorder(File stageFolder, ProjectOrganization org, Participant p, 
                                 int sensor, int samplingRate, BiosignalPluxCaptureConfiguration c) {
        participant = p;
        this.org = org;
        this.config = c;
        this.samplerate = samplingRate;
        switch (sensor) {
            case 1:
                this.sensor_op = 0;
//            case 10:
//                this.sensor_op = 1;
//            case 11:
//                this.sensor_op = 3;
//            case 100:
//                this.sensor_op = 2;
//            case 101:
//                this.sensor_op = 4;
//            case 110:
//                this.sensor_op = 5;
//            case 111:
//                this.sensor_op = 6;
        }
        createFile(stageFolder);
    }

    private void createFile(File parent) {

        Date now = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");

        String reportDate = df.format(now);

        output = new File(parent, reportDate + "_" + config.getId() + ".txt");
        path = parent.getAbsolutePath();
        file_name = reportDate + "_" + config.getId();
        try {
            output.createNewFile();
            outputStream = new FileOutputStream(output);
            desc = new FileDescription(output, BiosignalPluxRecorder.class.getName() + sensor_op);
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    }

    private void deleteFile() {
        if (output.isFile()) {
            output.delete();
        }
        if (desc.getDescriptionFile().isFile()) {
            desc.deleteFileDescription();
        }
    }

    private class Record implements Runnable {

        @Override
        public void run() {


        Runtime rt = Runtime.getRuntime();
        Process p = null;

        try {
            p = rt.exec("C:\\Users\\Italo\\Documents\\NetBeansProjects\\"
                    + "CapturePluginBiosignalPlux\\"
                    + "OneDeviceAcquisitionExample.exe --mac \"" + MAC + "\" --duration " + duration + " --frequency " + frequency + " --code \"" + code + "\" ");
            JOptionPane.showMessageDialog(null, "Comienza la captura", "BiosignalPlux", JOptionPane.ERROR_MESSAGE);
            
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
            JOptionPane.showMessageDialog(null, "No se ha encontrado dispositivo", "Bitalino", JOptionPane.ERROR_MESSAGE);
                
            CancelRecord();
        }

    }
        
    }

    public void StartRecord() {
        Thread t = new Thread(new Record());
        t.start();
    }

    public void StopRecord() {
        sw = 0;
    }

    public void PauseRecord() {
        sw = 2;
        pause = System.currentTimeMillis() - resume;
    }

    public void ResumeRecod() {
        sw = 1;
        resume = System.currentTimeMillis() - pause;
    }

    public void CancelRecord() {
        StopRecord();
        deleteFile();
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
        }
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
