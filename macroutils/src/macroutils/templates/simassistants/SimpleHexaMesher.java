package macroutils.templates.simassistants;

import macroutils.MacroUtils;
import macroutils.UserDeclarations;
import macroutils.creator.MainCreator;
import macroutils.getter.MainGetter;
import macroutils.io.MainIO;
import macroutils.misc.MainRemover;
import macroutils.misc.MainUpdater;
import macroutils.setter.MainSetter;
import macroutils.templates.TemplateMesh;
import star.common.Simulation;

/**
 * Low-level instructions for SimpleHexaMesher.
 *
 * @since June of 2018
 * @author Fabio Kasper
 */
public abstract class SimpleHexaMesher {

    protected final MainCreator _add;
    protected final MainGetter _get;
    protected final MainIO _io;
    protected final TemplateMesh _mesher;
    protected final MacroUtils _mu;
    protected final MainRemover _remove;
    protected final MainSetter _set;
    protected final Simulation _sim;
    protected final UserDeclarations _ud;
    protected final MainUpdater _upd;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public SimpleHexaMesher(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
        _ud = _mu.userDeclarations;
        _add = _mu.add;
        _get = _mu.get;
        _io = _mu.io;
        _set = _mu.set;
        _remove = _mu.remove;
        _mesher = _mu.templates.mesh;
        _upd = _mu.update;
    }

    /**
     * Removes all objects.
     */
    protected void removeAll() {
        _remove.all();
    }

}
