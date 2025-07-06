/*
 ********************Modifications********************
 * This is the implementation of MatrixFractions
 * with Fraction insteand of double.
 * Modifications are everywhere needed to change this
 * type.
 * ***************************************************
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

package optim.optim.src.simplex;

import java.io.Serializable;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.MatrixDimensionMismatchException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealMatrixPreservingVisitor;
import org.apache.commons.math3.util.MathUtils;

/**
 * Implementation of {@link RealMatrix} using a {@code Fraction[][]} array to
 * store entries.
 */
public class MatrixFractions extends AbstractRealMatrix implements Serializable {
    /** Serializable version identifier. */
    private static final long serialVersionUID = -1067294169172445528L;

    /** Entries of the matrix. */
    private Fraction data[][];

    /**
     * Creates a matrix with no data
     */
    public MatrixFractions() {
    }

    /**
     * Create a new RealMatrix with the supplied row and column dimensions.
     *
     * @param rowDimension    Number of rows in the new matrix.
     * @param columnDimension Number of columns in the new matrix.
     * @throws NotStrictlyPositiveException if the row or column dimension is
     *                                      not positive.
     */
    public MatrixFractions(final int rowDimension,
            final int columnDimension)
            throws NotStrictlyPositiveException {
        super(rowDimension, columnDimension);
        data = new Fraction[rowDimension][columnDimension];
    }

    /**
     * Create a new {@code RealMatrix} using the input array as the underlying
     * data array.
     * <p>
     * The input array is copied, not referenced. This constructor has
     * the same effect as calling {@link #MatrixFractions(Fraction[][], boolean)}
     * with the second argument set to {@code true}.
     * </p>
     *
     * @param d Data for the new matrix.
     * @throws DimensionMismatchException if {@code d} is not rectangular.
     * @throws NoDataException            if {@code d} row or column dimension is
     *                                    zero.
     * @throws NullArgumentException      if {@code d} is {@code null}.
     * @see #MatrixFractions(Fraction[][], boolean)
     */
    public MatrixFractions(final Fraction[][] d)
            throws DimensionMismatchException, NoDataException, NullArgumentException {
        copyIn(d);
    }

    /**
     * Create a new RealMatrix using the input array as the underlying
     * data array.
     * If an array is built specially in order to be embedded in a
     * RealMatrix and not used directly, the {@code copyArray} may be
     * set to {@code false}. This will prevent the copying and improve
     * performance as no new array will be built and no data will be copied.
     *
     * @param d         Data for new matrix.
     * @param copyArray if {@code true}, the input array will be copied,
     *                  otherwise it will be referenced.
     * @throws DimensionMismatchException if {@code d} is not rectangular.
     * @throws NoDataException            if {@code d} row or column dimension is
     *                                    zero.
     * @throws NullArgumentException      if {@code d} is {@code null}.
     * @see #MatrixFractions(Fraction[][])
     */
    public MatrixFractions(final Fraction[][] d, final boolean copyArray)
            throws DimensionMismatchException, NoDataException,
            NullArgumentException {
        if (copyArray) {
            copyIn(d);
        } else {
            if (d == null) {
                throw new NullArgumentException();
            }
            final int nRows = d.length;
            if (nRows == 0) {
                throw new NoDataException(LocalizedFormats.AT_LEAST_ONE_ROW);
            }
            final int nCols = d[0].length;
            if (nCols == 0) {
                throw new NoDataException(LocalizedFormats.AT_LEAST_ONE_COLUMN);
            }
            for (int r = 1; r < nRows; r++) {
                if (d[r].length != nCols) {
                    throw new DimensionMismatchException(d[r].length, nCols);
                }
            }
            data = d;
        }
    }

    /**
     * Create a new (column) RealMatrix using {@code v} as the
     * data for the unique column of the created matrix.
     * The input array is copied.
     *
     * @param v Column vector holding data for new matrix.
     */
    public MatrixFractions(final Fraction[] v) {
        final int nRows = v.length;
        data = new Fraction[nRows][1];
        for (int row = 0; row < nRows; row++) {
            data[row][0] = v[row];
        }
    }

