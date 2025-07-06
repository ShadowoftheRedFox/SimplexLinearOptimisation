package optim.optim.controller.body;

import optim.optim.src.simplex.data.IntegerMethod;
import optim.optim.src.simplex.data.PivotSelectionRule;

/** All advanced options fields can be null. If so, use the default value. */
public class AdvancedOptions {
    /** Default constructor. */
    public AdvancedOptions() {
    }

    /** Default maximum iterations limit {@code Integer.MAX_VALUE}. */
    public static final Integer maxIterationsDefault = Integer.MAX_VALUE;
    /** The Default pivot selection rule {@code DANTZIG}. */
    public static final PivotSelectionRule pivotSelectionRuleDefault = PivotSelectionRule.DANTZIG;
    /** The Default integer method: {@code NONE}. */
    public static final IntegerMethod integerMethodDefault = IntegerMethod.NONE;
    /** Maximum iterations limit. Must greater than 0. */
    public Integer maxIterations = maxIterationsDefault;
    /** The pivot selection rule. */
    public String pivotSelectionRule = pivotSelectionRuleDefault.name();
    /** The integer method. */
    public String integerMethod = integerMethodDefault.name();
}
