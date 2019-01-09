package com.garethevans.church.opensongtablet.core.bean;

import com.garethevans.church.opensongtablet.core.config.Config;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public abstract class BeanXmlMapper extends BeanMapper {

    private String rootTag;

    private boolean includeGroupTags;

    public BeanXmlMapper(Bean bean) {
        super(bean);
        if (bean instanceof Config) {
            this.rootTag = "myprofile";
        }
    }

    public String getRootTag() {
        return this.rootTag;
    }

    public void setRootTag(String rootTag) {
        this.rootTag = rootTag;
    }

    public boolean isIncludeGroupTags() {
        return this.includeGroupTags;
    }

    public void setIncludeGroupTags(boolean includeGroupTags) {
        this.includeGroupTags = includeGroupTags;
    }

    public abstract void loadXml(InputStream in);

    public abstract void loadXml(Reader reader);

    public void loadXmlFromString(String xml) {

        StringReader reader = new StringReader(xml);
        loadXml(reader);
    }

    public abstract void saveXml(OutputStream out);

    public abstract void saveXml(Writer writer);

    public String saveXmlAsString() {

        StringWriter sw = new StringWriter(1024);
        saveXml(sw);
        return sw.toString();
    }

}
