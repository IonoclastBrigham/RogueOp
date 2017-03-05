// XYZf.java
// Three dimensional point/vector class
//
// Copyright © 2010-2017 Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
//	80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode.geometrics;



/**
 *
 * @author Brigham Toskin
 */
public class XYZf extends XYf
{
	private static final long serialVersionUID = 4186038949433460479L;

	// cartesian z or vector component k
	public float z;

	// c'tors //

	public XYZf()
	{
		this(0.0f, 0.0f, 0.0f);
	}

	public XYZf(float pX, float pY, float pZ)
	{
		this.x = pX;
		this.y = pY;
		this.z = pZ;
	}

	public XYZf(XYZf pSource)
	{
		this(pSource.x, pSource.y, pSource.z);
	}

	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ", " + z + ")";
	}

	public void set(XYZf pSource)
	{
		x = pSource.x;
		y = pSource.y;
		z = pSource.z;
	}

	// arithmetic operators ////////////////////////////////////////////////////

	public XYZf plus(XYZf pOther)
	{
		return new XYZf(pOther.x + x, pOther.y + y, pOther.z + z);
	}

	public XYZf minus(XYZf pOther)
	{
		return new XYZf(x - pOther.x, y - pOther.y, z - pOther.z);
	}

	public XYZf add(XYZf pOther)
	{
		x += pOther.x;
		y += pOther.y;
		z += pOther.z;
		return this;
	}

	public XYZf sub(XYZf pOther)
	{
		x -= pOther.x;
		y -= pOther.y;
		z -= pOther.z;
		return this;
	}

	@Override
	public XYZf times(float pScalar)
	{
		return new XYZf(x * pScalar, y * pScalar, z * pScalar);
	}

	@Override
	public XYZf dividedBy(float pScalar)
	{
		return new XYZf(x / pScalar, y / pScalar, z / pScalar);
	}

	@Override
	public XYZf mul(float pScalar)
	{
		x *= pScalar;
		y *= pScalar;
		z *= pScalar;
		return this;
	}

	@Override
	public XYZf div(float pScalar)
	{
		x /= pScalar;
		y /= pScalar;
		z /= pScalar;
		return this;
	}

	// linear algebra operators ////////////////////////////////////////////////

	// TODO: cross-product

	public float Dot(XYZf pOther)
	{
		return x * pOther.x + y * pOther.y + z * pOther.z;
	}

	@Override
	public float Magnitude()
	{
		return (float)Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public XYZf Normalize()
	{
		float tMag = Magnitude();
		x /= tMag;
		y /= tMag;
		z /= tMag;
		return this;
	}
}
