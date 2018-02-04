package com.lichfaker.mqttclientandroid.mqtt;

/**
 * Created by hzl520 on 2018/1/29.
 */

public class Message {

    /**
     * Cmd : 001304
     * Content :
     * CmdID : b5d7f5cc-c773-4370-aaac-90bf38859c94
     */

    private String Cmd;
    private String Content;
    private String CmdID;

    public String getCmd() {
        return Cmd;
    }

    public void setCmd(String Cmd) {
        this.Cmd = Cmd;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String Content) {
        this.Content = Content;
    }

    public String getCmdID() {
        return CmdID;
    }

    public void setCmdID(String CmdID) {
        this.CmdID = CmdID;
    }
}
