import java.io.*;
import java.util.*;
import java.util.zip.*;

/*
need corrupted images:
  Multiple chunks are not allowed (all except sPLT, iTXt, tEXt, zTXt, IDAT)
x   IHDR
x   PLTE
x   gAMA
    ...
  Unrecognized filter type (Defilterer)
  Unrecognized compression method (iCCP, zTXt, iTXt)
  Invalid keyword length
  Meaningless zero gAMA chunk value
  Illegal pHYs chunk unit specifier
  Illegal sBIT sample depth
  ...
need iTXt
*/  
public class BrokenGenerator
{
    private static final int IHDR = 0x49484452;
    private static final int PLTE = 0x504c5445;
    private static final int IDAT = 0x49444154;
    private static final int IEND = 0x49454e44;
    private static final int bKGD = 0x624b4744;
    private static final int cHRM = 0x6348524d;
    private static final int gAMA = 0x67414d41;
    private static final int hIST = 0x68495354;
    private static final int iCCP = 0x69434350;
    private static final int iTXt = 0x69545874;
    private static final int pHYs = 0x70485973;
    private static final int sBIT = 0x73424954;
    private static final int sPLT = 0x73504c54;
    private static final int sRGB = 0x73524742;
    private static final int tEXt = 0x74455874;
    private static final int tIME = 0x74494d45;
    private static final int tRNS = 0x74524e53;
    private static final int zTXt = 0x7a545874;
    private static final int oFFs = 0x6f464673;
    private static final int pCAL = 0x7043414c;
    private static final int sCAL = 0x7343414c;
    private static final int gIFg = 0x67494667;
    private static final int gIFx = 0x67494678;
    private static final int sTER = 0x73544552;

    // private types
    private static final int heRB = 0x68655242;

    // bad types
    private static final int GaMA = 0x47614d41;
    private static final int gAM_ = 0x67614d5f;

