package boogle.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

public class UncheckedBufferedReader extends BufferedReader {

    public UncheckedBufferedReader(InputStreamReader reader) {
        super(reader);
    }

    @Override
    public String readLine() {
        try { return super.readLine(); }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean ready() {
        try { return super.ready(); }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
