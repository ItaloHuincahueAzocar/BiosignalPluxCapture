package capturepluginbiosignalplux;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.capture.RecordableConfiguration;
import mo.organization.Configuration;
import mo.organization.Participant;
import mo.organization.ProjectOrganization;

public class BiosignalPluxCaptureConfiguration implements RecordableConfiguration {
    
    private String id;
    BiosignalPluxRecorder sr;
    private int samplingRate;
    private int sensor;
    private static final Logger logger = Logger.getLogger(BiosignalPluxRecorder.class.getName());

    BiosignalPluxCaptureConfiguration(String id, int sensor, int samplingRate) {
        this.id = id;
        this.sensor = sensor;
        this.samplingRate = samplingRate;
    }
    
    BiosignalPluxCaptureConfiguration(){
        
    }

    @Override
    public void setupRecording(File stageFolder, ProjectOrganization org, Participant p) {
         sr = new BiosignalPluxRecorder(stageFolder, org, p,sensor,samplingRate, this);
    }

    @Override
    public void startRecording() {
            sr.StartRecord();
    }

    @Override
    public void stopRecording() {
        sr.StopRecord();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public File toFile(File parent) {
        try {
            File f = new File(parent, "biosignalplux_"+id+"-"+sensor+"_"+samplingRate+".xml");
            f.createNewFile();
            return f;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Configuration fromFile(File file) {
        String fileName = file.getName();
        if (fileName.contains("_") && fileName.contains(".") && fileName.contains("-")){
            String newId = fileName.substring(fileName.indexOf('_') + 1, fileName.indexOf("-"));
            String newSensor = fileName.substring(fileName.indexOf('-') + 1, fileName.lastIndexOf("_"));
            String newSR = fileName.substring(fileName.lastIndexOf('_') + 1, fileName.lastIndexOf("."));
            BiosignalPluxCaptureConfiguration c = new BiosignalPluxCaptureConfiguration(newId,Integer.parseInt(newSensor),Integer.parseInt(newSR));
            return c;
        }
        return null;
    }

    @Override
    public void cancelRecording() {
        sr.CancelRecord();
    }

    @Override
    public void pauseRecording() {
        sr.PauseRecord();
    }

    @Override
    public void resumeRecording() {
        sr.ResumeRecod();
    }
    
}
