package macroutils.getter;

import java.util.ArrayList;
import java.util.Arrays;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import star.base.neo.ClientServerObject;
import star.common.ColumnDescriptor;
import star.common.GeometryPart;
import star.common.Units;
import star.meshing.AutoMeshOperation;
import star.meshing.MesherBase;
import star.vis.Displayer;

/**
 * Low-level class for several different ways of getting Strings with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetStrings {

    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private final String _s = StaticDeclarations.UNIT_DIMENSIONLESS;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetStrings(MacroUtils m) {
        _mu = m;
    }

    /**
     * Gets the file base name (without the "." separator).
     *
     * @param filename given file name.
     * @return The file extension.
     */
    public String fileBasename(String filename) {
        return _getTokens(filename)[0];
    }

    /**
     * Gets the filename extension (without the "." separator).
     *
     * @param filename given file name.
     * @return The file extension.
     */
    public String fileExtension(String filename) {
        return _getTokens(filename)[1];
    }

    /**
     * Modifies a string in order to be used for filenames, i.e., eliminates special characters
     * (<i>= / #</i>, etc...). Spaces are replaced by underscores.
     *
     * @param base given base String.
     * @return modified String.
     */
    public String friendlyFilename(String base) {
        return base.replace(" ", "_").replace("=", "").replace("/", "").replace("#", "");
    }

    /**
     * Converts an array of objects to String.
     *
     * @param arrayObj given array of Objects.
     * @return The String.
     */
    public String fromArray(Object[] arrayObj) {
        return withinTheBrackets(Arrays.toString(arrayObj));
    }

    /**
     * Converts an array of double's to String.
     *
     * @param vals given array of constant values.
     * @return The String.
     */
    public String fromArray(double[] vals) {
        String strng = "" + vals[0];
        for (int i = 1; i < vals.length; i++) {
            strng += ", " + vals[i];
        }
        return strng;
    }

    /**
     * Unit to String.
     *
     * @param u given Units.
     * @return The String.
     */
    public String fromUnit(Units u) {
        if (u != null) {
            return u.toString();
        }
        return _s;
    }

    /**
     * Gets a more detailed information from a STAR-CCM+ object.
     *
     * @param cso given ClientServerObject.
     * @return A String.
     */
    public String information(ClientServerObject cso) {
        String thisCSO = _information(cso);
        if (cso instanceof Displayer) {
            Displayer d = (Displayer) cso;
            String sceneCSO = _information(d.getScene());
            return String.format("%s -> %s", sceneCSO, thisCSO);
        } else {
            return thisCSO;
        }
    }

    /**
     * Gets the current meshers selected in an Automated Mesh Operation.
     *
     * @param amo given AutoMeshOperation.
     * @param vo  given verbose option. False will not print anything.
     * @return An ArrayList of Strings.
     */
    public ArrayList<String> meshers(AutoMeshOperation amo, boolean vo) {
        _io.say.object(amo, vo);
        ArrayList<String> as = new ArrayList<>();
        for (MesherBase mb : amo.getMeshers().getObjects()) {
            as.add(mb.getClass().getName());
        }
        _io.say.msg(vo, "Current Meshers: %s.", withinTheBrackets(as.toString()));
        return as;
    }

    /**
     * Gets the necessary Meshers for an Automated Mesh Operation.
     *
     * @param meshers given meshers separated by comma. See
     *                {@link macroutils.StaticDeclarations.Meshers} for options.
     * @return An ArrayList of Strings. Useful with
     *         {@link macroutils.creator.CreateMeshOperation#automatedMesh}.
     */
    public ArrayList<String> meshers(StaticDeclarations.Meshers... meshers) {
        ArrayList<String> as = new ArrayList<>();
        for (StaticDeclarations.Meshers mesher : meshers) {
            as.add(mesher.getMesher());
        }
        return as;
    }

    /**
     * Gets the friendly presentation name from a STAR-CCM+ object.
     *
     * @param cso given ClientServerObject.
     * @return A String.
     */
    public String name(ClientServerObject cso) {
        if (cso == null) {
            return "NULL";
        }
        if (cso instanceof GeometryPart) {
            return ((GeometryPart) cso).getFullPathInHierarchy();
        } else if (cso instanceof ColumnDescriptor) {
            return ((ColumnDescriptor) cso).getColumnName();
        }
        return cso.getPresentationName();
    }

    /**
     * Gets the parent name from a STAR-CCM+ object.
     *
     * @param cso given ClientServerObject.
     * @return A String.
     */
    public String parentName(ClientServerObject cso) {
        if (cso == null) {
            return "NULL";
        }
        return _getSingular(cso.getParent().getBeanDisplayName());
    }

    /**
     * Gets a string repeated by a number of times.
     *
     * @param s given initial string.
     * @param n given number of times to be repeated.
     * @return The new string.
     */
    public String repeated(String s, int n) {
        return new String(new char[n]).replace("\0", s);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
    }

    /**
     * Gets whatever is within the brackets.
     *
     * @param s given string.
     * @return Whatever is within the brackets.
     */
    public String withinTheBrackets(String s) {
        java.util.regex.Pattern patt = java.util.regex.Pattern.compile(".*\\[(.*)\\]");
        java.util.regex.Matcher matcher = patt.matcher(s);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return macroutils.StaticDeclarations.NONE_STRING;
    }

    private String _getSingular(String name) {
        int lastChars = 3;
        if (name.length() < lastChars) {
            return name;  // There should not be such cases
        }
        String firstSlice = name.substring(0, name.length() - lastChars);
        String secondSlice = name.substring(name.length() - lastChars);
        if (secondSlice.endsWith("ies")) {
            secondSlice = secondSlice.replace("ies", "y");
        } else if (secondSlice.endsWith("s")) {
            secondSlice = secondSlice.replace("s", "");
        }
        return firstSlice + secondSlice;
    }

    private String[] _getTokens(String filename) {
        return filename.split("\\.(?=[^\\.]+$)");
    }

    private String _information(ClientServerObject cso) {
        return String.format("%s -> %s", parentName(cso), name(cso));
    }

}
