package com.forfun;

import java.text.SimpleDateFormat;
import java.util.Date;

/*

"FSRQ":"2022-07-05",    // 净值日期
"DWJZ":"7.3210",        // 单位净值
"LJJZ":"7.3210",        // 累计净值
"SDATE":null,
"ACTUALSYI":"",
"NAVTYPE":"1",
"JZZZL":"-0.83",       // 日增长率
"SGZT":"开放申购",      // 申购状态
"SHZT":"开放赎回",      // 赎回状态
"FHFCZ":"",
"FHFCBZ":"",
"DTYPE":null,
"FHSP":""

*/
public class FundHistoricData {
    Date FSRQ;  // 净值日期
    Float DWJZ; // 单位净值
    Float LJJZ; // 累计净值
    Float JZZZL;    // 日增长率
    String SDATE;
    String ACTUALSYI;
    String NAVTYPE;
    String SGZT;
    String SHZT;
    String FHFCZ;
    String FHFCBZ;
    String DTYPE;
    String FHSP;
    Float FQJZ;

    Date preHighFSRQ;   // 前一高值 净值日期
    Float preHighDWJZ;  // 前一高值 单位净值
    Float preHighLJJZ;  // 前一高值 累计净值
    Float preHighFQJZ;  // 前一高值 复权净值
    Float withdraw;     // 回撤 = 复权净值 / 前一高值复权净值
    
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public FundHistoricData(String date, String inputDWJZ, String inputLJJZ, String inputJZZZL, 
        String inputSDATE, 
        String inputACTUALSYI,
        String inputNAVTYPE,
        String inputSGZT,
        String inputSHZT,
        String inputFHFCZ,
        String inputFHFCBZ,
        String inputDTYPE,
        String inputFHSP
        ){
        
        try {
            FSRQ = dateFormat.parse(date);
            DWJZ = Float.valueOf(inputDWJZ);
            LJJZ = Float.valueOf(inputLJJZ);
            if (inputJZZZL.length() != 0) {
                JZZZL = Float.valueOf(inputJZZZL);
            } else {
                JZZZL = 0F;
            }
            SDATE = inputSDATE;
            ACTUALSYI = inputACTUALSYI;
            NAVTYPE = inputNAVTYPE;
            SGZT = inputSGZT;
            SHZT = inputSHZT;
            FHFCZ = inputFHFCZ;
            FHFCBZ = inputFHFCBZ;
            DTYPE = inputDTYPE;
            FHSP = inputFHSP;
        } catch (Exception e) {
            System.out.println("Got exception: ");
            System.out.println("date: " + date);
            System.out.println("inputDWJZ: " + inputDWJZ);
            System.out.println("inputLJJZ: " + inputLJJZ);
            System.out.println("inputJZZZL: " + inputJZZZL);
            System.out.println(e.getCause());
        }
    }

    public void setPreHighFSRQ(Date date) {
        this.preHighFSRQ = date;
    }
    public void setPreHighDWJZ(Float dwjz) {
        this.preHighDWJZ = dwjz;
    }
    public void setPreHighLJJZ(Float ljjz) {
        this.preHighLJJZ = ljjz;
    }
    public void setFQJZ(Float fqjz) {
        this.FQJZ = fqjz;
    }
    public void setPreHighFQJZ(Float fqjz) {
        this.preHighFQJZ = fqjz;
    }

    public Float calculateWithdraw() {
        // 通过复权净值计算回撤, 若为null, 回撤为0
        if(this.preHighFQJZ != null && this.FQJZ != null) {
            this.withdraw = (float) Math.round( (1 - this.FQJZ / this.preHighFQJZ) * 10000 )/100;
        } else {
            this.withdraw = 0F;
        }
        return this.withdraw;
    }

    public String getPreHighFSRQ() {
        return dateFormat.format(this.preHighFSRQ);
    }
    public String getPreHighDWJZ() {
        return this.preHighDWJZ.toString();
    }
    public String getPreHighLJJZ() {
        return this.preHighLJJZ.toString();
    }

    public String getFSRQ() {
        return dateFormat.format(this.FSRQ);
    }

    public String getDWJZ() {
        return this.DWJZ.toString();
    }

    public String getLJJZ() {
        return this.LJJZ.toString();
    }

    public String getWithdraw() {
        return this.withdraw.toString();
    }
}