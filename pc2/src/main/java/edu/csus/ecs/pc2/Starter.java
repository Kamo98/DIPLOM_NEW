package edu.csus.ecs.pc2;

import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.InternalContest;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.Problem;

/**
 * Starter class.
 * 
 * The Starter class is the main driver for all PC<sup>2</sup> modules.
 * <P>
 * This class creates a contest data {@link edu.csus.ecs.pc2.core.model.IInternalContest}, then
 * a  controller {@link edu.csus.ecs.pc2.core.IInternalController}.   Then it passes the
 * command line arguments to {@link edu.csus.ecs.pc2.core.InternalController#start(String[])} and
 * that starts a Login Frame. 
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public final class Starter  {

    private Starter(){
        // constructor per checkstyle suggestion.
    }

    /**
     * Start a contest module.
     *
     * @param args
     */
    public static void main(String[] args) {
        IInternalContest model = new InternalContest();
        InternalController controller = new InternalController (model);
        controller.start(args);
    }
}
