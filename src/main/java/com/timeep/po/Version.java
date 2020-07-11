package com.timeep.po;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Li
 **/
@Entity
@Table(name = "tb_version")
public class Version {
    @Id
    @GeneratedValue
    private Long id;

    private int version;
    private Date updateTime;
    private Boolean status;


    public Version() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getIs() {
        return status;
    }

    public void setIs(Boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Version{" +
                "id=" + id +
                ", version=" + version +
                ", updateTime=" + updateTime +
                '}';
    }
}
