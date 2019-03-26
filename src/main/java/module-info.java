module com.theoryinpractise.halbuilder.xml {
    exports com.theoryinpractise.halbuilder.xml;
    requires jdom; // FIXME filename-based module
    requires java.xml;
    requires transitive com.theoryinpractise.halbuilder.api;
    requires transitive com.theoryinpractise.halbuilder.core;
}