    public static void main(String[] args)
    throws Exception
    {
        gen("suite/basn3p08.png", "broken/missing_ihdr.png", swap(IHDR, gAMA));
        gen("suite/basn3p08.png", "broken/missing_idat.png", remove(find(IDAT)));
        gen("suite/basn3p08.png", "broken/missing_plte.png", remove(find(PLTE)));
        gen("suite/tbbn3p08.png", "broken/missing_plte_2.png", remove(find(PLTE)));

        gen("suite/basn3p08.png", "broken/chunk_private_critical.png", setType(find(gAMA), GaMA));
        gen("suite/basn3p08.png", "broken/chunk_type.png", setType(find(gAMA), gAM_));
        gen("suite/basn3p08.png", "broken/chunk_crc.png", setCRC(find(IHDR), 0x12345678));
        gen("suite/basn3p08.png", "broken/chunk_length.png", setLength(find(gAMA), -20));

        gen("suite/basn3p08.png", "broken/nonconsecutive_idat.png",
            addAfter(find(IDAT), custom(heRB, new byte[0])),
            addAfter(find(heRB), find(IDAT)));

        gen("suite/basn3p08.png", "broken/multiple_gama.png", duplicate(gAMA));
        gen("suite/basn3p08.png", "broken/multiple_plte.png", duplicate(PLTE));
        gen("suite/basn3p08.png", "broken/multiple_ihdr.png", duplicate(IHDR));

        gen("suite/ch1n3p04.png", "broken/hist_before_plte.png", swap(hIST, PLTE));
        gen("suite/ccwn3p08.png", "broken/chrm_after_plte.png", swap(cHRM, PLTE));
        gen("suite/basn3p02.png", "broken/sbit_after_plte.png", swap(sBIT, PLTE));
        gen("suite/basn3p08.png", "broken/gama_after_plte.png", swap(gAMA, PLTE));
        gen("misc/srgb-cc99ff.png", "broken/srgb_after_plte.png", swap(sRGB, PLTE));
        gen("misc/iccp-cc99ff.png", "broken/iccp_after_plte.png", swap(iCCP, PLTE));

        gen("suite/basn3p08.png", "broken/plte_after_idat.png", swap(PLTE, IDAT));
        gen("suite/basn0g01.png", "broken/gama_after_idat.png", swap(gAMA, IDAT));
        gen("suite/basn3p08.png", "broken/ster_after_idat.png",
            addAfter(find(IDAT), custom(sTER, new byte[]{ 0 })));
        gen("suite/ccwn2c08.png", "broken/chrm_after_idat.png", swap(cHRM, IDAT));
        gen("suite/cs5n2c08.png", "broken/sbit_after_idat.png", swap(sBIT, IDAT));
        gen("suite/bgbn4a08.png", "broken/bkgd_after_idat.png", swap(bKGD, IDAT));
        gen("suite/ch1n3p04.png", "broken/hist_after_idat.png", swap(hIST, IDAT));
        gen("suite/tbbn1g04.png", "broken/trns_after_idat.png", swap(tRNS, IDAT));
        gen("suite/cdun2c08.png", "broken/phys_after_idat.png", swap(pHYs, IDAT));
        gen("misc/ps2n2c16.png", "broken/splt_after_idat.png", swap(sPLT, IDAT));

        gen("misc/pngtest.png", "broken/offs_after_idat.png", swap(oFFs, IDAT));
        gen("misc/pngtest.png", "broken/pcal_after_idat.png", swap(pCAL, IDAT));
        gen("misc/pngtest.png", "broken/scal_after_idat.png", swap(sCAL, IDAT));

        Chunk iccp = extract("misc/ntsciccp.png", iCCP);
        Chunk srgb = extract("misc/srgbsrgb.png", sRGB);
        gen("suite/basn2c08.png", "broken/iccp_after_idat.png",
            remove(find(gAMA)), addAfter(find(IDAT), custom(iccp)));
        gen("suite/basn2c08.png", "broken/srgb_after_idat.png",
            remove(find(gAMA)), addAfter(find(IDAT), custom(srgb)));

        gen("suite/basn3p08.png", "broken/ihdr_image_size.png",
            replaceHeader(-32, -32, 8, 3, 0, 0, 0));
        gen("suite/basn3p08.png", "broken/ihdr_bit_depth.png",
            replaceHeader(32, -32, 7, 3, 0, 0, 0));
        gen("suite/basn3p08.png", "broken/ihdr_16bit_palette.png",
            replaceHeader(32, 32, 16, 3, 0, 0, 0));
        gen("suite/basn6a08.png", "broken/ihdr_1bit_alpha.png",
            replaceHeader(32, 32, 1, 6, 1, 0, 0));
        gen("suite/basn3p08.png", "broken/ihdr_compression_method.png",
            replaceHeader(32, 32, 8, 3, 1, 0, 0));
        gen("suite/basn3p08.png", "broken/ihdr_filter_method.png",
            replaceHeader(32, 32, 8, 3, 0, 1, 0));
        gen("suite/basn3p08.png", "broken/ihdr_interlace_method.png",
            replaceHeader(32, 32, 8, 3, 0, 0, 2));

        gen("suite/basn3p08.png", "broken/plte_length_mod_three.png",
            replace(find(PLTE), custom(new Chunk(PLTE, new byte[2]))));
        gen("suite/basn3p08.png", "broken/plte_empty.png",
            replace(find(PLTE), custom(new Chunk(PLTE, new byte[0]))));
        gen("suite/basn3p04.png", "broken/plte_too_many_entries.png",
            replace(find(PLTE), custom(new Chunk(PLTE, new byte[17 * 3]))));
        gen("suite/basn2c08.png", "broken/plte_too_many_entries_2.png",
            addAfter(find(gAMA), custom(new Chunk(PLTE, new byte[257 * 3]))));
        gen("suite/basn0g08.png", "broken/plte_in_grayscale.png",
            addAfter(find(gAMA), custom(new Chunk(PLTE, new byte[3]))));

        Chunk trans = extract("suite/tbbn1g04.png", tRNS);
        gen("suite/basn2c08.png", "broken/trns_bad_color_type.png",
            addAfter(find(gAMA), custom(trans)));
    }


    private static Processor replaceHeader(int width, int height, int bitDepth, int colorType,
                                           int compression, int filter, int interlace)
    throws IOException
    {
        return replace(find(IHDR),
                       custom(createHeader(width, height, bitDepth, colorType,
                                           compression, filter, interlace)));
    }

    private static Chunk extract(String src, int type)
    throws IOException
    {
        return find(type).query(readChunks(new File(src)));
    }

    private static void gen(String src, String dst, Processor... processors)
    throws IOException
    {
        List<Chunk> chunks = readChunks(new File(src));
        for (Processor p : processors)
            p.process(chunks);
        writeChunks(new File(dst), chunks);
    }

    private static Processor swap(int t1, int t2)
    {
        return swap(find(t1), find(t2));
    }

    private static Processor duplicate(int type)
    {
        return addAfter(find(type), find(type));
    }

    private static Processor replace(final Query oldChunk, final Query newChunk)
    {
        return new Processor(){
            public void process(List<Chunk> chunks) {
                chunks.set(chunks.indexOf(oldChunk.query(chunks)), newChunk.query(chunks));
            }
        };
    }