    /** {@inheritDoc} */
    @Override
    public RealMatrix createMatrix(final int rowDimension,
            final int columnDimension)
            throws NotStrictlyPositiveException {
        return new MatrixFractions(rowDimension, columnDimension);
    }

    /** {@inheritDoc} */
    @Override
    public RealMatrix copy() {
        return new MatrixFractions(copyOutFraction(), false);
    }

    /**
     * Compute the sum of {@code this} and {@code m}.
     *
     * @param m Matrix to be added.
     * @return {@code this + m}.
     * @throws MatrixDimensionMismatchException if {@code m} is not the same
     *                                          size as {@code this}.
     */
    public MatrixFractions add(final MatrixFractions m)
            throws MatrixDimensionMismatchException {
        // Safety check.
        MatrixUtils.checkAdditionCompatible(this, m);

        final int rowCount = getRowDimension();
        final int columnCount = getColumnDimension();
        final Fraction[][] outData = new Fraction[rowCount][columnCount];
        for (int row = 0; row < rowCount; row++) {
            final Fraction[] dataRow = data[row];
            final Fraction[] mRow = m.data[row];
            final Fraction[] outDataRow = outData[row];
            for (int col = 0; col < columnCount; col++) {
                outDataRow[col] = dataRow[col].add(mRow[col]);
            }
        }

        return new MatrixFractions(outData, false);
    }

    /**
     * Returns {@code this} minus {@code m}.
     *
     * @param m Matrix to be subtracted.
     * @return {@code this - m}
     * @throws MatrixDimensionMismatchException if {@code m} is not the same
     *                                          size as {@code this}.
     */
    public MatrixFractions subtract(final MatrixFractions m)
            throws MatrixDimensionMismatchException {
        MatrixUtils.checkSubtractionCompatible(this, m);

        final int rowCount = getRowDimension();
        final int columnCount = getColumnDimension();
        final Fraction[][] outData = new Fraction[rowCount][columnCount];
        for (int row = 0; row < rowCount; row++) {
            final Fraction[] dataRow = data[row];
            final Fraction[] mRow = m.data[row];
            final Fraction[] outDataRow = outData[row];
            for (int col = 0; col < columnCount; col++) {
                outDataRow[col] = dataRow[col].subtract(mRow[col]);
            }
        }

        return new MatrixFractions(outData, false);
    }

    /**
     * Returns the result of postmultiplying {@code this} by {@code m}.
     *
     * @param m matrix to postmultiply by
     * @return {@code this * m}
     * @throws DimensionMismatchException if
     *                                    {@code columnDimension(this) != rowDimension(m)}
     */
    public MatrixFractions multiply(final MatrixFractions m)
            throws DimensionMismatchException {
        MatrixUtils.checkMultiplicationCompatible(this, m);

        final int nRows = this.getRowDimension();
        final int nCols = m.getColumnDimension();
        final int nSum = this.getColumnDimension();

        final Fraction[][] outData = new Fraction[nRows][nCols];
        // Will hold a column of "m".
        final Fraction[] mCol = new Fraction[nSum];
        final Fraction[][] mData = m.data;

        // Multiply.
        for (int col = 0; col < nCols; col++) {
            // Copy all elements of column "col" of "m" so that
            // will be in contiguous memory.
            for (int mRow = 0; mRow < nSum; mRow++) {
                mCol[mRow] = mData[mRow][col];
            }

            for (int row = 0; row < nRows; row++) {
                final Fraction[] dataRow = data[row];
                Fraction sum = Fraction.ZERO;
                for (int i = 0; i < nSum; i++) {
                    sum = sum.add(dataRow[i].multiply(mCol[i]));
                }
                outData[row][col] = sum;
            }
        }

        return new MatrixFractions(outData, false);
    }

    /** {@inheritDoc} */
    @Override
    public double[][] getData() {
        return copyOut();
    }

