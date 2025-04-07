module project.jfx.client {
	exports com.project.datasource;
	exports com.project.dao;
	exports com.project.model;
	exports com.project.app;
	exports com.project.controller;

	requires javafx.base;
	requires javafx.fxml;
	requires javafx.controls;
	requires transitive javafx.graphics;
	requires com.zaxxer.hikari;
	requires transitive java.sql;
	requires org.hsqldb;
	requires org.slf4j;
	requires ch.qos.logback.classic;
	requires ch.qos.logback.core;
	requires java.desktop;

	opens com.project.app to javafx.graphics, javafx.fxml, javafx.base, javafx.controls;
	opens com.project.model to javafx.graphics, javafx.fxml, javafx.base, javafx.controls;
	opens com.project.controller to javafx.graphics, javafx.fxml, javafx.base, javafx.controls;
}