module com.splitms {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive java.sql;
    requires transitive jakarta.persistence;
    requires org.hibernate.orm.core;
    exports com.splitms.pages;
    exports com.splitms.utils;
    exports com.splitms.lib;
    exports com.splitms.entities;
    opens com.splitms.entities to org.hibernate.orm.core;
}