    /**
     * Returns matrix entries as a two-dimensional array.
     *
     * @return 2-dimensional array of entries.
     */
    public Fraction[][] getDataFraction() {
        return copyOutFraction();
    }

    /**
     * Get a reference to the underlying data array.
     *
     * @return 2-dimensional array of entries.
     */
    public Fraction[][] getDataRef() {
        return data;
    }

    /**
     * Replace the submatrix starting at {@code row, column} using data in the
     * input {@code subMatrix} array. Indexes are 0-based.
     * <p>
     * Example:<br>
     * Starting with
     *
     * <pre>
     * 1  2  3  4
     * 5  6  7  8
     * 9  0  1  2
     * </pre>
     *
     * and <code>subMatrix = {{3, 4} {5,6}}</code>, invoking
     * {@code setSubMatrix(subMatrix,1,1))} will result in
     *
     * <pre>
     * 1  2  3  4
     * 5  3  4  8
     * 9  5  6  2
     * </pre>
     *
     * @param subMatrix array containing the submatrix replacement data
     * @param row       row coordinate of the top, left element to be replaced
     * @param column    column coordinate of the top, left element to be replaced
     * @throws NoDataException            if {@code subMatrix} is empty.
     * @throws OutOfRangeException        if {@code subMatrix} does not fit into
     *                                    this matrix from element in
     *                                    {@code (row, column)}.
     * @throws DimensionMismatchException if {@code subMatrix} is not rectangular
     *                                    (not all rows have the same length) or
     *                                    empty.
     * @throws NullArgumentException      if {@code subMatrix} is {@code null}.
     */
    public void setSubMatrix(final Fraction[][] subMatrix, final int row,
            final int column)
            throws NoDataException, OutOfRangeException,
            DimensionMismatchException, NullArgumentException {
        if (data == null) {
            if (row > 0) {
                throw new MathIllegalStateException(LocalizedFormats.FIRST_ROWS_NOT_INITIALIZED_YET, row);
            }
            if (column > 0) {
                throw new MathIllegalStateException(LocalizedFormats.FIRST_COLUMNS_NOT_INITIALIZED_YET, column);
            }
            MathUtils.checkNotNull(subMatrix);
            final int nRows = subMatrix.length;
            if (nRows == 0) {
                throw new NoDataException(LocalizedFormats.AT_LEAST_ONE_ROW);
            }

            final int nCols = subMatrix[0].length;
            if (nCols == 0) {
                throw new NoDataException(LocalizedFormats.AT_LEAST_ONE_COLUMN);
            }
            data = new Fraction[subMatrix.length][nCols];
            for (int i = 0; i < data.length; ++i) {
                if (subMatrix[i].length != nCols) {
                    throw new DimensionMismatchException(subMatrix[i].length, nCols);
                }
                System.arraycopy(subMatrix[i], 0, data[i + row], column, nCols);
            }
        } else if (subMatrix.length > 0) {
            // transform subMatrix in double[][]
            final double[][] subMatrixDouble = new double[subMatrix.length][subMatrix[0].length];
            for (int i = 0; i < subMatrix.length; i++) {
                for (int j = 0; j < subMatrix[i].length; j++) {
                    subMatrixDouble[i][j] = subMatrix[i][j].doubleValue();
                }
            }
            super.setSubMatrix(subMatrixDouble, row, column);
        }

    }

    /** {@inheritDoc} */
    @Override
    public double getEntry(final int row, final int column)
            throws OutOfRangeException {
        MatrixUtils.checkMatrixIndex(this, row, column);
        return data[row][column].doubleValue();
    }

    /**
     * Get the entry in the specified row and column. Row and column indices
     * start at 0.
     *
     * @param row    Row index of entry to be fetched.
     * @param column Column index of entry to be fetched.
     * @return the matrix entry at {@code (row, column)}.
     * @throws OutOfRangeException if the row or column index is not valid.
     */
    public Fraction getEntryFraction(final int row, final int column)
            throws OutOfRangeException {
        MatrixUtils.checkMatrixIndex(this, row, column);
        return (data[row][column]);
    }

