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

import java.io.*;

class CountingInputStream
extends FilterInputStream
{
    private long count;
    
    public CountingInputStream(InputStream in)
    {
        super(in);
    }

    public void setCount(long count)
    {
        this.count = count;
    }

    public long getCount()
    {
        return count;
    }

    public int read()
    throws IOException
    {
        int result = in.read();
        if (result != -1)
            count++;
        return result;
    }
    
    public int read(byte[] b, int off, int len)
    throws IOException
    {
        int result = in.read(b, off, len);
        if (result > 0)
            count += result;
        return result;
    }

    public long skip(long n)
    throws IOException
    {
        long result = in.skip(n);
        count += result;
        return result;
    }
}