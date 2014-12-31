/**
 * JWave - Java implementation of wavelet transform algorithms
 *
 * Copyright 2008-2014 Christian Scheiblich
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *
 * This file is part of JWave.
 *
 * @author Christian Scheiblich (cscheiblich@gmail.com)
 * @date 23.05.2008 17:42:23
 *
 */
package math.jwave.transforms;

import java.util.Arrays;

import math.jwave.exceptions.JWaveException;
import math.jwave.exceptions.JWaveFailure;
import math.jwave.transforms.wavelets.Wavelet;

/**
 * Base class for the forward and reverse Fast Wavelet Transform in 1-D, 2-D,
 * and 3-D using a specified Wavelet by inheriting class.
 * 
 * @date 10.02.2010 08:10:42
 * @author Christian Scheiblich (cscheiblich@gmail.com)
 */
/**
 * @author Christian Scheiblich 05.02.2014 22:12:45
 */
public class FastWaveletTransform extends WaveletTransform {

  /**
   * Constructor receiving a Wavelet object.
   * 
   * @date 10.02.2010 08:10:42
   * @author Christian Scheiblich (cscheiblich@gmail.com)
   * @param wavelet
   *          object of type Wavelet; Haar1, Daubechies2, Coiflet1, ...
   */
  public FastWaveletTransform( Wavelet wavelet ) {
    super( wavelet );
  } // FastWaveletTransform

  /**
   * Performs the 1-D forward transform for arrays of dim N from time domain to
   * Hilbert domain for the given array using the Fast Wavelet Transform (FWT)
   * algorithm.
   * 
   * @date 10.02.2010 08:23:24
   * @author Christian Scheiblich (cscheiblich@gmail.com)
   * @throws JWaveException
   * @see math.jwave.transforms.BasicTransform#forward(double[])
   */
  @Override public double[ ] forward( double[ ] arrTime ) throws JWaveException {

    if( !_mathToolKit.isBinary( arrTime.length ) )
      throw new JWaveFailure(
          "given array length is not 2^p = 1, 2, 4, 8, 16, 32, .. "
              + "please use the Ancient Egyptian Decomposition for any other array length!" );

    double[ ] arrHilb = Arrays.copyOf( arrTime, arrTime.length );

    int h = arrHilb.length;
    int transformWavelength = _wavelet.getTransformWavelength( ); // 2, 4, 8, 16, 32, ...
    while( h >= transformWavelength ) {

      double[ ] arrTempPart = _wavelet.forward( arrHilb, h );
      System.arraycopy( arrTempPart, 0, arrHilb, 0, h );
      h = h >> 1;

    } // levels

    return arrHilb;

  } // forward

  /**
   * Performs the 1-D reverse transform for arrays of dim N from Hilbert domain
   * to time domain for the given array using the Fast Wavelet Transform (FWT)
   * algorithm and the selected wavelet.
   * 
   * @date 10.02.2010 08:23:24
   * @author Christian Scheiblich (cscheiblich@gmail.com)
   * @throws JWaveException
   * @see math.jwave.transforms.BasicTransform#reverse(double[])
   */
  @Override public double[ ] reverse( double[ ] arrHilb ) throws JWaveException {

    if( !_mathToolKit.isBinary( arrHilb.length ) )
      throw new JWaveFailure(
          "given array length is not 2^p = 1, 2, 4, 8, 16, 32, .. "
              + "please use the Ancient Egyptian Decomposition for any other array length!" );

    double[ ] arrTime = Arrays.copyOf( arrHilb, arrHilb.length );

    int transformWavelength = _wavelet.getTransformWavelength( ); // 2, 4, 8, 16, 32, ...
    int h = transformWavelength;
    while( h <= arrTime.length && h >= transformWavelength ) {

      double[ ] arrTempPart = _wavelet.reverse( arrTime, h );
      System.arraycopy( arrTempPart, 0, arrTime, 0, h );
      h = h << 1;

    } // levels

    return arrTime;

  } // reverse

  /**
   * Generates from a 1-D signal a 2-D output, where the second dimension are
   * the levels of the wavelet transform. The first level is keeping the
   * original coefficients. All following levels keep each step of the
   * decomposition of the Fast Wavelet Transform.
   * 
   * @author Christian Scheiblich (cscheiblich@gmail.com)
   * @date 17.08.2014 10:07:19
   * @param arrTime
   *          coefficients of time domain
   * @return matDeComp coefficients of frequency or Hilbert domain in 2-D
   *         spaces: [ 0 .. p ][ 0 .. N ] where p is the exponent of N=2^p
   * @throws JWaveException
   */
  @Override public double[ ][ ] decompose( double[ ] arrTime ) {

    int levels = _mathToolKit.getExponent( arrTime.length );
    double[ ] arrHilb = Arrays.copyOf( arrTime, arrTime.length );
    double[ ][ ] matDeComp = new double[ levels + 1 ][ arrTime.length ];
    for( int i = 0; i < arrTime.length; i++ )
      matDeComp[ 0 ][ i ] = arrTime[ i ];

    int l = 1; // start with level 1 cause level 0 is the normal space
    int h = arrHilb.length;
    int transformWavelength = _wavelet.getTransformWavelength( ); // 2, 4, 8, 16, 32, ...
    while( h >= transformWavelength ) {

      double[ ] arrTempPart = _wavelet.forward( arrHilb, h );
      System.arraycopy( arrTempPart, 0, arrHilb, 0, h );

      for( int i = 0; i < arrTime.length; i++ )
        if( i < h )
          matDeComp[ l ][ i ] = arrHilb[ i ];
        else
          matDeComp[ l ][ i ] = 0.;

      h = h >> 1;
      l++; // next level

    } // levels

    return matDeComp;

  } // decompose

  /**
   * Generates from a 1-D signal a 2-D output, where the second dimension are
   * the levels of the wavelet transform. The first level should keep the
   * original coefficients. All following levels should keep each step of the
   * decompostion of the Fast Wavelet Transform.
   * 
   * @author Christian Scheiblich (cscheiblich@gmail.com)
   * @date 17.08.2014 10:07:19
   * @param matDeComp
   *          2-D Hilbert spaces: [ 0 .. p ][ 0 .. N ] where p is the exponent
   *          of N=2^p
   * @return a 1-D time domain signal
   */
  public double[ ] recompose( double[ ][ ] matDeComp ) {

    int length = matDeComp[ 0 ].length; // length of first Hilbert space
    double[ ] arrTime = new double[ length ];

    int levels = matDeComp.length;
    for( int l = 1; l < levels; l++ ) {

      int steps = (int)_mathToolKit.scalb( (double)l, 1 );
      for( int i = 0; i < length; i++ )
        if( i < steps )
          arrTime[ i ] = matDeComp[ l ][ i ]; // add them together

    } // l

    int transformWavelength = _wavelet.getTransformWavelength( ); // 2, 4, 8, 16, 32, ...

    int h = transformWavelength;
    while( h <= arrTime.length && h >= transformWavelength ) {

      double[ ] arrTempPart = _wavelet.reverse( arrTime, h );
      System.arraycopy( arrTempPart, 0, arrTime, 0, h );
      h = h << 1;

    } // levels

    return arrTime;

  } // reverse

} // FastWaveletTransfromSplit
