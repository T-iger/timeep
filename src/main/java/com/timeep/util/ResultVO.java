package com.timeep.util;



import java.util.Date;

/**
 * @ClassName ResultDTO
 * @Description
 * @Author wangjie
 * @Date 2020/7/4 11:22 下午
 * @Email wangjie_fourth@163.com
 **/

public class ResultVO {
    private String id;
    private Date updateTime;
    private String status;
    private String msg;

    private static final String SUCCESS_STATUS = "1";
    private static final String FAIL_STATUS = "0";

    public static ResultVO success(String id, Date updateTime) {
        return new ResultVO(id, updateTime, SUCCESS_STATUS, "操作成功");
    }

    public static ResultVO fail(String id, Date updateTime, String msg) {
        return new ResultVO(id, updateTime, FAIL_STATUS, msg);
    }

    public static ResultVO nullFail() {
        return new ResultVO("", null, "", "参数为空");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ResultVO(String id, Date updateTime, String status, String msg) {
        this.id = id;
        this.updateTime = updateTime;
        this.status = status;
        this.msg = msg;
    }
}
