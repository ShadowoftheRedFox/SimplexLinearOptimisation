package optim.optim.src.simplex.data;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** A constraint set groups different {@link Constraint}. */
public class ConstraintSet implements SimplexData {
    /** Set of constraints. */
    private final Set<Constraint> Constraints = new LinkedHashSet<Constraint>();

    /**
     * Creates a set containing the given constraints.
     *
     * @param constraints Constraints.
     */
    public ConstraintSet(Constraint... constraints) {
        for (Constraint c : constraints) {
            Constraints.add(c);
        }
    }

    /**
     * Creates a set containing the given constraints.
     *
     * @param constraints Constraints.
     */
    public ConstraintSet(Collection<Constraint> constraints) {
        Constraints.addAll(constraints);
    }

    /**
     * Gets the set of linear constraints.
     *
     * @return the constraints.
     */
    public Collection<Constraint> getConstraints() {
        return Collections.unmodifiableSet(Constraints);
    }
}
