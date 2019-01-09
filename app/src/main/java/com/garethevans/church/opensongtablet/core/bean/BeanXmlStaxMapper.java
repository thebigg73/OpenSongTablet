package com.garethevans.church.opensongtablet.core.bean;

import com.garethevans.church.opensongtablet.core.property.BeanProperty;
import com.garethevans.church.opensongtablet.core.property.Property;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class BeanXmlStaxMapper extends BeanXmlMapper {

    public BeanXmlStaxMapper(Bean config) {
        super(config);
    }

    @Override
    public void saveXml(OutputStream out) {

        try {
            XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
            saveXml(xmlWriter);
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to save XML.", e);
        }
    }

    @Override
    public void saveXml(Writer writer) {

        try {
            XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
            saveXml(xmlWriter);
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to save XML.", e);
        }
    }

    public void saveXml(XMLStreamWriter xmlWriter) throws XMLStreamException {

        xmlWriter.writeStartDocument();
        String rootTag = getRootTag();
        if (rootTag != null) {
            xmlWriter.writeStartElement(rootTag);
        }
        saveXml(xmlWriter, getBean());
        if (rootTag != null) {
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndDocument();
    }

    private void saveXml(XMLStreamWriter xmlWriter, Bean bean) throws XMLStreamException {

        for (Property<?> property : bean.getProperties()) {
            if (property instanceof BeanProperty) {
                Bean childBean = ((BeanProperty) property).getValue();
                if (childBean != null) {
                    if (isIncludeGroupTags()) {
                        xmlWriter.writeStartElement(property.getName());
                    }
                    saveXml(xmlWriter, childBean);
                    if (isIncludeGroupTags()) {
                        xmlWriter.writeEndElement();
                    }
                }
            } else {
                saveXml(xmlWriter, property);
            }
        }
    }

    private void saveXml(XMLStreamWriter xmlWriter, Property<?> property) throws XMLStreamException {

        Object value = property.getValue();
        if (value == null) {
            return;
        }
        String tag = property.getName();
        xmlWriter.writeStartElement(tag);
        xmlWriter.writeCharacters(value.toString());
        xmlWriter.writeEndElement();
    }

    @Override
    public void loadXml(Reader reader) {

        try {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
            loadXml(xmlReader);
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to load XML.", e);
        }
    }

    @Override
    public void loadXml(InputStream in) {

        try {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(in);
            loadXml(xmlReader);
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to load XML.", e);
        }
    }

    private void expectTag(XMLStreamReader xmlReader, String tag, boolean closing) throws XMLStreamException {
        expectTag(xmlReader, tag, closing, false);
    }

    private void expectTag(XMLStreamReader xmlReader, String tag, boolean closing, boolean preventNext) throws XMLStreamException {

        if (tag == null) {
            return;
        }
        int event;
        if (preventNext) {
            event = xmlReader.getEventType();
        } else {
            event = xmlReader.nextTag();
        }
        int expectedEvent;
        if (closing) {
            expectedEvent = XMLStreamConstants.END_ELEMENT;
        } else {
            expectedEvent = XMLStreamConstants.START_ELEMENT;
        }
        if (event == expectedEvent) {
            String name = xmlReader.getName().getLocalPart();
            if (!name.equals(tag)) {
                throw new IllegalStateException("Expected XML tag " + tag + " but found " + name + ".");
            }
        } else {
            String type = closing ? "closing" : "opening";
            throw new IllegalStateException("Expected " + type + " '" + tag + "' element XML event (but found '" + xmlReader.getName().getLocalPart() + "' with type " + event + ").");
        }
    }

    public void loadXml(XMLStreamReader xmlReader) throws XMLStreamException {

        String rootTag = getRootTag();
        expectTag(xmlReader, rootTag, false);
        loadXml(xmlReader, getBean());
        expectTag(xmlReader, rootTag, true, true);
    }

    private void loadXml(XMLStreamReader xmlReader, Bean bean) throws XMLStreamException {

        boolean includeGroupTags = isIncludeGroupTags();
        int event = xmlReader.next();
        while ((event != XMLStreamConstants.END_ELEMENT) && (event != XMLStreamConstants.END_DOCUMENT)) {
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xmlReader.getName().getLocalPart();
                Property<?> property = bean.getProperty(name, !includeGroupTags);
                if (property instanceof BeanProperty) {
                    assert (includeGroupTags);
                    Bean childBean = ((BeanProperty) property).getValue();
                    if (childBean != null) {
                        loadXml(xmlReader, childBean);
                    }
                } else {
                    String value = readText(xmlReader).trim();
                    property.setValueAsString(value);
                }
                expectTag(xmlReader, name, true, true);
            }
            event = xmlReader.next();
        }
    }

    private static String readText(XMLStreamReader xmlReader) throws XMLStreamException {

        return xmlReader.getElementText();
    }

}
