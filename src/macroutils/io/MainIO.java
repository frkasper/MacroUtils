package macroutils.io;

import java.io.*;
import macroutils.*;

/**
 * Main class for IO methods in MacroUtils.
 *
 * @since January of 2016
 * @author Fabio Kasper
 */
public class MainIO {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     * @param debugOpt given Debug option. True to enable.
     */
    public MainIO(MacroUtils m, boolean debugOpt) {
        _mu = m;
        print = new Print(m);
        read = new Read(m);
        write = new Write(m);
        print.setDebug(debugOpt);
        say = print;
        say.msgDebug("Class loaded: %s...", this.getClass().getSimpleName());
    }

    /**
     * Creates a folder in the same {@link UserDeclarations#simPath}.
     *
     * @param fld given folder name.
     * @return The File. Null if an error is caught.
     */
    public File createFolder(String fld) {
        File f = new File(_ud.simPath, fld);
        say.value("Creating a Folder", f.toString(), true, true);
        if (f.exists()) {
            say.msg(true, "Already exists.");
        } else if (f.mkdir()) {
            say.ok(true);
        } else {
            say.msg(true, "Could not be created due an unknown error.");
            return null;
        }
        return f;
    }

    /**
     * Sleeps for a while.
     *
     * @param ms the length of time to sleep in milliseconds.
     */
    public void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            say.msg(true, "Sleep stopped via: %s", e.getMessage());
        }
    }

    /**
     * This method is called automatically by {@link MacroUtils}. It is internal to MacroUtils.
     */
    public void updateInstances() {
        print.updateInstances();
        read.updateInstances();
        write.updateInstances();
        _ud = _mu.userDeclarations;
        print.msgDebug("" + this.getClass().getSimpleName() + " instances updated succesfully.");
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private UserDeclarations _ud = null;

    /**
     * This class is responsible for reading objects in general with MacroUtils.
     */
    public Read read = null;

    /**
     * This class is responsible for printing information into STAR-CCM+ console/output.
     */
    public Print print = null;

    /**
     * Useful for printing information into console/output. It is the same as {@link #print}.
     */
    public Print say = null;

    /**
     * This class is responsible for writing data in general with MacroUtils.
     */
    public Write write = null;

}
