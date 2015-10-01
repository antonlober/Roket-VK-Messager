package sweet.messager.vk.utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

import sweet.messager.vk.interfaces.UploadProgressListener;

public class CustomMultiPartEntity extends MultipartEntity {

    private final UploadProgressListener listener;

    public CustomMultiPartEntity(final UploadProgressListener listener) {
        super();
        this.listener = listener;
    }

    public CustomMultiPartEntity(final HttpMultipartMode mode, final UploadProgressListener listener) {
        super(mode);
        this.listener = listener;
    }

    public CustomMultiPartEntity(HttpMultipartMode mode, final String boundary, final Charset charset, final UploadProgressListener listener) {
        super(mode, boundary, charset);
        this.listener = listener;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        super.writeTo(new CountingOutputStream(outstream, this.listener));
    }


    public static class CountingOutputStream extends FilterOutputStream {

        private final UploadProgressListener listener;
        private long transferred;

        public CountingOutputStream(final OutputStream out, final UploadProgressListener listener) {
            super(out);
            this.listener = listener;
            this.transferred = 0;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            this.transferred += len;
            this.listener.transferred(this.transferred);
        }

        public void write(int b) throws IOException {
            out.write(b);
            this.transferred++;
            this.listener.transferred(this.transferred);
        }
    }
}