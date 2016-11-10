package macroutils.getter;

import java.util.*;
import macroutils.*;
import star.base.neo.*;
import star.common.*;
import star.meshing.*;

/**
 * Low-level class for several different ways of getting Strings with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetStrings {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetStrings(MacroUtils m) {
        _mu = m;
    }

    private String[] _getTokens(String filename) {
        return filename.split("\\.(?=[^\\.]+$)");
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
     * Modifies a string in order to be used for filenames, i.e., eliminates special characters (<i>= / #</i>, etc...).
     * Spaces are replaced by underscores.
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
     * Gets the current meshers selected in an Automated Mesh Operation.
     *
     * @param amo given AutoMeshOperation.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Strings.
     */
    public ArrayList<String> meshers(AutoMeshOperation amo, boolean vo) {
        _io.say.object(amo, vo);
        ArrayList<String> as = new ArrayList();
        for (MesherBase mb : amo.getMeshers().getObjects()) {
            as.add(mb.getClass().getName());
        }
        _io.say.msg(vo, "Current Meshers: %s.", withinTheBrackets(as.toString()));
        return as;
    }

    /**
     * Gets the necessary Meshers for an Automated Mesh Operation.
     *
     * @param meshers given meshers separated by comma. See {@link macroutils.StaticDeclarations.Meshers} for options.
     * @return An ArrayList of Strings. Useful with {@link macroutils.creator.CreateMeshOperation#automatedMesh}.
     */
    public ArrayList<String> meshers(StaticDeclarations.Meshers... meshers) {
        ArrayList<String> as = new ArrayList();
        for (StaticDeclarations.Meshers mesher : meshers) {
            as.add(mesher.getMesher());
        }
        return as;
    }

    /**
     * Gets the parent name from a STAR-CCM+ object.
     *
     * @param cso given Client Server Object.
     * @return The String.
     */
    public String parentName(ClientServerObject cso) {
        String s = cso.getParent().getBeanDisplayName();
        if (s.endsWith("ies")) {
            return s.replace("ies", "y");
        } else if (s.endsWith("s")) {
            return s.replace("s", "");
        }
        return s;
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

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
    }

    //--
    //-- Variables declaration area.
    //--
    private final String _s = StaticDeclarations.UNIT_DIMENSIONLESS;

    private MacroUtils _mu = null;
    private macroutils.io.MainIO _io = null;

}