    /** {@inheritDoc} */
    @Override
    public void setEntry(final int row, final int column, final double value)
            throws OutOfRangeException {
        MatrixUtils.checkMatrixIndex(this, row, column);
        data[row][column] = new Fraction(value);
    }

    /**
     * Adds (in place) the specified value to the specified entry of
     * {@code this} matrix. Row and column indices start at 0.
     *
     * @param row    Row index of the entry to be modified.
     * @param column Column index of the entry to be modified.
     * @param value  value to add to the matrix entry.
     * @throws OutOfRangeException if the row or column index is not valid.
     */
    public void setEntryFraction(final int row, final int column, final Fraction value)
            throws OutOfRangeException {
        MatrixUtils.checkMatrixIndex(this, row, column);
        data[row][column] = value;
    }

    /** {@inheritDoc} */
    @Override
    public void addToEntry(final int row, final int column,
            final double increment)
            throws OutOfRangeException {
        MatrixUtils.checkMatrixIndex(this, row, column);
        data[row][column] = data[row][column].add(new Fraction(increment));
    }

    /**
     * Adds (in place) the specified value to the specified entry of
     * {@code this} matrix. Row and column indices start at 0.
     *
     * @param row       Row index of the entry to be modified.
     * @param column    Column index of the entry to be modified.
     * @param increment value to add to the matrix entry.
     * @throws OutOfRangeException if the row or column index is not valid.
     */
    public void addToEntry(final int row, final int column,
            final Fraction increment)
            throws OutOfRangeException {
        MatrixUtils.checkMatrixIndex(this, row, column);
        data[row][column] = data[row][column].add(increment);
    }

    /** {@inheritDoc} */
    @Override
    public void multiplyEntry(final int row, final int column,
            final double factor)
            throws OutOfRangeException {
        MatrixUtils.checkMatrixIndex(this, row, column);
        data[row][column] = data[row][column].multiply(new Fraction(factor));
    }

    /**
     * Multiplies (in place) the specified entry of {@code this} matrix by the
     * specified value. Row and column indices start at 0.
     *
     * @param row    Row index of the entry to be modified.
     * @param column Column index of the entry to be modified.
     * @param factor Multiplication factor for the matrix entry.
     * @throws OutOfRangeException if the row or column index is not valid.
     */
    public void multiplyEntryFraction(final int row, final int column,
            final Fraction factor)
            throws OutOfRangeException {
        MatrixUtils.checkMatrixIndex(this, row, column);
        data[row][column] = data[row][column].multiply(factor);
    }