    private static Processor addAfter(final Query after, final Query chunk)
    {
        return new Processor(){
            public void process(List<Chunk> chunks) {
                chunks.add(chunks.indexOf(after.query(chunks)) + 1, chunk.query(chunks));
            }
        };
    }

    private static Query custom(int type, byte[] data)
    throws IOException
    {
        return custom(new Chunk(type, data));
    }

    private static Query custom(final Chunk chunk)
    throws IOException
    {
        return new Query(){
            public Chunk query(List<Chunk> chunks) {
                return chunk;
            }
        };
    }

    private static Processor setLength(final Query q, final int length)
    {
        return new Processor(){
            public void process(List<Chunk> chunks) {
                q.query(chunks).length = length;
            }
        };
    }

    private static Processor setCRC(final Query q, final int crc)
    {
        return new Processor(){
            public void process(List<Chunk> chunks) {
                q.query(chunks).crc = crc;
            }
        };
    }

    private static Processor setType(final Query q, final int type)
    {
        return new Processor(){
            public void process(List<Chunk> chunks) throws IOException {
                Chunk chunk = q.query(chunks);
                chunk.type = type;
                chunk.crc = crc(type, chunk.data);
            }
        };
    }

    private static Processor remove(final Query q)
    {
        return new Processor(){
            public void process(List<Chunk> chunks) {
                chunks.remove(q.query(chunks));
            }
        };
    }

    private static Processor swap(final Query q1, final Query q2)
    {
        return new Processor(){
            public void process(List<Chunk> chunks) {
                Collections.swap(chunks,
                                 chunks.indexOf(q1.query(chunks)),
                                 chunks.indexOf(q2.query(chunks)));
            }
        };
    }

    private static Query find(int type)
    {
        return find(type, 0);
    }

    private static Query find(final int type, final int index)
    {
        return new Query(){
            public Chunk query(List<Chunk> chunks) {
                int count = 0;
                for (Chunk chunk : chunks) {
                    if (chunk.type == type && index == count++)
                        return chunk;
                }
                return null;
            }
        };
    }

    private interface Query
    {
        Chunk query(List<Chunk> chunks);
    }

    private interface Processor
    {
        void process(List<Chunk> chunks) throws IOException;
    }

    private static class Chunk
    {
        public int type;
        public byte[] data;
        public int crc;
        public int length;
        
        public Chunk(int type, byte[] data)
        throws IOException
        {
            this(type, data, crc(type, data));
        }
        
        public Chunk(int type, byte[] data, int crc)
        {
            this.type = type;
            this.data = data;
            this.crc = crc;
            this.length = data.length;
        }
    }

    private static int crc(int type, byte[] bytes)
    throws IOException
    {
        CheckedOutputStream checked = new CheckedOutputStream(new NullOutputStream(), new CRC32());
        DataOutputStream data = new DataOutputStream(checked);
        data.writeInt(type);
        data.write(bytes);
        data.flush();
        return (int)checked.getChecksum().getValue();
    }
    
    private static List<Chunk> readChunks(File src)
    throws IOException
    {
        List<Chunk> chunks = new ArrayList<Chunk>();
        FileInputStream in = new FileInputStream(src);
        try {
            DataInputStream data = new DataInputStream(new BufferedInputStream(in));
            data.readLong(); // signature
            int type;
            do {
                byte[] bytes = new byte[data.readInt()];
                type = data.readInt();
                data.readFully(bytes);
                int crc = data.readInt();
                chunks.add(new Chunk(type, bytes, crc));
            } while (type != IEND);
            return chunks;
        } finally {
            in.close();
        }
    }

    private static void writeChunks(File dst, List<Chunk> chunks)
    throws IOException
    {
        FileOutputStream out = new FileOutputStream(dst);
        try {
            DataOutputStream data = new DataOutputStream(out);
            data.writeLong(0x89504E470D0A1A0AL);
            for (Chunk chunk : chunks) {
                data.writeInt(chunk.length);
                data.writeInt(chunk.type);
                data.write(chunk.data);
                data.writeInt(chunk.crc);
            }
            data.flush();
        } finally {
            out.close();
        }
    }

    private static class NullOutputStream extends OutputStream
    {
        public void write(int b) { }
        public void write(byte[] b) { }
        public void write(byte[] b, int off, int len) { }
    }

    private static Chunk createHeader(int width, int height, int bitDepth, int colorType,
                                      int compression, int filter, int interlace)
    throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(baos);
        data.writeInt(width);
        data.writeInt(height);
        data.writeByte((byte)bitDepth);
        data.writeByte((byte)colorType);
        data.writeByte((byte)compression);
        data.writeByte((byte)filter);
        data.writeByte((byte)interlace);
        return new Chunk(IHDR, baos.toByteArray());
    }
}