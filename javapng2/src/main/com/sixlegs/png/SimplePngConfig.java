/*
com.sixlegs.image.png - Java package to read and display PNG images
Copyright (C) 1998-2005 Chris Nokleberg

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*/

package com.sixlegs.png;

public class SimplePngConfig
implements PngConfig
{
    private double displayExponent = 2.2;
    private double userExponent = 1.0;
    private boolean metadataOnly;
    private boolean warningsFatal;

    public SimplePngConfig()
    {
        this(false, false);
    }

    public SimplePngConfig(boolean metadataOnly, boolean warningsFatal)
    {
        this.metadataOnly = metadataOnly;
        this.warningsFatal = warningsFatal;
    }

    public double getDisplayExponent()
    {
        return displayExponent;
    }

    public void setDisplayExponent(double displayExponent)
    {
        this.displayExponent = displayExponent;
    }
    
    public double getUserExponent()
    {
        return userExponent;
    }

    public void setUserExponent(double userExponent)
    {
        this.userExponent = userExponent;
    }

    public void handleException(PngException e)
    throws PngException
    {
        if (warningsFatal || (e instanceof PngError))
            throw e;
    }

    public boolean getMetadataOnly()
    {
        return metadataOnly;
    }

    private static final PngChunk IHDR = new Chunk_IHDR();
    private static final PngChunk PLTE = new Chunk_PLTE();
    private static final PngChunk IDAT = new Chunk_IDAT();
    private static final PngChunk IEND = new Chunk_IEND();
    private static final PngChunk bKGD = new Chunk_bKGD();
    private static final PngChunk tRNS = new Chunk_tRNS();

    public PngChunk getChunk(int type)
    {
        switch (type) {
        case PngChunk.IHDR: return IHDR;
        case PngChunk.PLTE: return PLTE;
        case PngChunk.IDAT: return metadataOnly ? null : IDAT;
        case PngChunk.IEND: return IEND;
        case PngChunk.bKGD: return bKGD;
        case PngChunk.tRNS: return tRNS;
            /*
        case PngChunk.cHRM: return new Chunk_cHRM();
        case PngChunk.gAMA: return new Chunk_gAMA();
        case PngChunk.hIST: return new Chunk_hIST();
        case PngChunk.pHYs: return new Chunk_pHYs();
        case PngChunk.sBIT: return new Chunk_sBIT();
        case PngChunk.tEXt: return new Chunk_tEXt();
        case PngChunk.tIME: return new Chunk_tIME();
        case PngChunk.zTXt: return new Chunk_zTXt();
        case PngChunk.sRGB: return new Chunk_sRGB();
        case PngChunk.sPLT: return new Chunk_sPLT();
        case PngChunk.oFFs: return new Chunk_oFFs();
        case PngChunk.sCAL: return new Chunk_sCAL();
        case PngChunk.iCCP: return new Chunk_iCCP();
        case PngChunk.pCAL: return new Chunk_pCAL();
        case PngChunk.iTXt: return new Chunk_iTXt();
        case PngChunk.gIFg: return new Chunk_gIFg();
        case PngChunk.gIFx: return new Chunk_gIFx();
            */
        default:
            return null;
        }
    }
}
