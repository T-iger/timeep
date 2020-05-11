package com.timeep.po;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Li
 **/
@Entity
@Table(name = "tb_owl")
public class Owl {
    @Id
    @GeneratedValue
    private Long id;

    private String object;
    private String property;
    private String subject;

    public Owl() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        return "Owl{" +
                "id=" + id +
                ", object='" + object + '\'' +
                ", property='" + property + '\'' +
                ", subject='" + subject + '\'' +
                '}';
    }
}
