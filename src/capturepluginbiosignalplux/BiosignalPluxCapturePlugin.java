package capturepluginbiosignalplux;

import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.organization.Configuration;
import mo.organization.ProjectOrganization;
import mo.organization.StagePlugin;
import mo.capture.CaptureProvider;

/**
 *
 * @author Italo
 */
@Extension(
        xtends = {
            @Extends(extensionPointId = "mo.capture.CaptureProvider")
        }
)

public class BiosignalPluxCapturePlugin implements CaptureProvider{

    List<Configuration> configurations;

    private static final Logger logger = Logger.getLogger(BiosignalPluxCapturePlugin.class.getName());
    private BiosignalPluxCaptureConfigurationDialog dialog;

    public BiosignalPluxCapturePlugin() {
        configurations = new ArrayList<>();
    }

    @Override
    public String getName() {
        return "BiosignalPlux";
    }

    @Override
    public Configuration initNewConfiguration(ProjectOrganization organization) {

        dialog = new BiosignalPluxCaptureConfigurationDialog(organization);

        boolean accepted = dialog.showDialog();

        if (accepted) {
            BiosignalPluxCaptureConfiguration configuration = new BiosignalPluxCaptureConfiguration(dialog.getConfigurationName(), dialog.sensor_rec, dialog.SR);

            configurations.add(configuration);
            return configuration;
        }

        return null;
    }

    @Override
    public List<Configuration> getConfigurations() {
        return configurations;
    }

    @Override
    public StagePlugin fromFile(File file) {
        if (file.isFile()) {
            try {
                BiosignalPluxCapturePlugin mc = new BiosignalPluxCapturePlugin();
                XElement root = XIO.readUTF(new FileInputStream(file));
                XElement[] pathsX = root.getElements("path");
                for (XElement pathX : pathsX) {
                    String path = pathX.getString();
                    BiosignalPluxCaptureConfiguration c = new BiosignalPluxCaptureConfiguration();
                    Configuration config = c.fromFile(new File(file.getParentFile(), path));
                    if (config != null) {
                        mc.configurations.add(config);
                    }
                }
                return mc;
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @Override
    public File toFile(File parent) {
        File file = new File(parent, "biosignalplux-capture.xml");
        if (!file.isFile()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        XElement root = new XElement("capturers");
        for (Configuration config : configurations) {
            File p = new File(parent, "biosignalplux-capture");
            p.mkdirs();
            File f = config.toFile(p);

            XElement path = new XElement("path");
            Path parentPath = parent.toPath();
            Path configPath = f.toPath();
            path.setString(parentPath.relativize(configPath).toString());
            root.addElement(path);
        }
        try {
            XIO.writeUTF(root, new FileOutputStream(file));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return file;
    }
//    public static void main(String[] args) {
//
//        BiosignalPluxCapture capture = new BiosignalPluxCapture();
//
//        capture.ejecutaPython("98:D3:31:B2:11:33", 10, 100, "0x01");
//    }

}
