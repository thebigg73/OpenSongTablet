package com.garethevans.church.opensongtablet.core.format;

import java.io.IOException;

public class AppendableAdapter implements Appendable {

    private final Appendable delegate;

    private boolean updated;

    private String separator;

    public AppendableAdapter(Appendable delegate) {
        this.delegate = delegate;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void setSeparatorIfUpdated(String separator) {
        if (this.updated) {
            this.separator = separator;
            this.updated = false;
        }
    }
    public boolean isUpdated() {
        return this.updated;
    }

    public void resetUpdated() {
        this.updated = false;
    }

    private void appendSeparator() throws IOException {
        if (this.separator != null) {
            this.delegate.append(separator);
            this.separator = null;
        }
    }

    @Override
    public Appendable append(char c) throws IOException {
        this.delegate.append(c);
        return this;
    }

    @Override
    public Appendable append(CharSequence charSequence) throws IOException {
        this.delegate.append(charSequence);
        return this;
    }

    @Override
    public Appendable append(CharSequence charSequence, int offset, int len) throws IOException {
        this.delegate.append(charSequence, offset, len);
        return this;
    }
}
