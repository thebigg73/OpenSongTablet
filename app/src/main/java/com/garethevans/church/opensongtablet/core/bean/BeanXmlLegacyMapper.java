package com.garethevans.church.opensongtablet.core.bean;

import com.garethevans.church.opensongtablet.core.property.BeanProperty;
import com.garethevans.church.opensongtablet.core.property.Property;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Legacy implementation of {@link BeanXmlMapper}. Will be deleted after cleanup.
 */
public class BeanXmlLegacyMapper extends BeanXmlMapper {

    public BeanXmlLegacyMapper(Bean config) {
        super(config);
    }

    @Override
    public void loadXml(InputStream in) {

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(in, null);
            loadXml(xpp);
        } catch (IOException | XmlPullParserException e) {
            throw new IllegalStateException("Loading XML failed.", e);
        }
    }

    @Override
    public void loadXml(Reader reader) {

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(reader);
            loadXml(xpp);
        } catch (IOException | XmlPullParserException e) {
            throw new IllegalStateException("Loading XML failed.", e);
        }
    }

    public void loadXml(XmlPullParser xpp) throws IOException, XmlPullParserException {

        Bean bean = getBean();
        int eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String name = xpp.getName();
                String value = xpp.nextText();
                bean.setValue(name, value);
            }
            eventType = xpp.next();
        }
    }

    @Override
    public void saveXml(OutputStream out) {

        try {
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            saveXml(writer);
            writer.close();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to close writer for XML.", e);
        }
    }

    @Override
    public void saveXml(Writer writer) {

        try {
            String xml = saveXmlAsString();
            writer.append(xml);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save XML.", e);
        }
    }


    @Override
    public String saveXmlAsString() {

        StringWriter sw = new StringWriter(1024);
        sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        String rootTag = getRootTag();
        writeXmlTag(sw, rootTag, false);
        sw.append('\n');
        saveXml(sw, getBean(), "  ", "  ");
        writeXmlTag(sw, rootTag, true);
        sw.append('\n');
        return sw.toString();
    }

    public void saveXml(Appendable out, Bean bean, String indent, String currentIndent) {

        try {
            boolean groupTags = isIncludeGroupTags();
            for (Property<?> p : bean.getProperties()) {
                if (p instanceof BeanProperty) {
                    Bean childBean = ((BeanProperty) p).getValue();
                    if (childBean != null) {
                        String childIndent = currentIndent;
                        if (groupTags) {
                            out.append(currentIndent);
                            writeXmlTag(out, p.getName(), false);
                            out.append('\n');
                            childIndent = currentIndent + indent;
                        }
                        saveXml(out, childBean, indent, childIndent);
                        if (groupTags) {
                            out.append(currentIndent);
                            writeXmlTag(out, p.getName(), true);
                            out.append('\n');
                        }
                    }
                } else {
                    saveXml(out, p, currentIndent);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("IO error whilst writing XML.", e);
        }
    }

    private void saveXml(Appendable out, Property<?> property, String indent) throws IOException {

        Object value = property.getValue();
        if (value == null) {
            return;
        }
        out.append(indent);
        writeXmlTag(out, property.getName(), false);
        out.append(value.toString());
        writeXmlTag(out, property.getName(), true);
        out.append('\n');
    }

    private void writeXmlTag(Appendable out, String tag, boolean closing) {

        try {
            out.append('<');
            if (closing) {
                out.append('/');
            }
            out.append(tag);
            out.append('>');
        } catch (IOException e) {
            throw new IllegalStateException("IO error whilst writing XML.", e);
        }
    }

}