    /** {@inheritDoc} */
    @Override
    public int getRowDimension() {
        return (data == null) ? 0 : data.length;
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnDimension() {
        return ((data == null) || (data[0] == null)) ? 0 : data[0].length;
    }

    /** {@inheritDoc} */
    @Override
    public double[] operate(final double[] v)
            throws DimensionMismatchException {
        final int nRows = this.getRowDimension();
        final int nCols = this.getColumnDimension();
        if (v.length != nCols) {
            throw new DimensionMismatchException(v.length, nCols);
        }
        final double[] out = new double[nRows];
        for (int row = 0; row < nRows; row++) {
            final Fraction[] dataRow = data[row];
            double sum = 0;
            for (int i = 0; i < nCols; i++) {
                sum += dataRow[i].multiply(new Fraction(v[i])).doubleValue();
            }
            out[row] = sum;
        }
        return out;
    }

    /**
     * Returns the result of multiplying this by the vector {@code v}.
     *
     * @param v the vector to operate on
     * @return {@code this * v}
     * @throws DimensionMismatchException if the length of {@code v} does not
     *                                    match the column dimension of
     *                                    {@code this}.
     */
    public Fraction[] operateFraction(final Fraction[] v)
            throws DimensionMismatchException {
        final int nRows = this.getRowDimension();
        final int nCols = this.getColumnDimension();
        if (v.length != nCols) {
            throw new DimensionMismatchException(v.length, nCols);
        }
        final Fraction[] out = new Fraction[nRows];
        for (int row = 0; row < nRows; row++) {
            final Fraction[] dataRow = data[row];
            Fraction sum = Fraction.ZERO;
            for (int i = 0; i < nCols; i++) {
                sum = sum.add(dataRow[i].multiply(v[i]));
            }
            out[row] = sum;
        }
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public double[] preMultiply(final double[] v)
            throws DimensionMismatchException {
        final int nRows = getRowDimension();
        final int nCols = getColumnDimension();
        if (v.length != nRows) {
            throw new DimensionMismatchException(v.length, nRows);
        }

        final double[] out = new double[nCols];
        for (int col = 0; col < nCols; ++col) {
            double sum = 0;
            for (int i = 0; i < nRows; ++i) {
                sum += data[i][col].multiply(new Fraction(v[i])).doubleValue();
            }
            out[col] = sum;
        }

        return out;
    }

    /**
     * Returns the (row) vector result of premultiplying this by the vector
     * {@code v}.
     *
     * @param v the row vector to premultiply by
     * @return {@code v * this}
     * @throws DimensionMismatchException if the length of {@code v} does not
     *                                    match the row dimension of {@code this}.
     */
    public Fraction[] preMultiplyFraction(final Fraction[] v)
            throws DimensionMismatchException {
        final int nRows = getRowDimension();
        final int nCols = getColumnDimension();
        if (v.length != nRows) {
            throw new DimensionMismatchException(v.length, nRows);
        }

        final Fraction[] out = new Fraction[nCols];
        for (int col = 0; col < nCols; ++col) {
            Fraction sum = Fraction.ZERO;
            for (int i = 0; i < nRows; ++i) {
                sum = sum.add(data[i][col].multiply(v[i]));
            }
            out[col] = sum;
        }

        return out;
    }

    /** {@inheritDoc} */
    @Override
    public double walkInRowOrder(final RealMatrixChangingVisitor visitor) {
        final int rows = getRowDimension();
        final int columns = getColumnDimension();
        visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
        for (int i = 0; i < rows; ++i) {
            final Fraction[] rowI = data[i];
            for (int j = 0; j < columns; ++j) {
                rowI[j] = new Fraction(visitor.visit(i, j, rowI[j].doubleValue()));
            }
        }
        return visitor.end();
    }

    /** {@inheritDoc} */
    @Override
    public double walkInRowOrder(final RealMatrixPreservingVisitor visitor) {
        final int rows = getRowDimension();
        final int columns = getColumnDimension();
        visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
        for (int i = 0; i < rows; ++i) {
            final Fraction[] rowI = data[i];
            for (int j = 0; j < columns; ++j) {
                visitor.visit(i, j, rowI[j].doubleValue());
            }
        }
        return visitor.end();
    }

    /** {@inheritDoc} */
    @Override
    public double walkInRowOrder(final RealMatrixChangingVisitor visitor,
            final int startRow, final int endRow,
            final int startColumn, final int endColumn)
            throws OutOfRangeException, NumberIsTooSmallException {
        MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
        visitor.start(getRowDimension(), getColumnDimension(),
                startRow, endRow, startColumn, endColumn);
        for (int i = startRow; i <= endRow; ++i) {
            final Fraction[] rowI = data[i];
            for (int j = startColumn; j <= endColumn; ++j) {
                rowI[j] = new Fraction(visitor.visit(i, j, rowI[j].doubleValue()));
            }
        }
        return visitor.end();
    }

    /** {@inheritDoc} */
    @Override
    public double walkInRowOrder(final RealMatrixPreservingVisitor visitor,
            final int startRow, final int endRow,
            final int startColumn, final int endColumn)
            throws OutOfRangeException, NumberIsTooSmallException {
        MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
        visitor.start(getRowDimension(), getColumnDimension(),
                startRow, endRow, startColumn, endColumn);
        for (int i = startRow; i <= endRow; ++i) {
            final Fraction[] rowI = data[i];
            for (int j = startColumn; j <= endColumn; ++j) {
                visitor.visit(i, j, rowI[j].doubleValue());
            }
        }
        return visitor.end();
    }

    /** {@inheritDoc} */
    @Override
    public double walkInColumnOrder(final RealMatrixChangingVisitor visitor) {
        final int rows = getRowDimension();
        final int columns = getColumnDimension();
        visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
        for (int j = 0; j < columns; ++j) {
            for (int i = 0; i < rows; ++i) {
                final Fraction[] rowI = data[i];
                rowI[j] = new Fraction(visitor.visit(i, j, rowI[j].doubleValue()));
            }
        }
        return visitor.end();
    }

    /** {@inheritDoc} */
    @Override
    public double walkInColumnOrder(final RealMatrixPreservingVisitor visitor) {
        final int rows = getRowDimension();
        final int columns = getColumnDimension();
        visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
        for (int j = 0; j < columns; ++j) {
            for (int i = 0; i < rows; ++i) {
                visitor.visit(i, j, data[i][j].doubleValue());
            }
        }
        return visitor.end();
    }

    /** {@inheritDoc} */
    @Override
    public double walkInColumnOrder(final RealMatrixChangingVisitor visitor,
            final int startRow, final int endRow,
            final int startColumn, final int endColumn)
            throws OutOfRangeException, NumberIsTooSmallException {
        MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
        visitor.start(getRowDimension(), getColumnDimension(),
                startRow, endRow, startColumn, endColumn);
        for (int j = startColumn; j <= endColumn; ++j) {
            for (int i = startRow; i <= endRow; ++i) {
                final Fraction[] rowI = data[i];
                rowI[j] = new Fraction(visitor.visit(i, j, rowI[j].doubleValue()));
            }
        }
        return visitor.end();
    }

    /** {@inheritDoc} */
    @Override
    public double walkInColumnOrder(final RealMatrixPreservingVisitor visitor,
            final int startRow, final int endRow,
            final int startColumn, final int endColumn)
            throws OutOfRangeException, NumberIsTooSmallException {
        MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
        visitor.start(getRowDimension(), getColumnDimension(),
                startRow, endRow, startColumn, endColumn);
        for (int j = startColumn; j <= endColumn; ++j) {
            for (int i = startRow; i <= endRow; ++i) {
                visitor.visit(i, j, data[i][j].doubleValue());
            }
        }
        return visitor.end();
    }

    /**
     * Get a fresh copy of the underlying data array.
     *
     * @return a copy of the underlying data array.
     */
    private double[][] copyOut() {
        final int nRows = this.getRowDimension();
        final double[][] out = new double[nRows][this.getColumnDimension()];
        // can't copy 2-d array in one shot, otherwise get row references
        for (int i = 0; i < nRows; i++) {
            System.arraycopy(data[i], 0, out[i], 0, data[i].length);
        }
        return out;
    }

    /**
     * Get a fresh copy of the underlying data array.
     *
     * @return a copy of the underlying data array.
     */
    private Fraction[][] copyOutFraction() {
        final int nRows = this.getRowDimension();
        final Fraction[][] out = new Fraction[nRows][this.getColumnDimension()];
        // can't copy 2-d array in one shot, otherwise get row references
        for (int i = 0; i < nRows; i++) {
            System.arraycopy(data[i], 0, out[i], 0, data[i].length);
        }
        return out;
    }

    /**
     * Replace data with a fresh copy of the input array.
     *
     * @param in Data to copy.
     * @throws NoDataException            if the input array is empty.
     * @throws DimensionMismatchException if the input array is not rectangular.
     * @throws NullArgumentException      if the input array is {@code null}.
     */
    private void copyIn(final Fraction[][] in)
            throws DimensionMismatchException, NoDataException, NullArgumentException {
        setSubMatrix(in, 0, 0);
    }
}
