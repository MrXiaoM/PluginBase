package top.mrxiaom.pluginbase.utils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckForNull;
import java.io.*;

public class Bytes {
    public static ByteArrayDataOutput newDataOutput() {
        return new ByteArrayDataOutputStream(new ByteArrayOutputStream());
    }

    public static ByteArrayDataInput newDataInput(byte[] bytes) {
        return newDataInput(new ByteArrayInputStream(bytes));
    }

    public static ByteArrayDataInput newDataInput(ByteArrayInputStream byteArrayInputStream) {
        return new ByteArrayDataInputStream(byteArrayInputStream);
    }

    public static class ByteArrayDataOutputStream implements ByteArrayDataOutput {
        final DataOutput output;
        final ByteArrayOutputStream byteArrayOutputStream;

        public ByteArrayDataOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
            this.byteArrayOutputStream = byteArrayOutputStream;
            this.output = new DataOutputStream(byteArrayOutputStream);
        }
        @Override
        public void write(int b) {
            try {
                this.output.write(b);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        @Override
        public void write(byte @NotNull [] b) {
            try {
                this.output.write(b);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        @Override
        public void write(byte @NotNull [] b, int off, int len) {
            try {
                this.output.write(b, off, len);
            } catch (IOException var5) {
                throw new AssertionError(var5);
            }
        }
        @Override
        public void writeBoolean(boolean v) {
            try {
                this.output.writeBoolean(v);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        @Override
        public void writeByte(int v) {
            try {
                this.output.writeByte(v);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        @Override
        public void writeBytes(@NotNull String s) {
            try {
                this.output.writeBytes(s);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        @Override
        public void writeChar(int v) {
            try {
                this.output.writeChar(v);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        @Override
        public void writeChars(@NotNull String s) {
            try {
                this.output.writeChars(s);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        @Override
        public void writeDouble(double v) {
            try {
                this.output.writeDouble(v);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        @Override
        public void writeFloat(float v) {
            try {
                this.output.writeFloat(v);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        @Override
        public void writeInt(int v) {
            try {
                this.output.writeInt(v);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        @Override
        public void writeLong(long v) {
            try {
                this.output.writeLong(v);
            } catch (IOException var4) {
                throw new AssertionError(var4);
            }
        }
        @Override
        public void writeShort(int v) {
            try {
                this.output.writeShort(v);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        @Override
        public void writeUTF(@NotNull String s) {
            try {
                this.output.writeUTF(s);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        @Override
        public byte @NotNull [] toByteArray() {
            return this.byteArrayOutputStream.toByteArray();
        }
    }

    public static class ByteArrayDataInputStream implements ByteArrayDataInput {
        final DataInput input;

        public ByteArrayDataInputStream(ByteArrayInputStream byteArrayInputStream) {
            this.input = new DataInputStream(byteArrayInputStream);
        }
        @Override
        public void readFully(byte @NotNull [] b) {
            try {
                this.input.readFully(b);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        @Override
        public void readFully(byte @NotNull [] b, int off, int len) {
            try {
                this.input.readFully(b, off, len);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        @Override
        public int skipBytes(int n) {
            try {
                return this.input.skipBytes(n);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        @Override
        public boolean readBoolean() {
            try {
                return this.input.readBoolean();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        @Override
        public byte readByte() {
            try {
                return this.input.readByte();
            } catch (EOFException e) {
                throw new IllegalStateException(e);
            } catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }
        @Override
        public int readUnsignedByte() {
            try {
                return this.input.readUnsignedByte();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        @Override
        public short readShort() {
            try {
                return this.input.readShort();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        @Override
        public int readUnsignedShort() {
            try {
                return this.input.readUnsignedShort();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        @Override
        public char readChar() {
            try {
                return this.input.readChar();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        @Override
        public int readInt() {
            try {
                return this.input.readInt();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        @Override
        public long readLong() {
            try {
                return this.input.readLong();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        @Override
        public float readFloat() {
            try {
                return this.input.readFloat();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        @Override
        public double readDouble() {
            try {
                return this.input.readDouble();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        @Override
        @CheckForNull
        public String readLine() {
            try {
                return this.input.readLine();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        @Override
        @NotNull
        public String readUTF() {
            try {
                return this.input.readUTF();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
