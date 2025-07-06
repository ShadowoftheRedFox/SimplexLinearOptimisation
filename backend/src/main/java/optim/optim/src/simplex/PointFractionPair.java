package optim.optim.src.simplex;

/**
 ******************************* Modifications ********************************
 * Mofified version of "org.apache.commons.math3.optim.PointValuePair"
 * from Apache Software Foundation (ASF).
 * License found below, or here: http://www.apache.org/licenses/LICENSE-2.0
 *
 * Modifications are made use the Fraction class instead of double.
 ******************************************************************************
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.Serializable;

import org.apache.commons.math3.util.Pair;

/**
 * This class holds a point and the value of an objective function at
 * that point.
 *
 * @see org.apache.commons.math3.optim.PointValuePair
 * @see org.apache.commons.math3.analysis.MultivariateFunction
 */
public class PointFractionPair extends Pair<Fraction[], Fraction> implements Serializable {
    /**
     * Builds a point/objective function value pair.
     *
     * @param point Point coordinates. This instance will store
     *              a copy of the array, not the array passed as argument.
     * @param value Value of the objective function at the point.
     */
    public PointFractionPair(final Fraction[] point, final Fraction value) {
        this(point, value, true);
    }

    /**
     * Builds a point/objective function value pair.
     *
     * @param point     Point coordinates.
     * @param value     Value of the objective function at the point.
     * @param copyArray if {@code true}, the input array will be copied,
     *                  otherwise it will be referenced.
     */
    public PointFractionPair(final Fraction[] point, final Fraction value, final boolean copyArray) {
        super(copyArray ? ((point == null) ? null : point.clone()) : point, value);
    }

    /**
     * Gets the point.
     *
     * @return a copy of the stored point.
     */
    public Fraction[] getPoint() {
        final Fraction[] p = getKey();
        return p == null ? null : p.clone();
    }

    /**
     * Gets a reference to the point.
     *
     * @return a reference to the internal array storing the point.
     */
    public Fraction[] getPointRef() {
        return getKey();
    }
}
